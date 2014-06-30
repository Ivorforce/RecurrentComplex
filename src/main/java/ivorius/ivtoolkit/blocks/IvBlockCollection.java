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

import ivorius.ivtoolkit.tools.MCRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Iterator;

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
        this(new Block[width * height * length], new byte[width * height * length], width, height, length);
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
