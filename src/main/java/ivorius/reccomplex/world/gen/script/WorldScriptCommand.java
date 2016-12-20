/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.script;

import io.netty.buffer.ByteBuf;
import ivorius.ivtoolkit.random.WeightedSelector;
import ivorius.ivtoolkit.tools.IvCollections;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.block.SpawnCommandLogic;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.worldscripts.command.TableDataSourceWorldScriptCommand;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureLoadContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructurePrepareContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;
import ivorius.reccomplex.utils.NBTNone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
    public NBTNone prepareInstanceData(StructurePrepareContext context, BlockPos pos)
    {
        return new NBTNone();
    }

    @Override
    public NBTNone loadInstanceData(StructureLoadContext context, NBTBase nbt)
    {
        return new NBTNone();
    }

    @Override
    public void generate(StructureSpawnContext context, NBTNone instanceData, BlockPos pos)
    {
        // TODO Fix for partial generation (if areas are affected?)
        if (context.includes(pos))
        {
            if (entries.size() > 0)
            {
                WorldScriptCommand.Entry entry = WeightedSelector.selectItem(context.random, entries);
                SpawnCommandLogic logic = new SpawnCommandLogic()
                {
                    @Override
                    public BlockPos getPosition()
                    {
                        return pos;
                    }

                    @Override
                    public Vec3d getPositionVector()
                    {
                        return new Vec3d((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D);
                    }

                    @Override
                    public World getEntityWorld()
                    {
                        return context.environment.world;
                    }

                    @Override
                    public void updateCommand()
                    {
                        IBlockState iblockstate = context.environment.world.getBlockState(pos);
                        context.environment.world.notifyBlockUpdate(pos, iblockstate, iblockstate, 3);
                    }

                    @Override
                    @SideOnly(Side.CLIENT)
                    public int getCommandBlockType()
                    {
                        return 0;
                    }

                    @Override
                    @SideOnly(Side.CLIENT)
                    public void fillInInfo(ByteBuf buf)
                    {
                        buf.writeInt(pos.getX());
                        buf.writeInt(pos.getY());
                        buf.writeInt(pos.getZ());
                    }

                    @Override
                    public Entity getCommandSenderEntity()
                    {
                        return null;
                    }

                    @Override
                    public MinecraftServer getServer()
                    {
                        return context.environment.world.getMinecraftServer();
                    }
                };
                logic.setCommand(entry.command);

                try
                {
                    logic.trigger(context.environment.world);
                }
                catch (Throwable t)
                {
                    RecurrentComplex.logger.error("Error executing command '%s'", entry.command);
                    RecurrentComplex.logger.error("Command execution failed", t);
                }
            }
        }
    }

    @Override
    public String getDisplayString()
    {
        return IvTranslations.get("reccomplex.worldscript.command");
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate tableDelegate)
    {
        return new TableDataSourceWorldScriptCommand(this, tableDelegate, navigator);
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
