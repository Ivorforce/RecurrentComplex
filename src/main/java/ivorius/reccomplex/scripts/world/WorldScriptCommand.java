/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.scripts.world;

import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.random.WeightedSelector;
import ivorius.ivtoolkit.tools.IvCollections;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.blocks.SpawnCommandLogic;
import ivorius.reccomplex.structures.StructureLoadContext;
import ivorius.reccomplex.structures.StructurePrepareContext;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.utils.NBTNone;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 13.09.15.
 */
public class WorldScriptCommand implements WorldScript<NBTNone>
{
    public List<Entry> entries = new ArrayList<>();

    public List<WorldScriptCommand.Entry> getEntries()
    {
        return Collections.unmodifiableList(entries);
    }

    public void setEntries(List<WorldScriptCommand.Entry> entries)
    {
        IvCollections.setContentsOfList(this.entries, entries);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound)
    {
        entries.clear();
        NBTTagList entryNBTs = nbtTagCompound.getTagList("commands", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < entryNBTs.tagCount(); i++)
            entries.add(new Entry(entryNBTs.getCompoundTagAt(i)));
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound)
    {
        NBTTagList structureNBTList = new NBTTagList();
        for (Entry entry : entries)
            structureNBTList.appendTag(entry.writeToNBT());
        nbtTagCompound.setTag("commands", structureNBTList);
    }

    @Override
    public NBTNone prepareInstanceData(StructurePrepareContext context, BlockCoord coord, World world)
    {
        return new NBTNone();
    }

    @Override
    public NBTNone loadInstanceData(StructureLoadContext context, NBTBase nbt)
    {
        return new NBTNone();
    }

    @Override
    public void generate(StructureSpawnContext context, NBTNone instanceData, BlockCoord coord)
    {
        // TODO Fix for partial generation (if areas are affected?)
        if (context.includes(coord))
        {
            if (entries.size() > 0)
            {
                WorldScriptCommand.Entry entry = WeightedSelector.selectItem(context.random, entries);
                SpawnCommandLogic logic = new SpawnCommandLogic(context.world, coord, entry.command);

                try
                {
                    logic.executeCommand(context.world);
                }
                catch (Throwable t)
                {
                    RecurrentComplex.logger.error("Error executing command '%s'", entry.command);
                    RecurrentComplex.logger.error("Command execution failed", t);
                }
            }
        }
    }

    public static class Entry implements WeightedSelector.Item
    {
        public String command;
        public Double weight;

        public Entry(Double weight, String command)
        {
            this.command = command;
            this.weight = weight;
        }

        public Entry(NBTTagCompound compound)
        {
            command = compound.getString("command");

            if (compound.hasKey("weight", Constants.NBT.TAG_DOUBLE))
                weight = compound.getDouble("weight");
            else if (compound.hasKey("weight", Constants.NBT.TAG_INT)) // Legacy
                weight = compound.getInteger("weight") * 0.01;
            else
                weight = null;
        }

        public NBTTagCompound writeToNBT()
        {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setString("command", command);
            if (weight != null)
                compound.setDouble("weight", weight);
            return compound;
        }

        @Override
        public double getWeight()
        {
            return weight != null ? weight : 1.0;
        }

        public boolean hasDefaultWeight()
        {
            return weight == null;
        }
    }
}
