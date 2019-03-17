/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.transformers;

import com.google.gson.*;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.ivtoolkit.tools.NBTCompoundObjects;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.editstructure.transformers.TableDataSourceBTWorldScript;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.json.NBTToJson;
import ivorius.reccomplex.utils.algebra.ExpressionCache;
import ivorius.reccomplex.world.gen.feature.StructureGenerator;
import ivorius.reccomplex.world.gen.script.WorldScriptMulti;
import ivorius.reccomplex.world.gen.feature.structure.Environment;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureLoadContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructurePrepareContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;
import ivorius.reccomplex.world.gen.feature.structure.generic.WeightedBlockState;
import ivorius.reccomplex.utils.expression.BlockExpression;
import ivorius.reccomplex.nbt.NBTStorable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

/**
 * Created by lukas on 25.05.14.
 */
public class TransformerWorldScript extends TransformerSingleBlock<TransformerWorldScript.InstanceData>
{
    public WorldScriptMulti script;
    public BlockExpression sourceMatcher;

    public TransformerWorldScript()
    {
        this(null, new WorldScriptMulti(), BlockExpression.of(RecurrentComplex.specialRegistry, Blocks.WOOL));
    }

    public TransformerWorldScript(@Nullable String id, WorldScriptMulti script, String sourceExpression)
    {
        super(id != null ? id : randomID(TransformerWorldScript.class));
        this.script = script;
        this.sourceMatcher = ExpressionCache.of(new BlockExpression(RecurrentComplex.specialRegistry), sourceExpression);
    }

    @Override
    public boolean matches(Environment environment, InstanceData instanceData, BlockPos sourcePos, IBlockState state)
    {
        return sourceMatcher.test(state);
    }

    @Override
    public void transformBlock(InstanceData instanceData, Phase phase, StructureSpawnContext context, RunTransformer transformer, int[] areaSize, BlockPos coord, IBlockState sourceState)
    {
        StructureGenerator<NBTStorable> generator = new StructureGenerator<>().asChild(context)
                .maturity(StructureSpawnContext.GenerateMaturity.FIRST);

        WorldScriptMulti.InstanceData scriptInstanceData = script.prepareInstanceData(generator.prepare().get(), coord);
        script.generate(generator.spawn().get(), transformer, scriptInstanceData, coord);
    }

    @Override
    public InstanceData prepareInstanceData(StructurePrepareContext context, IvWorldData worldData)
    {
        return new InstanceData();
    }

    @Override
    public InstanceData loadInstanceData(StructureLoadContext context, NBTBase nbt)
    {
        InstanceData instanceData = new InstanceData();
        instanceData.readFromNBT(this, context, nbt);
        return instanceData;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public String getDisplayString()
    {
        return script.getDisplayString();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceBTWorldScript(this, navigator, delegate);
    }

    @Override
    public boolean generatesInPhase(InstanceData instanceData, Phase phase)
    {
        return phase == Phase.BEFORE;
    }

    public static class InstanceData implements NBTStorable
    {
        @Override
        public NBTBase writeToNBT()
        {
            NBTTagCompound compound = new NBTTagCompound();
            return compound;
        }

        public void readFromNBT(TransformerWorldScript transformer, StructureLoadContext context, NBTBase nbt)
        {
        }
    }

    public static class Serializer implements JsonDeserializer<TransformerWorldScript>, JsonSerializer<TransformerWorldScript>
    {
        private MCRegistry registry;
        private Gson gson;

        public Serializer(MCRegistry registry)
        {
            this.registry = registry;
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(WeightedBlockState.class, new WeightedBlockState.Serializer(registry));
            NBTToJson.registerSafeNBTSerializer(builder);
            gson = builder.create();
        }

        @Override
        public TransformerWorldScript deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonObject = JsonUtils.asJsonObject(jsonElement, "transformerReplace");

            String id = readID(jsonObject);

            String expression = JsonUtils.getString(jsonObject, "sourceExpression", "");
            WorldScriptMulti script = NBTCompoundObjects.read(gson.fromJson(JsonUtils.getJsonObject(jsonObject, "script", new JsonObject()), NBTTagCompound.class), WorldScriptMulti::new);

            return new TransformerWorldScript(id, script, expression);
        }

        @Override
        public JsonElement serialize(TransformerWorldScript transformer, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", transformer.id());
            jsonObject.addProperty("sourceExpression", transformer.sourceMatcher.getExpression());

            jsonObject.add("script", gson.toJsonTree(NBTCompoundObjects.write(transformer.script)));

            return jsonObject;
        }
    }
}
