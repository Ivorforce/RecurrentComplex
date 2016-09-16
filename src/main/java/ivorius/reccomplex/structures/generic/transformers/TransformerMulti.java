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
import ivorius.reccomplex.structures.Environment;
import ivorius.reccomplex.structures.StructureLoadContext;
import ivorius.reccomplex.structures.StructurePrepareContext;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.structures.generic.matchers.EnvironmentMatcher;
import ivorius.reccomplex.utils.NBTStorable;
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
    private final List<Transformer> transformers = new ArrayList<>();
    private final EnvironmentMatcher environmentMatcher;

    public TransformerMulti()
    {
        this(null, "");
    }

    public TransformerMulti(@Nullable String id, String expression)
    {
        super(id != null ? id : randomID(TransformerMulti.class));
        this.environmentMatcher = new EnvironmentMatcher(expression);
    }

    public TransformerMulti(@Nullable String id, String expression, Collection<Transformer> transformers)
    {
        this(id, expression);
        this.transformers.addAll(transformers);
    }

    public List<Transformer> getTransformers()
    {
        return transformers;
    }

    public EnvironmentMatcher getEnvironmentMatcher()
    {
        return environmentMatcher;
    }

    @Override
    public String getDisplayString()
    {
        int amount = transformers.size();
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
        transformers.forEach(t -> instanceData.pairedTransformers.add(Pair.of(t, t.prepareInstanceData(context, worldData))));
        instanceData.deactivated = !environmentMatcher.test(context.environment);
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
        instanceData.readFromNBT(context, nbt, transformers);
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

    public static class Serializer implements JsonDeserializer<TransformerMulti>, JsonSerializer<TransformerMulti>
    {
        @Override
        public TransformerMulti deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.asJsonObject(json, "transformerNatural");

            String id = JsonUtils.getString(jsonObject, "id", null);
            String expression = JsonUtils.getString(jsonObject, "environmentMatcher", "");
            TransformerMulti transformer = new TransformerMulti(id, expression);

            Collections.addAll(transformer.transformers, context.<Transformer[]>deserialize(jsonObject.get("transformers"), Transformer[].class));
            return transformer;
        }

        @Override
        public JsonElement serialize(TransformerMulti src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", src.id());
            jsonObject.addProperty("environmentMatcher", src.environmentMatcher.getExpression());

            jsonObject.add("transformers", context.serialize(src.transformers));

            return jsonObject;
        }
    }
}
