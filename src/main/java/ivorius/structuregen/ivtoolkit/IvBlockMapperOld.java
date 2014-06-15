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
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukas on 11.02.14.
 */
public class IvBlockMapperOld
{
    public List<Block> mappings = new ArrayList<Block>();
    public List<Integer> currentIDs = new ArrayList<Integer>();
    public List<Byte> currentMetadatas = new ArrayList<Byte>();

    public void addBlock(Block block, byte metadata)
    {
        this.currentIDs.add(getMapping(block));
        currentMetadatas.add(metadata);
    }

    public int getMapping(Block block)
    {
        int index = mappings.indexOf(block);

        if (index < 0)
        {
            mappings.add(block);
            return mappings.size() - 1;
        }

        return index;
    }

    public Block getMapping(int index)
    {
        return this.mappings.get(index);
    }

    public byte getMeta(int index)
    {
        return this.currentMetadatas.get(index);
    }

    public void outputMapping(Logger logger)
    {
        logger.debug(getAttributeString("map", "int[]", "", "", getNames(currentIDs)));
        logger.debug(getAttributeString("metas", "byte[]", "", "", getNames(currentMetadatas)));

        ArrayList<String> registryNames = new ArrayList<String>();
        for (int i = 0; i < mappings.size(); i++)
        {
            registryNames.add(Block.blockRegistry.getNameForObject(getMapping(i)));
        }

        logger.debug(getAttributeString("mappings", "String[]", "\"", "\"", registryNames));
    }

    public void writeToFile(String filename)
    {
        NBTTagCompound cmp = new NBTTagCompound();
        writeToNBT(cmp);

        try
        {
            CompressedStreamTools.safeWrite(cmp, new File(Minecraft.getMinecraft().mcDataDir, filename));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void readFromResource(ResourceLocation location)
    {
        IResource res = null;

        try
        {
            res = Minecraft.getMinecraft().getResourceManager().getResource(location);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (res != null)
        {
            NBTTagCompound cmp = null;

            try
            {
                cmp = CompressedStreamTools.read(new DataInputStream(res.getInputStream()));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            if (cmp != null)
            {
                readFromNBT(cmp);
            }
        }
    }

    public void writeToNBT(NBTTagCompound tagCompound)
    {
        NBTTagList mappingsList = new NBTTagList();
        for (int i = 0; i < mappings.size(); i++)
        {
            mappingsList.appendTag(new NBTTagString(Block.blockRegistry.getNameForObject(getMapping(i))));
        }
        tagCompound.setTag("mappings", mappingsList);

        byte[] metas = new byte[currentMetadatas.size()];
        for (int i = 0; i < metas.length; i++)
        {
            metas[i] = currentMetadatas.get(i);
        }
        tagCompound.setByteArray("metas", metas);

        int[] ids = new int[currentIDs.size()];
        for (int i = 0; i < ids.length; i++)
        {
            ids[i] = currentIDs.get(i);
        }
        tagCompound.setIntArray("ids", ids);
    }

    public void readFromNBT(NBTTagCompound tagCompound)
    {
        NBTTagList mappingsList = tagCompound.getTagList("mappings", Constants.NBT.TAG_STRING);
        for (int i = 0; i < mappingsList.tagCount(); i++)
        {
            mappings.add((Block) Block.blockRegistry.getObject(mappingsList.getStringTagAt(i)));
        }

        byte[] metas = tagCompound.getByteArray("metas");
        for (byte b : metas)
        {
            currentMetadatas.add(b);
        }

        int[] ids = tagCompound.getIntArray("ids");
        for (int b : ids)
        {
            currentIDs.add(b);
        }
    }

    private List<String> getNames(List list)
    {
        ArrayList<String> stringList = new ArrayList<String>(list.size());

        for (Object s : list)
        {
            stringList.add(s.toString());
        }

        return stringList;
    }

    private String getAttributeString(String name, String type, String prefix, String suffix, List<String> names)
    {
        String mappingString = "";

        for (int i = 0; i < names.size(); i++)
        {
            mappingString += prefix + names.get(i) + suffix + (i == names.size() - 1 ? "" : ", ");
        }

        return "public static " + type + " " + name + " = new " + type + "{" + mappingString + "};";
    }

    public IvBlockCollection readCollectionFromMapping(int width, int height)
    {
        int length = currentIDs.size() / width / height;
        IvBlockCollection collection = new IvBlockCollection(width, height, length);

        int index = 0;
        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                for (int z = 0; z < length; z++)
                {
                    collection.setBlock(x, y, z, getMapping(currentIDs.get(index)));
                    collection.setMetadata(x, y, z, getMeta(index));
                    index++;
                }
            }
        }

        return collection;
    }
}
