/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.scripts.world;

import com.google.common.collect.Lists;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.tools.NBTCompoundObjects;
import ivorius.ivtoolkit.tools.NBTTagLists;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.worldscripts.multi.TableDataSourceWorldScriptList;
import ivorius.reccomplex.structures.StructureLoadContext;
import ivorius.reccomplex.structures.StructurePrepareContext;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.utils.IvTranslations;
import ivorius.reccomplex.utils.NBTStorable;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by lukas on 14.09.15.
 */
public class WorldScriptMulti implements WorldScript<WorldScriptMulti.InstanceData>
{
    public final List<WorldScript> scripts = new ArrayList<>();

    @Override
    public InstanceData prepareInstanceData(StructurePrepareContext context, BlockCoord coord, World world)
    {
        InstanceData instanceData = new InstanceData();

        for (WorldScript script : scripts)
            instanceData.addInstanceData(script.prepareInstanceData(context, coord, world), WorldScriptRegistry.INSTANCE.type(script.getClass()));

        return instanceData;
    }

    @Override
    public InstanceData loadInstanceData(StructureLoadContext context, NBTBase nbt)
    {
        return new InstanceData(nbt instanceof NBTTagCompound ? (NBTTagCompound) nbt : new NBTTagCompound(), scripts, context);
    }

    @Override
    public void generate(StructureSpawnContext context, InstanceData instanceData, BlockCoord coord)
    {
        for (int i = 0; i < scripts.size(); i++)
        {
            NBTStorable scriptData = instanceData.instanceDates.get(i);
            if (scriptData != null)
                scripts.get(i).generate(context, scriptData, coord);
        }
    }

    @Override
    public String getDisplayString()
    {
        return IvTranslations.get("reccomplex.worldscript.multi");
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate tableDelegate)
    {
        return new TableDataSourceWorldScriptList(scripts, tableDelegate, navigator);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        scripts.clear();
        scripts.addAll(NBTTagLists.compoundsFrom(compound, "scripts").stream().map(WorldScriptRegistry.INSTANCE::read).filter(Objects::nonNull).collect(Collectors.toList()));
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        NBTTagLists.writeCompoundsTo(compound, "scripts", scripts.stream().map(WorldScriptRegistry.INSTANCE::write).collect(Collectors.toList()));
    }

    public static class InstanceData implements NBTStorable
    {
        public final List<NBTStorable> instanceDates = new ArrayList<>();
        public final List<String> ids = new ArrayList<>();

        public InstanceData()
        {
        }

        public InstanceData(NBTTagCompound compound, List<WorldScript> expectedScripts, StructureLoadContext context)
        {
            List<NBTTagCompound> compoundsFrom = NBTTagLists.compoundsFrom(compound, "instanceDates");
            for (int i = 0; i < compoundsFrom.size(); i++)
            {
                NBTTagCompound scriptTag = compoundsFrom.get(i);

                WorldScript script = expectedScripts.get(i);
                if (WorldScriptRegistry.INSTANCE.type(script.getClass()).equals(scriptTag.getString("id")))
                    instanceDates.add(script.loadInstanceData(context, compound.getTag("data")));
                else
                    instanceDates.add(null);
            }
            for (int i = compoundsFrom.size(); i < expectedScripts.size(); i++)
                instanceDates.add(null);
        }

        public void addInstanceData(NBTStorable instanceData, String id)
        {
            instanceDates.add(instanceData);
            ids.add(id);
        }

        @Override
        public NBTBase writeToNBT()
        {
            NBTTagCompound nbt = new NBTTagCompound();

            NBTTagList dataList = new NBTTagList();
            for (int i = 0; i < instanceDates.size(); i++)
            {
                NBTTagCompound scriptTag = new NBTTagCompound();
                scriptTag.setTag("data", instanceDates.get(i).writeToNBT());
                scriptTag.setString("id", ids.get(i));
                dataList.appendTag(scriptTag);
            }
            nbt.setTag("instanceDates", dataList);

            return null;
        }
    }
}
