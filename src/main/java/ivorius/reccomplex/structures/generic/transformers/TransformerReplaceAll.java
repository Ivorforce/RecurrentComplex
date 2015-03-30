/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.transformers;

import com.google.gson.*;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.gui.editstructure.transformers.TableDataSourceBTReplaceAll;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.StructureLoadContext;
import ivorius.reccomplex.structures.StructurePrepareContext;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.structures.generic.WeightedBlockState;
import ivorius.reccomplex.structures.generic.matchers.BlockMatcher;
import ivorius.reccomplex.structures.generic.presets.WeightedBlockStatePresets;
import ivorius.reccomplex.utils.NBTStorable;
import ivorius.reccomplex.utils.PresettedList;
import ivorius.ivtoolkit.random.WeightedSelector;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class TransformerReplaceAll implements Transformer<TransformerReplaceAll.InstanceData>
{
    public BlockMatcher sourceMatcher;

    public final PresettedList<WeightedBlockState> destination = new PresettedList<>(WeightedBlockStatePresets.instance(), null);

    public TransformerReplaceAll()
    {
        this(BlockMatcher.of(Blocks.wool, new IntegerRange(0, 15)));
        destination.setToDefault();
    }

    public TransformerReplaceAll(String sourceExpression)
    {
        this.sourceMatcher = new BlockMatcher(sourceExpression);
    }

    public TransformerReplaceAll replaceWith(WeightedBlockState... states)
    {
        destination.setContents(Arrays.asList(states));
        return this;
    }

    @Override
    public boolean skipGeneration(InstanceData instanceData, Block block, int metadata)
    {
        return matches(instanceData, block, metadata);
    }

    public boolean matches(NBTStorable instanceData, Block block, int metadata)
    {
        return sourceMatcher.apply(new BlockMatcher.BlockFragment(block, metadata));
    }

    @Override
    public void transform(InstanceData instanceData, Phase phase, StructureSpawnContext context, IvWorldData worldData, List<Pair<Transformer, NBTStorable>> transformers)
    {
        WeightedBlockState blockState = instanceData.blockState;
        if (blockState.block != null)
        {
            IvBlockCollection blockCollection = worldData.blockCollection;

            int[] areaSize = new int[]{blockCollection.width, blockCollection.height, blockCollection.length};
            BlockCoord lowerCoord = context.lowerCoord();

            NBTTagCompound nbtTagCompound = blockState.tileEntityInfo.trim().length() > 0 && blockState.block.hasTileEntity(blockState.metadata)
                    ? TransformerReplace.tryParse(blockState.tileEntityInfo) : null;

            for (BlockCoord sourceCoord : blockCollection)
            {
                BlockCoord worldCoord = context.transform.apply(sourceCoord, areaSize).add(lowerCoord);
                if (context.includes(worldCoord))
                {
                    Block block = blockCollection.getBlock(sourceCoord);
                    int meta = blockCollection.getMetadata(sourceCoord);

                    if (matches(instanceData, block, meta))
                    {
                        context.world.setBlock(worldCoord.x, worldCoord.y, worldCoord.z, blockState.block, blockState.metadata, 3);

                        if (nbtTagCompound != null)
                        {
                            TileEntity tileentity = context.world.getTileEntity(worldCoord.x, worldCoord.y, worldCoord.z);
                            if (tileentity != null)
                                tileentity.readFromNBT(TransformerReplace.positionedCopy(nbtTagCompound, worldCoord));
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getDisplayString()
    {
        return "Replace All: " + sourceMatcher.getDisplayString();
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceBTReplaceAll(this, navigator, delegate);
    }

    @Override
    public InstanceData prepareInstanceData(StructurePrepareContext context)
    {
        WeightedBlockState blockState;
        if (destination.list.size() > 0)
            blockState = WeightedSelector.selectItem(context.random, destination.list);
        else
            blockState = new WeightedBlockState(null, Blocks.air, 0, "");

        return new InstanceData(blockState);
    }

    @Override
    public InstanceData loadInstanceData(StructureLoadContext context, NBTBase nbt)
    {
        return new InstanceData(nbt instanceof NBTTagCompound ? (NBTTagCompound) nbt : new NBTTagCompound());
    }

    @Override
    public boolean generatesInPhase(InstanceData instanceData, Phase phase)
    {
        return phase == Phase.BEFORE;
    }

    public static class InstanceData implements NBTStorable
    {
        public WeightedBlockState blockState;

        public InstanceData(WeightedBlockState blockState)
        {
            this.blockState = blockState;
        }

        public InstanceData(NBTTagCompound compound)
        {
            this.blockState = new WeightedBlockState(compound.getCompoundTag("blockState"));
        }

        @Override
        public NBTBase writeToNBT()
        {
            NBTTagCompound compound = new NBTTagCompound();

            compound.setTag("blockState", blockState.writeToNBT());

            return compound;
        }
    }

    public static class Serializer implements JsonDeserializer<TransformerReplaceAll>, JsonSerializer<TransformerReplaceAll>
    {
        private MCRegistry registry;
        private Gson gson;

        public Serializer(MCRegistry registry)
        {
            this.registry = registry;
            gson = new GsonBuilder().registerTypeAdapter(WeightedBlockState.class, new WeightedBlockState.Serializer(registry)).create();
        }

        @Override
        public TransformerReplaceAll deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(jsonElement, "transformerReplace");

            String expression = TransformerReplace.Serializer.readLegacyMatcher(jsonObject, "source", "sourceMetadata"); // Legacy
            if (expression == null)
                expression = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "sourceExpression", "");

            TransformerReplaceAll transformer = new TransformerReplaceAll(expression);

            if (!transformer.destination.setPreset(JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "destinationPreset", null)))
            {
                if (jsonObject.has("destination"))
                    Collections.addAll(transformer.destination.list, gson.fromJson(jsonObject.get("destination"), WeightedBlockState[].class));
            }

            if (jsonObject.has("dest"))
            {
                // Legacy
                String destBlock = JsonUtils.getJsonObjectStringFieldValue(jsonObject, "dest");
                Block dest = registry.blockFromID(destBlock);
                byte[] destMeta = context.deserialize(jsonObject.get("destMetadata"), byte[].class);

                transformer.destination.setToCustom();
                for (byte b : destMeta)
                    transformer.destination.list.add(new WeightedBlockState(null, dest, b, ""));
            }

            return transformer;
        }

        @Override
        public JsonElement serialize(TransformerReplaceAll transformer, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("sourceExpression", transformer.sourceMatcher.getExpression());

            if (transformer.destination.getPreset() != null)
                jsonObject.addProperty("destinationPreset", transformer.destination.getPreset());
            jsonObject.add("destination", gson.toJsonTree(transformer.destination.list));

            return jsonObject;
        }
    }
}
