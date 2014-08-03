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

import ivorius.ivtoolkit.math.IvBytePacker;
import ivorius.ivtoolkit.tools.IvNBTHelper;
import ivorius.ivtoolkit.tools.MCRegistry;
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

    public IvBlockMapper(NBTTagCompound compound, String key, MCRegistry registry)
    {
        this(compound.getTagList(key, Constants.NBT.TAG_STRING), registry);
    }

    public IvBlockMapper(NBTTagList list, MCRegistry registry)
    {
        mapping = new ArrayList<>(list.tagCount());

        for (int i = 0; i < list.tagCount(); i++)
        {
            mapping.add(registry.blockFromID(list.getStringTagAt(i)));
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
            String name = Block.blockRegistry.getNameForObject(block);

            if (name != null)
                list.appendTag(new NBTTagString(name));
            else
            {
                list.appendTag(new NBTTagString("minecraft:stone"));
                System.out.println("Did not find name for block '" + block + "' to map");
            }
        }

        return list;
    }

    public NBTTagCompound createNBTForBlocks(Block[] blocks)
    {
        NBTTagCompound compound = new NBTTagCompound();

        int[] vals = new int[blocks.length];
        for (int i = 0; i < blocks.length; i++)
            vals[i] = getMapping(blocks[i]);
        NBTTagCompound compressed = new NBTTagCompound();
        IvNBTHelper.writeCompressed("data", vals, getMapSize() - 1, compressed);
        compound.setTag("blocksCompressed", compressed);

//        if (getMapSize() <= Byte.MAX_VALUE)
//        {
//            byte[] byteArray = new byte[blocks.length];
//
//            for (int i = 0; i < blocks.length; i++)
//            {
//                byteArray[i] = (byte) getMapping(blocks[i]);
//            }
//
//            compound.setByteArray("blockBytes", byteArray);
//        }
//        else
//        {
//            int[] intArray = new int[blocks.length];
//
//            for (int i = 0; i < blocks.length; i++)
//            {
//                intArray[i] = getMapping(blocks[i]);
//            }
//
//            compound.setIntArray("blockInts", intArray);
//        }

        return compound;
    }

    public Block[] createBlocksFromNBT(NBTTagCompound compound)
    {
        Block[] blocks;

        if (compound.hasKey("blocksCompressed"))
        {
            NBTTagCompound compressed = compound.getCompoundTag("blocksCompressed");
            int[] vals = IvNBTHelper.readCompressed("data", compressed);

            blocks = new Block[vals.length];

            for (int i = 0; i < vals.length; i++)
                blocks[i] = getBlock(vals[i]);
        }
        else if (compound.hasKey("blockBytes"))
        {
            byte[] byteArray = compound.getByteArray("blockBytes");
            blocks = new Block[byteArray.length];

            for (int i = 0; i < byteArray.length; i++)
                blocks[i] = getBlock(byteArray[i]);
        }
        else if (compound.hasKey("blockInts"))
        {
            int[] intArray = compound.getIntArray("blockInts");
            blocks = new Block[intArray.length];

            for (int i = 0; i < intArray.length; i++)
                blocks[i] = getBlock(intArray[i]);
        }
        else
        {
            throw new RuntimeException("Unrecognized block collection type " + compound);
        }

        return blocks;
    }
}
