/*
 * Copyright (c) 2014, Lukas Tenbrink.
 * http://lukas.axxim.net
 *
 * You are free to:
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes, unless you have a permit by the creator.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package ivorius.ivtoolkit.blocks;

import ivorius.ivtoolkit.raytracing.IvRaytraceableAxisAlignedBox;
import ivorius.ivtoolkit.raytracing.IvRaytraceableObject;
import ivorius.ivtoolkit.raytracing.IvRaytracedIntersection;
import ivorius.ivtoolkit.raytracing.IvRaytracer;
import ivorius.ivtoolkit.tools.MCRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.*;

/**
 * Created by lukas on 11.02.14.
 */
public class IvBlockCollection implements Iterable<BlockCoord>
{
    private final Block[] blocks;
    private final byte[] metas;
    public final int width;
    public final int height;
    public final int length;

    public IvBlockCollection(int width, int height, int length)
    {
        this(airArray(width, height, length), new byte[width * height * length], width, height, length);
    }

    private static Block[] airArray(int width, int height, int length)
    {
        Block[] blocks = new Block[width * height * length];
        Arrays.fill(blocks, Blocks.air);
        return blocks;
    }

    public IvBlockCollection(Block[] blocks, byte[] metas, int width, int height, int length)
    {
        if (blocks.length != width * height * length || metas.length != blocks.length)
        {
            throw new IllegalArgumentException();
        }

        this.blocks = blocks;
        this.metas = metas;
        this.width = width;
        this.height = height;
        this.length = length;
    }

    public IvBlockCollection(NBTTagCompound compound, MCRegistry registry)
    {
        width = compound.getInteger("width");
        height = compound.getInteger("height");
        length = compound.getInteger("length");

        metas = compound.getByteArray("metadata");

        IvBlockMapper mapper = new IvBlockMapper(compound, "mapping", registry);
        blocks = mapper.createBlocksFromNBT(compound.getCompoundTag("blocks"));
        if (blocks.length != width * height * length)
        {
            throw new RuntimeException("Block collection length is " + blocks.length + " but should be " + width + " * " + height + " * " + length);
        }
    }

    public Block getBlock(BlockCoord coord)
    {
        if (!hasCoord(coord))
        {
            return Blocks.air;
        }

        return blocks[indexFromCoord(coord)];
    }

    public byte getMetadata(BlockCoord coord)
    {
        if (!hasCoord(coord))
        {
            return 0;
        }

        return metas[indexFromCoord(coord)];
    }

    public void setBlockAndMetadata(BlockCoord coord, Block block, byte meta)
    {
        if (!hasCoord(coord))
        {
            return;
        }

        int index = indexFromCoord(coord);
        blocks[index] = block;
        metas[index] = meta;
    }

    public void setBlock(BlockCoord coord, Block block)
    {
        if (!hasCoord(coord))
        {
            return;
        }

        blocks[indexFromCoord(coord)] = block;
    }

    public void setMetadata(BlockCoord coord, byte meta)
    {
        if (!hasCoord(coord))
        {
            return;
        }

        metas[indexFromCoord(coord)] = meta;
    }

    private int indexFromCoord(BlockCoord coord)
    {
        return ((coord.z * height) + coord.y) * width + coord.x;
    }

    public boolean hasCoord(BlockCoord coord)
    {
        return coord.x >= 0 && coord.x < width && coord.y >= 0 && coord.y < height && coord.z >= 0 && coord.z < length;
    }

    public boolean shouldRenderSide(BlockCoord coord, ForgeDirection side)
    {
        BlockCoord sideCoord = coord.add(side.offsetX, side.offsetY, side.offsetZ);

        Block block = getBlock(sideCoord);
        return !block.isOpaqueCube();
    }

    public MovingObjectPosition rayTrace(Vec3 position, Vec3 direction)
    {
        IvRaytraceableAxisAlignedBox containingBox = new IvRaytraceableAxisAlignedBox(null, 0.001, 0.001, 0.001, width - 0.002, height - 0.002, length - 0.002);
        IvRaytracedIntersection intersection = IvRaytracer.getFirstIntersection(Arrays.<IvRaytraceableObject>asList(containingBox), position.xCoord, position.yCoord, position.zCoord, direction.xCoord, direction.yCoord, direction.zCoord);

        if (intersection != null)
        {
            position = Vec3.createVectorHelper(intersection.getX(), intersection.getY(), intersection.getZ());
            BlockCoord curCoord = new BlockCoord(MathHelper.floor_double(position.xCoord), MathHelper.floor_double(position.yCoord), MathHelper.floor_double(position.zCoord));
            ForgeDirection hitSide = ((ForgeDirection) intersection.getHitInfo()).getOpposite();

            while (hasCoord(curCoord))
            {
                if (getBlock(curCoord).getMaterial() != Material.air)
                    return new MovingObjectPosition(curCoord.x, curCoord.y, curCoord.z, hitSide.getOpposite().ordinal(), position);

                hitSide = getExitSide(position, direction);

                if (hitSide.offsetX != 0)
                {
                    double offX = hitSide.offsetX > 0 ? 1.0001 : -0.0001;
                    double dirLength = ((curCoord.x + offX) - position.xCoord) / direction.xCoord;
                    position = Vec3.createVectorHelper(curCoord.x + offX, position.yCoord + direction.yCoord * dirLength, position.zCoord + direction.zCoord * dirLength);
                }
                else if (hitSide.offsetY != 0)
                {
                    double offY = hitSide.offsetY > 0 ? 1.0001 : -0.0001;
                    double dirLength = ((curCoord.y + offY) - position.yCoord) / direction.yCoord;
                    position = Vec3.createVectorHelper(position.xCoord + direction.xCoord * dirLength, curCoord.y + offY, position.zCoord + direction.zCoord * dirLength);
                }
                else
                {
                    double offZ = hitSide.offsetZ > 0 ? 1.0001 : -0.0001;
                    double dirLength = ((curCoord.z + offZ) - position.zCoord) / direction.zCoord;
                    position = Vec3.createVectorHelper(position.xCoord + direction.xCoord * dirLength, position.yCoord + direction.yCoord * dirLength, curCoord.z + offZ);
                }

                curCoord = curCoord.add(hitSide.offsetX, hitSide.offsetY, hitSide.offsetZ);
            }
        }

        return null;
    }

    private ForgeDirection getExitSide(Vec3 position, Vec3 direction)
    {
        double innerX = ((position.xCoord % 1.0) + 1.0) % 1.0;
        double innerY = ((position.yCoord % 1.0) + 1.0) % 1.0;
        double innerZ = ((position.zCoord % 1.0) + 1.0) % 1.0;

        double xDist = direction.xCoord > 0.0 ? ((1.0 - innerX) / direction.xCoord) : (innerX / -direction.xCoord);
        double yDist = direction.yCoord > 0.0 ? ((1.0 - innerY) / direction.yCoord) : (innerY / -direction.yCoord);
        double zDist = direction.zCoord > 0.0 ? ((1.0 - innerZ) / direction.zCoord) : (innerZ / -direction.zCoord);

        if (xDist < yDist && xDist < zDist)
            return direction.xCoord > 0.0 ? ForgeDirection.EAST : ForgeDirection.WEST;
        else if (yDist < zDist)
            return direction.yCoord > 0.0 ? ForgeDirection.UP : ForgeDirection.DOWN;
        else
            return direction.zCoord > 0.0 ? ForgeDirection.SOUTH : ForgeDirection.NORTH;
    }

    public int getBlockMultiplicity()
    {
        Set<Block> blockSet = new HashSet<>();
        Collections.addAll(blockSet, blocks);
        return blockSet.size();
    }

    public NBTTagCompound createTagCompound()
    {
        NBTTagCompound compound = new NBTTagCompound();
        IvBlockMapper mapper = new IvBlockMapper();

        compound.setInteger("width", width);
        compound.setInteger("height", height);
        compound.setInteger("length", length);

        compound.setByteArray("metadata", metas);

        mapper.addMapping(blocks);
        compound.setTag("mapping", mapper.createTagList());
        compound.setTag("blocks", mapper.createNBTForBlocks(blocks));
        return compound;
    }

    @Override
    public String toString()
    {
        return "IvBlockCollection{" +
                "length=" + length +
                ", height=" + height +
                ", width=" + width +
                '}';
    }

    @Override
    public Iterator<BlockCoord> iterator()
    {
        return new BlockAreaIterator(new BlockCoord(0, 0, 0), new BlockCoord(width - 1, height - 1, length - 1));
    }
}
