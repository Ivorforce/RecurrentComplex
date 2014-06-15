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

package ivorius.structuregen.ivtoolkit;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;

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

    public IvBlockCollection(NBTTagCompound compound)
    {
        width = compound.getInteger("width");
        height = compound.getInteger("height");
        length = compound.getInteger("length");

        metas = compound.getByteArray("metadata");

        IvBlockMapper mapper = new IvBlockMapper(compound, "mapping");
        blocks = mapper.createBlocksFromNBT(compound.getCompoundTag("blocks"));
        if (blocks.length != width * height * length)
        {
            throw new RuntimeException("Block collection length is " + blocks.length + " but should be " + width + " * " + height + " * " + length);
        }
    }

    public Block getBlock(int x, int y, int z)
    {
        if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= length)
        {
            return Blocks.air;
        }

        return blocks[((z * height) + y) * width + x];
    }

    public byte getMetadata(int x, int y, int z)
    {
        if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= length)
        {
            return 0;
        }

        return metas[((z * height) + y) * width + x];
    }

    public void setBlock(int x, int y, int z, Block block)
    {
        if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= length)
        {
            return;
        }

        blocks[((z * height) + y) * width + x] = block;
    }

    public void setMetadata(int x, int y, int z, byte meta)
    {
        if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= length)
        {
            return;
        }

        metas[((z * height) + y) * width + x] = meta;
    }

    public boolean shouldRenderSide(int x, int y, int z, int side)
    {
        if (side == 0)
        {
            y--;
        }
        else if (side == 1)
        {
            y++;
        }
        else if (side == 2)
        {
            z--;
        }
        else if (side == 3)
        {
            z++;
        }
        else if (side == 4)
        {
            x--;
        }
        else if (side == 5)
        {
            x++;
        }

        Block block = getBlock(x, y, z);
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
