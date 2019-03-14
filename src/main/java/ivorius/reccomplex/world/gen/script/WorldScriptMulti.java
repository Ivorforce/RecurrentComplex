/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.script;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.ivtoolkit.tools.NBTTagLists;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.worldscripts.multi.TableDataSourceWorldScriptMulti;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureLoadContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructurePrepareContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;
import ivorius.reccomplex.utils.expression.EnvironmentExpression;
import ivorius.reccomplex.nbt.NBTStorable;
import ivorius.reccomplex.world.gen.feature.structure.generic.transformers.RunTransformer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

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
    public final EnvironmentExpression environmentExpression = new EnvironmentExpression();

    @Override
    public InstanceData prepareInstanceData(StructurePrepareContext context, BlockPos pos)
    {
        InstanceData instanceData = new InstanceData();

        for (WorldScript script : scripts)
            instanceData.addInstanceData(script, script.prepareInstanceData(context, pos));

        instanceData.deactivated = !environmentExpression.test(context.environment);

        return instanceData;
    }

    @Override
    public InstanceData loadInstanceData(StructureLoadContext context, NBTBase nbt)
    {
        return new InstanceData(nbt instanceof NBTTagCompound
                ? (NBTTagCompound) nbt
                : new NBTTagCompound(), scripts, context);
    }

    @Override
    public void generate(StructureSpawnContext context, RunTransformer transformer, InstanceData instanceData, BlockPos pos)
    {
        if (!instanceData.deactivated)
        {
            for (Pair<WorldScript, NBTStorable> paired : instanceData.pairedScripts)
                //noinspection unchecked
                paired.getLeft().generate(context, transformer, paired.getRight(), pos);
        }
    }

    @Override
    public String getDisplayString()
    {
        int amount = scripts.size();
        return amount == 0 ? IvTranslations.get("reccomplex.worldscript.multi.none")
                : IvTranslations.format("reccomplex.worldscript.multi.multiple", amount);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public TableDataSource tableDataSource(BlockPos realWorldPos, TableNavigator navigator, TableDelegate tableDelegate)
    {
        return new TableDataSourceWorldScriptMulti(this, realWorldPos, tableDelegate, navigator);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        scripts.clear();
        scripts.addAll(NBTTagLists.compoundsFrom(compound, "scripts").stream().map(WorldScriptRegistry.INSTANCE::read).filter(Objects::nonNull).collect(Collectors.toList()));

        environmentExpression.setExpression(compound.getString("environmentMatcher"));
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        NBTTagLists.writeTo(compound, "scripts", scripts.stream().map(WorldScriptRegistry.INSTANCE::write).collect(Collectors.toList()));

        compound.setString("environmentMatcher", environmentExpression.getExpression());
    }

    public static class InstanceData implements NBTStorable
    {
        public final List<Pair<WorldScript, NBTStorable>> pairedScripts = new ArrayList<>();

        public boolean deactivated;

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
                    addInstanceData(script, script.loadInstanceData(context, scriptTag.getTag("data")));
            }

            deactivated = compound.getBoolean("deactivated");
        }

        public void addInstanceData(WorldScript script, NBTStorable instanceData)
        {
            pairedScripts.add(Pair.of(script, instanceData));
        }

        @Override
        public NBTBase writeToNBT()
        {
            NBTTagCompound nbt = new NBTTagCompound();

            NBTTagList dataList = new NBTTagList();
            for (int i = 0; i < pairedScripts.size(); i++)
            {
                NBTTagCompound scriptTag = new NBTTagCompound();
                scriptTag.setTag("data", pairedScripts.get(i).getRight().writeToNBT());
                scriptTag.setString("id", WorldScriptRegistry.INSTANCE.type(pairedScripts.get(i).getLeft().getClass()));
                dataList.appendTag(scriptTag);
            }
            nbt.setTag("instanceDates", dataList);

            nbt.setBoolean("deactivated", deactivated);

            return nbt;
        }
    }
}
