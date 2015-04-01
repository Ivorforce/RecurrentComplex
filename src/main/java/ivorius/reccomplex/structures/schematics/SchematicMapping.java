/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.schematics;

import gnu.trove.map.TShortObjectMap;
import gnu.trove.map.hash.TShortObjectHashMap;
import gnu.trove.procedure.TShortObjectProcedure;
import gnu.trove.set.TShortSet;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Set;

/**
 * Created by lukas on 22.03.15.
 */
public class SchematicMapping
{
    public static final String COMPOUND_KEY = "SchematicaMapping";
    private TShortObjectMap<Block> blockMapping = new TShortObjectHashMap<>();

    public SchematicMapping()
    {
    }

    public SchematicMapping(NBTTagCompound compound)
    {
        Set<String> names = compound.func_150296_c();
        for (String name : names)
        {
            Block block = Block.getBlockFromName(name);
            if (block != null)
                blockMapping.put(compound.getShort(name), block);
        }
    }

    public void putBlock(int id, Block block)
    {
        blockMapping.put((short) id, block);
    }

    public void removeBlock(int id)
    {
        blockMapping.remove((short) id);
    }

    public TShortSet allIDs()
    {
        return blockMapping.keySet();
    }

    public NBTTagCompound writeToNBT()
    {
        final NBTTagCompound compound = new NBTTagCompound();
        blockMapping.forEachEntry(new TShortObjectProcedure<Block>()
        {
            @Override
            public boolean execute(short a, Block b)
            {
                compound.setShort(Block.blockRegistry.getNameForObject(b), a);
                return true;
            }
        });
        return compound;
    }

    public Block blockFromID(int id)
    {
        Block block = blockMapping.get((short) id);
        return block != null ? block : Block.getBlockById(id);
    }
}
