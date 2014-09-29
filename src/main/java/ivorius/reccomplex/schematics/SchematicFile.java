/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.schematics;

import ivorius.reccomplex.RecurrentComplex;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukas on 29.09.14.
 */
public class SchematicFile
{
    public short width, height, length;
    public short weOriginX, weOriginY, weOriginZ;
    public Block[] blocks;
    public byte[] metadatas;
    public List<NBTTagCompound> entityCompounds = new ArrayList<>();
    public List<NBTTagCompound> tileEntityCompounds = new ArrayList<>();

    public SchematicFile(NBTTagCompound tagCompound) throws UnsupportedSchematicFormatException
    {
        String materials = tagCompound.getString("Materials");
        if (!(materials.equals("Alpha")))
            throw new UnsupportedSchematicFormatException(materials);

        width = tagCompound.getShort("Width");
        height = tagCompound.getShort("Height");
        length = tagCompound.getShort("Length");

        weOriginX = tagCompound.getShort("WEOriginX");
        weOriginY = tagCompound.getShort("WEOriginY");
        weOriginZ = tagCompound.getShort("WEOriginZ");

        metadatas = tagCompound.getByteArray("Data");
        byte[] blocks = tagCompound.getByteArray("Blocks");

        this.blocks = new Block[blocks.length];
        for (int i = 0; i < blocks.length; i++)
            this.blocks[i] = Block.getBlockById(blocks[i]);

        NBTTagList entities = tagCompound.getTagList("Entities", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < entities.tagCount(); i++)
            entityCompounds.add(entities.getCompoundTagAt(i));

        NBTTagList tileEntities = tagCompound.getTagList("TileEntities", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tileEntities.tagCount(); i++)
            tileEntityCompounds.add(tileEntities.getCompoundTagAt(i));
    }

    public void generate(World world, int x, int y, int z)
    {
        for (int xP = 0; xP < width; xP++)
            for (int yP = 0; yP < height; yP++)
                for (int zP = 0; zP < length; zP++)
                {
                    int index = xP + (yP * length + zP) * width;

                    if (blocks[index] != null)
                        world.setBlock(x + xP, y + yP, z + zP, blocks[index], metadatas[index], 3);
                }
    }

    public static class UnsupportedSchematicFormatException extends Exception
    {
        public final String format;

        public UnsupportedSchematicFormatException(String format)
        {
            this.format = format;
        }
    }
}
