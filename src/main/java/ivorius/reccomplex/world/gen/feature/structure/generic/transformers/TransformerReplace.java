/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.transformers;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import ivorius.ivtoolkit.blocks.BlockStates;
import ivorius.ivtoolkit.random.WeightedSelector;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.editstructure.transformers.TableDataSourceBTReplace;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.utils.NBTNone;
import ivorius.reccomplex.utils.algebra.ExpressionCache;
import ivorius.reccomplex.utils.expression.BlockMatcher;
import ivorius.reccomplex.utils.presets.PresettedList;
import ivorius.reccomplex.utils.presets.PresettedObjects;
import ivorius.reccomplex.world.gen.feature.structure.Environment;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureLoadContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructurePrepareContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructureInfo;
import ivorius.reccomplex.world.gen.feature.structure.generic.WeightedBlockState;
import ivorius.reccomplex.world.gen.feature.structure.generic.presets.WeightedBlockStatePresets;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Created by lukas on 25.05.14.
 */
public class TransformerReplace extends TransformerSingleBlock<NBTNone>
{
    public final PresettedList<WeightedBlockState> destination = new PresettedList<>(WeightedBlockStatePresets.instance(), null);
    public BlockMatcher sourceMatcher;

    public TransformerReplace()
    {
        this(null, BlockMatcher.of(RecurrentComplex.specialRegistry, Blocks.WOOL));
        destination.setPreset("air");
    }

    public TransformerReplace(@Nullable String id, String sourceExpression)
    {
        super(id != null ? id : randomID(TransformerReplace.class));
        this.sourceMatcher = ExpressionCache.of(new BlockMatcher(RecurrentComplex.specialRegistry), sourceExpression);
    }

    public static void setBlock(StructureSpawnContext context, BlockPos pos, WeightedBlockState entry, Supplier<NBTTagCompound> tileEntity)
    {
        if (entry.state != null && RecurrentComplex.specialRegistry.isSafe(entry.state.getBlock()))
        {
            GenericStructureInfo.setBlock(context, pos, entry.state, tileEntity);
        }
    }

    public TransformerReplace replaceWith(WeightedBlockState... states)
    {
        destination.setContents(Arrays.asList(states));
        return this;
    }

    @Override
    public boolean matches(Environment environment, NBTNone instanceData, IBlockState state)
    {
        return sourceMatcher.test(state);
    }

    @Override
    public void transformBlock(NBTNone instanceData, Phase phase, StructureSpawnContext context, BlockPos coord, IBlockState sourceState)
    {
        WeightedBlockState blockState;
        if (destination.getContents().size() > 0)
            blockState = WeightedSelector.selectItem(context.random, destination.getContents());
        else
            blockState = new WeightedBlockState(null, null, null);

        setBlock(context, coord, blockState, () -> blockState.tileEntityInfo);
    }

    @Override
    public NBTNone prepareInstanceData(StructurePrepareContext context, IvWorldData worldData)
    {
        return new NBTNone();
    }

    @Override
    public NBTNone loadInstanceData(StructureLoadContext context, NBTBase nbt)
    {
        return new NBTNone();
    }

    @Override
    public String getDisplayString()
    {
        return "Replace: " + sourceMatcher.getDisplayString(null);
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceBTReplace(this, navigator, delegate);
    }

    @Override
    public boolean generatesInPhase(NBTNone instanceData, Phase phase)
    {
        return phase == Phase.BEFORE;
    }

    public static class Serializer implements JsonDeserializer<TransformerReplace>, JsonSerializer<TransformerReplace>
    {
        private MCRegistry registry;
        private Gson gson;

        public Serializer(MCRegistry registry)
        {
            this.registry = registry;
            gson = new GsonBuilder().registerTypeAdapter(WeightedBlockState.class, new WeightedBlockState.Serializer(registry)).create();
        }

        public static String readLegacyMatcher(JsonObject jsonObject, String blockKey, String metadataKey)
        {
            if (jsonObject.has(blockKey))
            {
                String sourceBlock = JsonUtils.getString(jsonObject, blockKey);
                int sourceMeta = JsonUtils.getInt(jsonObject, metadataKey, -1);
                return sourceMeta >= 0 ? String.format("%s & %s%d", sourceBlock, BlockMatcher.METADATA_PREFIX, sourceMeta) : sourceBlock;
            }

            return null;
        }

        @Override
        public TransformerReplace deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonObject = JsonUtils.asJsonObject(jsonElement, "transformerReplace");

            String id = JsonUtils.getString(jsonObject, "id", null);

            String expression = readLegacyMatcher(jsonObject, "source", "sourceMetadata"); // Legacy
            if (expression == null)
                expression = JsonUtils.getString(jsonObject, "sourceExpression", "");

            TransformerReplace transformer = new TransformerReplace(id, expression);

            PresettedObjects.read(jsonObject, gson, transformer.destination, "destinationPreset", "destination", new TypeToken<ArrayList<WeightedBlockState>>() {}.getType());

            if (jsonObject.has("dest"))
            {
                // Legacy
                Block dest = registry.blockFromID(new ResourceLocation(JsonUtils.getString(jsonObject, "dest")));
                byte[] destMeta = context.deserialize(jsonObject.get("destMetadata"), byte[].class);

                transformer.destination.setToCustom();
                for (byte b : destMeta)
                    transformer.destination.getContents().add(new WeightedBlockState(null, BlockStates.fromMetadata(dest, b), null));
            }

            return transformer;
        }

        @Override
        public JsonElement serialize(TransformerReplace transformer, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", transformer.id());
            jsonObject.addProperty("sourceExpression", transformer.sourceMatcher.getExpression());

            PresettedObjects.write(jsonObject, gson, transformer.destination, "destinationPreset", "destination");

            return jsonObject;
        }
    }
}
