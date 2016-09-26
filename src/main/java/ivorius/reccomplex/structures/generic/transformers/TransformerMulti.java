/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.structures.generic.transformers;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.ivtoolkit.tools.NBTTagLists;
import ivorius.reccomplex.gui.editstructure.transformers.TableDataSourceBTMulti;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.*;
import ivorius.reccomplex.structures.generic.DimensionGenerationInfo;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import ivorius.reccomplex.structures.generic.matchers.EnvironmentMatcher;
import ivorius.reccomplex.structures.generic.presets.TransfomerPresets;
import ivorius.reccomplex.utils.NBTStorable;
import ivorius.reccomplex.utils.presets.PresettedObject;
import ivorius.reccomplex.utils.presets.PresettedObjects;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
        data.getContents().environmentMatcher.setExpression(expression);
        data.getContents().transformers.addAll(transformers);
    }

    private static Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Data.class, new DataSerializer());
        StructureRegistry.INSTANCE.getTransformerRegistry().constructGson(builder);
        return builder.create();
    }

    public static TransformerMulti fromPreset(String preset)
    {
        TransformerMulti transformer = new TransformerMulti();
        transformer.setID("preset_" + preset);
        transformer.data.setPreset(preset);
        return transformer;
    }

    public static TransformerMulti fuse(List<TransformerMulti> transformers)
    {
        TransformerMulti transformer = new TransformerMulti();
        transformers.forEach(t -> transformer.getTransformers().add(t));
        return transformer;
    }

    public InstanceData fuseDatas(List<InstanceData> instanceDatas)
    {
        List<Transformer> transformers = getTransformers();
        InstanceData instanceData = new InstanceData();
        for (int i = 0; i < instanceDatas.size(); i++)
            instanceData.pairedTransformers.add(Pair.of(transformers.get(i), instanceDatas.get(i)));
        return instanceData;
    }

    public void configureInstanceData(InstanceData instanceData, StructurePrepareContext context, IvWorldData worldData)
    {
        configureInstanceData(instanceData, context, worldData, this, instanceData);
    }

    public boolean mayGenerate(InstanceData instanceData, StructureSpawnContext context, IvWorldData worldData)
    {
        return mayGenerate(instanceData, context, worldData, this, instanceData);
    }

    public void transform(InstanceData instanceData, Phase phase, StructureSpawnContext context, IvWorldData worldData)
    {
        transform(instanceData, phase, context, worldData, this, instanceData);
    }

    public List<Transformer> getTransformers()
    {
        return data.getContents().transformers;
    }

    public EnvironmentMatcher getEnvironmentMatcher()
    {
        return data.getContents().environmentMatcher;
    }

    public PresettedObject<Data> getData()
    {
        return data;
    }

    @Override
    public String getDisplayString()
    {
        int amount = getTransformers().size();
        return amount == 0 ? IvTranslations.get("reccomplex.transformer.multi.none")
                : IvTranslations.format("reccomplex.transformer.multi.multiple", amount);
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceBTMulti(this, navigator, delegate);
    }

    @Override
    public InstanceData prepareInstanceData(StructurePrepareContext context, IvWorldData worldData)
    {
        InstanceData instanceData = new InstanceData();
        getTransformers().forEach(t -> instanceData.pairedTransformers.add(Pair.of(t, t.prepareInstanceData(context, worldData))));
        instanceData.deactivated = !getEnvironmentMatcher().test(context.environment);
        return instanceData;
    }

    @Override
    public void configureInstanceData(InstanceData instanceData, StructurePrepareContext context, IvWorldData worldData, TransformerMulti transformer, InstanceData transformerID)
    {
        instanceData.pairedTransformers.forEach(pair -> pair.getLeft().configureInstanceData(pair.getRight(), context, worldData, transformer, transformerID));
    }

    @Override
    public InstanceData loadInstanceData(StructureLoadContext context, NBTBase nbt)
    {
        InstanceData instanceData = new InstanceData();
        instanceData.readFromNBT(context, nbt, getTransformers());
        return instanceData;
    }

    @Override
    public boolean mayGenerate(InstanceData instanceData, StructureSpawnContext context, IvWorldData worldData, TransformerMulti transformer, InstanceData transformerID)
    {
        return instanceData.deactivated || instanceData.pairedTransformers.stream()
                .allMatch(input -> input.getLeft().mayGenerate(input.getRight(), context, worldData, transformer, transformerID));
    }

    @Override
    public boolean skipGeneration(InstanceData instanceData, Environment environment, BlockPos pos, IBlockState state)
    {
        return !instanceData.deactivated && instanceData.pairedTransformers.stream()
                .anyMatch(input -> input.getLeft().skipGeneration(input.getRight(), environment, pos, state));
    }

    @Override
    public void transform(InstanceData instanceData, Phase phase, StructureSpawnContext context, IvWorldData worldData, TransformerMulti transformer, InstanceData transformerID)
    {
        if (!instanceData.deactivated)
            instanceData.pairedTransformers.forEach(pair -> pair.getLeft().transform(pair.getRight(), phase, context, worldData, transformer, transformerID));
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
        public final EnvironmentMatcher environmentMatcher = new EnvironmentMatcher("");
    }

    public static class DataSerializer implements JsonDeserializer<Data>, JsonSerializer<Data>
    {
        @Override
        public Data deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.asJsonObject(json, "transformerMultiData");

            Data data = new Data();

            data.environmentMatcher.setExpression(JsonUtils.getString(jsonObject, "environmentMatcher", ""));
            Transformer[] transformers = gson.fromJson(jsonObject.get("transformers"), Transformer[].class);
            if (transformers != null)
                Collections.addAll(data.transformers, transformers);

            return data;
        }

        @Override
        public JsonElement serialize(Data src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("environmentMatcher", src.environmentMatcher.getExpression());
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

            transformer.setID(JsonUtils.getString(jsonObject, "id", null));
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
