/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.structures.generic.transformers;

import com.google.gson.*;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.ivtoolkit.tools.NBTTagLists;
import ivorius.reccomplex.gui.editstructure.TableDataSourceTransformerList;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.StructureLoadContext;
import ivorius.reccomplex.structures.StructurePrepareContext;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.utils.NBTStorable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lukas on 06.09.16.
 */
public class TransformerMulti extends Transformer<TransformerMulti.InstanceData>
{
    private final List<Transformer> transformers = new ArrayList<>();

    public TransformerMulti()
    {
        this(randomID(TransformerMulti.class));
    }

    public TransformerMulti(@Nonnull String id)
    {
        super(id);
    }

    public static boolean skips(List<Pair<Transformer, NBTStorable>> transformers, final IBlockState state)
    {
        return transformers.stream().anyMatch(input -> input.getLeft().skipGeneration(input.getRight(), state));
    }

    @Override
    public String getDisplayString()
    {
        return IvTranslations.get("reccomplex.transformer.multi");
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceTransformerList(transformers, delegate, navigator);
    }

    @Override
    public InstanceData prepareInstanceData(StructurePrepareContext context)
    {
        InstanceData instanceData = new InstanceData();
        transformers.forEach(transformer -> instanceData.pairedTransformers.add(Pair.of(transformer, transformer.prepareInstanceData(context))));
        return instanceData;
    }

    @Override
    public InstanceData loadInstanceData(StructureLoadContext context, NBTBase nbt)
    {
        InstanceData instanceData = new InstanceData();
        instanceData.readFromNBT(context, nbt, transformers);
        return instanceData;
    }

    @Override
    public boolean skipGeneration(InstanceData instanceData, IBlockState state)
    {
        return skips(instanceData.pairedTransformers, state);
    }

    @Override
    public boolean generatesInPhase(InstanceData instanceData, Phase phase)
    {
        return instanceData.pairedTransformers.stream().anyMatch(pair -> pair.getLeft().generatesInPhase(pair.getRight(), phase));
    }

    @Override
    public void transform(InstanceData instanceData, Phase phase, StructureSpawnContext context, IvWorldData worldData, List transformers)
    {
        instanceData.pairedTransformers.forEach(pair -> pair.getLeft().transform(pair.getRight(), phase, context, worldData, transformers));
    }

    public static class InstanceData implements NBTStorable
    {
        public static final String KEY_TRANSFORMERS = "transformers";

        public final List<Pair<Transformer, NBTStorable>> pairedTransformers = new ArrayList<>();

        public void readFromNBT(StructureLoadContext context, NBTBase nbt, List<Transformer> transformers)
        {
            NBTTagCompound compound = nbt instanceof NBTTagCompound ? (NBTTagCompound) nbt : new NBTTagCompound();

            NBTTagLists.compoundsFrom(compound, KEY_TRANSFORMERS).forEach(transformerCompound ->
            {
                String transformerID = transformerCompound.getString("id");
                transformers.stream().filter(t -> t.id().equals(transformerID)).findAny().ifPresent(transformer ->
                        pairedTransformers.add(Pair.of(transformer, transformer.loadInstanceData(context, transformerCompound.getTag("data")))));
            });
        }

        @Override
        public NBTBase writeToNBT()
        {
            NBTTagCompound compound = new NBTTagCompound();

            NBTTagLists.writeTo(compound, KEY_TRANSFORMERS, pairedTransformers.stream().map(pair -> {
                NBTTagCompound transformerCompound = new NBTTagCompound();
                transformerCompound.setTag("data", pair.getRight().writeToNBT());
                transformerCompound.setString("id", pair.getLeft().id());
                return transformerCompound;
            }).collect(Collectors.toList()));

            return compound;
        }
    }

    public static class Serializer implements JsonDeserializer<TransformerMulti>, JsonSerializer<TransformerMulti>
    {
        @Override
        public TransformerMulti deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(json, "transformerNatural");

            String id = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "id", randomID(TransformerNatural.class));
            TransformerMulti transformer = new TransformerMulti(id);

            Collections.addAll(transformer.transformers, context.<Transformer[]>deserialize(jsonObject.get("transformers"), Transformer[].class));
            return transformer;
        }

        @Override
        public JsonElement serialize(TransformerMulti src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", src.id());

            jsonObject.add("transformers", context.serialize(src.transformers));

            return jsonObject;
        }
    }
}
