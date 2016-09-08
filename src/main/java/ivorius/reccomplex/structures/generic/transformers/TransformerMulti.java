/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.structures.generic.transformers;

import com.google.gson.*;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.ivtoolkit.tools.NBTTagLists;
import ivorius.reccomplex.gui.editstructure.transformers.TableDataSourceBTMulti;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.StructureLoadContext;
import ivorius.reccomplex.structures.StructurePrepareContext;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.structures.generic.matchers.GenerationMatcher;
import ivorius.reccomplex.utils.NBTStorable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
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
    private final GenerationMatcher generationMatcher = new GenerationMatcher("");

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

    public List<Transformer> getTransformers()
    {
        return transformers;
    }

    public GenerationMatcher getGenerationMatcher()
    {
        return generationMatcher;
    }

    @Override
    public String getDisplayString()
    {
        int amount = transformers.size();
        return amount == 0 ? IvTranslations.get("reccomplex.transformer.multi.none")
                : amount == 1 ? transformers.get(0).getDisplayString()
                : IvTranslations.format("reccomplex.transformer.multi.multiple", amount);
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceBTMulti(this, navigator, delegate);
    }

    @Override
    public InstanceData prepareInstanceData(StructurePrepareContext context)
    {
        InstanceData instanceData = new InstanceData();
        transformers.forEach(transformer -> instanceData.pairedTransformers.add(Pair.of(transformer, transformer.prepareInstanceData(context))));
        Biome biome = context.world.getBiome(new BlockPos(context.boundingBox.getCenter()));
        instanceData.activated = generationMatcher.test(new GenerationMatcher.Argument(context, biome));
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
        return instanceData.activated && skips(instanceData.pairedTransformers, state);
    }

    @Override
    public void transform(InstanceData instanceData, Phase phase, StructureSpawnContext context, IvWorldData worldData, List<Pair<Transformer, NBTStorable>> transformers)
    {
        if (instanceData.activated)
            instanceData.pairedTransformers.forEach(pair -> pair.getLeft().transform(pair.getRight(), phase, context, worldData, transformers));
    }

    public static class InstanceData implements NBTStorable
    {
        public static final String KEY_TRANSFORMERS = "transformers";

        public final List<Pair<Transformer, NBTStorable>> pairedTransformers = new ArrayList<>();
        public boolean activated;

        public void readFromNBT(StructureLoadContext context, NBTBase nbt, List<Transformer> transformers)
        {
            NBTTagCompound compound = nbt instanceof NBTTagCompound ? (NBTTagCompound) nbt : new NBTTagCompound();

            NBTTagLists.compoundsFrom(compound, KEY_TRANSFORMERS).forEach(transformerCompound ->
            {
                String transformerID = transformerCompound.getString("id");
                transformers.stream().filter(t -> t.id().equals(transformerID)).findAny().ifPresent(transformer ->
                        pairedTransformers.add(Pair.of(transformer, transformer.loadInstanceData(context, transformerCompound.getTag("data")))));
            });
            activated = compound.getBoolean("activated");
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
            compound.setBoolean("activated", activated);

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
