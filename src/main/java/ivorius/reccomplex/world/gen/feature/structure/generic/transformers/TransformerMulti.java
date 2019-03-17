/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.transformers;

import com.google.gson.*;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.ivtoolkit.tools.NBTTagLists;
import ivorius.reccomplex.gui.editstructure.transformers.TableDataSourceBTMulti;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.world.gen.feature.structure.*;
import ivorius.reccomplex.utils.expression.EnvironmentExpression;
import ivorius.reccomplex.world.gen.feature.structure.context.*;
import ivorius.reccomplex.world.gen.feature.structure.generic.presets.TransfomerPresets;
import ivorius.reccomplex.nbt.NBTStorable;
import ivorius.reccomplex.utils.presets.PresettedObject;
import ivorius.reccomplex.utils.presets.PresettedObjects;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lukas on 06.09.16.
 */
public class TransformerMulti extends Transformer<TransformerMulti.InstanceData>
{
    public static Gson gson = createGson();

    protected final PresettedObject<Data> data = new PresettedObject<>(TransfomerPresets.instance(), null);

    public TransformerMulti()
    {
        this(null, "");
    }

    public TransformerMulti(@Nullable String id, String expression)
    {
        this(id, expression, Collections.emptyList());
    }

    public TransformerMulti(@Nullable String id, String expression, Collection<Transformer> transformers)
    {
        super(id != null ? id : randomID(TransformerMulti.class));

        data.setContents(new Data());
        data.getContents().environmentExpression.setExpression(expression);
        data.getContents().transformers.addAll(transformers);
    }

    private static Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Data.class, new DataSerializer());
        StructureRegistry.TRANSFORMERS.constructGson(builder);
        return builder.create();
    }

    public static TransformerMulti fromPreset(String preset)
    {
        TransformerMulti transformer = new TransformerMulti();
        transformer.setID("preset_" + preset);
        transformer.data.setPreset(preset);
        return transformer;
    }

    public static TransformerMulti fuse(List<Transformer> transformers)
    {
        TransformerMulti transformer = new TransformerMulti("fused", "");
        transformers.forEach(t -> transformer.getTransformers().add(t));
        return transformer;
    }

    public boolean isEmpty(InstanceData instanceData)
    {
        return instanceData.pairedTransformers.isEmpty();
    }

    public InstanceData fuseDatas(List<NBTStorable> instanceDatas)
    {
        List<Transformer> transformers = getTransformers();
        InstanceData instanceData = new InstanceData();
        for (int i = 0; i < instanceDatas.size(); i++)
            instanceData.pairedTransformers.add(Pair.of(transformers.get(i), instanceDatas.get(i)));
        return instanceData;
    }

    public List<Transformer> getTransformers()
    {
        return data.getContents().transformers;
    }

    public EnvironmentExpression getEnvironmentMatcher()
    {
        return data.getContents().environmentExpression;
    }

    public PresettedObject<Data> getData()
    {
        return data;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public String getDisplayString()
    {
        if (data.getPreset() != null)
            return data.getPresetRegistry().metadata(data.getPreset())
                .map(m -> TextFormatting.GREEN + m.title).orElse(TextFormatting.GOLD + data.getPreset());

        int amount = getTransformers().size();
        return amount == 0 ? IvTranslations.get("reccomplex.transformer.multi.none")
                : IvTranslations.format("reccomplex.transformer.multi.multiple", amount);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceBTMulti(this, navigator, delegate);
    }

    @Override
    public InstanceData prepareInstanceData(StructurePrepareContext context, IvWorldData worldData)
    {
        InstanceData instanceData = new InstanceData();
        instanceData.deactivated = !getEnvironmentMatcher().test(context.environment);
        if (!instanceData.deactivated)
            getTransformers().forEach(t -> instanceData.pairedTransformers.add(Pair.of(t, t.prepareInstanceData(context, worldData))));
        return instanceData;
    }

    @Override
    public void configureInstanceData(InstanceData instanceData, StructurePrepareContext context, IvWorldData worldData, RunTransformer transformer)
    {
        if (!instanceData.deactivated)
            //noinspection unchecked
            instanceData.pairedTransformers.forEach(pair -> pair.getLeft().configureInstanceData(pair.getRight(), context, worldData, transformer));
    }

    @Override
    public InstanceData loadInstanceData(StructureLoadContext context, NBTBase nbt)
    {
        InstanceData instanceData = new InstanceData();
        instanceData.readFromNBT(context, nbt, getTransformers());
        return instanceData;
    }

    @Override
    public boolean mayGenerate(InstanceData instanceData, StructurePrepareContext context, IvWorldData worldData)
    {
        //noinspection unchecked
        return instanceData.deactivated || instanceData.pairedTransformers.stream()
                .allMatch(input -> input.getLeft().mayGenerate(input.getRight(), context, worldData));
    }

    @Override
    public boolean skipGeneration(InstanceData instanceData, StructureLiveContext context, BlockPos pos, IBlockState state, IvWorldData worldData, BlockPos sourcePos)
    {
        //noinspection unchecked
        return !instanceData.deactivated && instanceData.pairedTransformers.stream()
                .anyMatch(input -> input.getLeft().skipGeneration(input.getRight(), context, pos, state, worldData, sourcePos));
    }

    @Override
    public void transform(InstanceData instanceData, Phase phase, StructureSpawnContext context, IvWorldData worldData, RunTransformer transformer)
    {
        if (!instanceData.deactivated)
            //noinspection unchecked
            instanceData.pairedTransformers.forEach(pair -> pair.getLeft().transform(pair.getRight(), phase, context, worldData, transformer));
    }

    public static class InstanceData implements NBTStorable
    {
        public static final String KEY_TRANSFORMERS = "transformers";

        public final List<Pair<Transformer, NBTStorable>> pairedTransformers = new ArrayList<>();
        public boolean deactivated;

        public void readFromNBT(StructureLoadContext context, NBTBase nbt, List<Transformer> transformers)
        {
            NBTTagCompound compound = nbt instanceof NBTTagCompound ? (NBTTagCompound) nbt : new NBTTagCompound();

            NBTTagLists.compoundsFrom(compound, KEY_TRANSFORMERS).forEach(transformerCompound ->
            {
                String transformerID = transformerCompound.getString("id");
                transformers.stream().filter(t -> t.id().equals(transformerID)).findAny().ifPresent(transformer ->
                        pairedTransformers.add(Pair.of(transformer, transformer.loadInstanceData(context, transformerCompound.getTag("data")))));
            });
            deactivated = compound.getBoolean("deactivated");
        }

        @Override
        public NBTBase writeToNBT()
        {
            NBTTagCompound compound = new NBTTagCompound();

            NBTTagLists.writeTo(compound, KEY_TRANSFORMERS, pairedTransformers.stream().map(pair ->
            {
                NBTTagCompound transformerCompound = new NBTTagCompound();
                transformerCompound.setTag("data", pair.getRight().writeToNBT());
                transformerCompound.setString("id", pair.getLeft().id());
                return transformerCompound;
            }).collect(Collectors.toList()));
            compound.setBoolean("deactivated", deactivated);

            return compound;
        }
    }

    public static class Data
    {
        public final List<Transformer> transformers = new ArrayList<>();
        public final EnvironmentExpression environmentExpression = new EnvironmentExpression();
    }

    public static class DataSerializer implements JsonDeserializer<Data>, JsonSerializer<Data>
    {
        @Override
        public Data deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.asJsonObject(json, "transformerMultiData");

            Data data = new Data();

            data.environmentExpression.setExpression(JsonUtils.getString(jsonObject, "environmentMatcher", ""));

            Transformer.idRandomizers.push(new Random(0xDEADBEEF)); // Legacy for missing IDs
            Transformer[] transformers = gson.fromJson(jsonObject.get("transformers"), Transformer[].class);
            if (transformers != null)
                Collections.addAll(data.transformers, transformers);
            Transformer.idRandomizers.pop();

            return data;
        }

        @Override
        public JsonElement serialize(Data src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("environmentMatcher", src.environmentExpression.getExpression());
            jsonObject.add("transformers", gson.toJsonTree(src.transformers));

            return jsonObject;
        }
    }

    public static class Serializer implements JsonDeserializer<TransformerMulti>, JsonSerializer<TransformerMulti>
    {
        @Override
        public TransformerMulti deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.asJsonObject(json, "transformerMulti");

            TransformerMulti transformer = new TransformerMulti();

            transformer.setID(readID(jsonObject));
            if (!PresettedObjects.read(jsonObject, gson, transformer.data, "dataPreset", "data", Data.class)
                    && jsonObject.has("environmentMatcher") && jsonObject.has("transformers"))
            {
                // Legacy
                transformer.getData().setContents(gson.fromJson(jsonObject, Data.class));
            }

            return transformer;
        }

        @Override
        public JsonElement serialize(TransformerMulti src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", src.id());
            PresettedObjects.write(jsonObject, gson, src.data, "dataPreset", "data");

            return jsonObject;
        }
    }
}
