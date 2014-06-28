/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.ivtoolkit.blocks;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukas on 11.02.14.
 */
public class IvBlockMapper
{
    private List<Block> mapping;

    public IvBlockMapper()
    {
        mapping = new ArrayList<>();
    }

    public IvBlockMapper(NBTTagCompound compound, String key)
    {
        this(compound.getTagList(key, Constants.NBT.TAG_STRING));
    }

    public IvBlockMapper(NBTTagList list)
    {
        mapping = new ArrayList<>(list.tagCount());

        for (int i = 0; i < list.tagCount(); i++)
        {
            mapping.add((Block) Block.blockRegistry.getObject(list.getStringTagAt(i)));
        }
    }

    public void addMapping(Block block)
    {
        if (!mapping.contains(block))
        {
            mapping.add(block);
        }
    }

    public void addMapping(Block[] blocks)
    {
        for (Block block : blocks)
        {
            addMapping(block);
        }
    }

    public int getMapping(Block block)
    {
        return mapping.indexOf(block);
    }

    public Block getBlock(int mapping)
    {
        return this.mapping.get(mapping);
    }

    public int getMapSize()
    {
        return mapping.size();
    }

    public NBTTagList createTagList()
    {
        NBTTagList list = new NBTTagList();

        for (Block block : mapping)
        {
            list.appendTag(new NBTTagString(Block.blockRegistry.getNameForObject(block)));
        }

        return list;
    }

    public NBTTagCompound createNBTForBlocks(Block[] blocks)
    {
        NBTTagCompound compound = new NBTTagCompound();

        if (getMapSize() <= Byte.MAX_VALUE)
        {
            byte[] byteArray = new byte[blocks.length];

            for (int i = 0; i < blocks.length; i++)
            {
                byteArray[i] = (byte) getMapping(blocks[i]);
            }

            compound.setByteArray("blockBytes", byteArray);
        }
        else
        {
            int[] intArray = new int[blocks.length];

            for (int i = 0; i < blocks.length; i++)
            {
                intArray[i] = getMapping(blocks[i]);
            }

            compound.setIntArray("blockInts", intArray);
        }

        return compound;
    }

    public Block[] createBlocksFromNBT(NBTTagCompound compound)
    {
        Block[] blocks;

        if (compound.hasKey("blockBytes"))
        {
            byte[] byteArray = compound.getByteArray("blockBytes");
            blocks = new Block[byteArray.length];

            for (int i = 0; i < byteArray.length; i++)
            {
                blocks[i] = getBlock(byteArray[i]);
            }
        }
        else if (compound.hasKey("blockInts"))
        {
            int[] intArray = compound.getIntArray("blockInts");
            blocks = new Block[intArray.length];

            for (int i = 0; i < intArray.length; i++)
            {
                blocks[i] = getBlock(intArray[i]);
            }
        }
        else
        {
            throw new RuntimeException("Unrecognized block collection type " + compound);
        }

        return blocks;
    }
}
