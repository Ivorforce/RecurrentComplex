/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.transformers;

import com.google.gson.*;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.random.WeightedSelector;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.gui.editstructure.transformers.TableDataSourceBTReplace;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.MCRegistrySpecial;
import ivorius.reccomplex.structures.StructureLoadContext;
import ivorius.reccomplex.structures.StructurePrepareContext;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.structures.generic.WeightedBlockState;
import ivorius.reccomplex.structures.generic.matchers.BlockMatcher;
import ivorius.reccomplex.structures.generic.presets.WeightedBlockStatePresets;
import ivorius.reccomplex.utils.NBTNone;
import ivorius.reccomplex.utils.PresettedList;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by lukas on 25.05.14.
 */
public class TransformerReplace extends TransformerSingleBlock<NBTNone>
{
    public final PresettedList<WeightedBlockState> destination = new PresettedList<>(WeightedBlockStatePresets.instance(), null);
    public BlockMatcher sourceMatcher;

    public TransformerReplace()
    {
        this(BlockMatcher.of(MCRegistrySpecial.INSTANCE, Blocks.wool));
        destination.setToDefault();
    }

    public TransformerReplace(String sourceExpression)
    {
        this.sourceMatcher = new BlockMatcher(MCRegistrySpecial.INSTANCE, sourceExpression);
    }

    public static NBTTagCompound tryParse(String json)
    {
        NBTBase nbtbase = null;

        try
        {
            nbtbase = JsonToNBT.func_150315_a(json);
        }
        catch (NBTException ignored)
        {

        }

        if (nbtbase instanceof NBTTagCompound)
            return (NBTTagCompound) nbtbase;

        return null;
    }

    public static NBTTagCompound positionedCopy(NBTTagCompound compound, BlockCoord teCoord)
    {
        NBTTagCompound positioned = (NBTTagCompound) compound.copy();

        positioned.setInteger("x", teCoord.x);
        positioned.setInteger("y", teCoord.y);
        positioned.setInteger("z", teCoord.z);

        return positioned;
    }

    public TransformerReplace replaceWith(WeightedBlockState... states)
    {
        destination.setContents(Arrays.asList(states));
        return this;
    }

    @Override
    public boolean matches(NBTNone instanceData, Block block, int metadata)
    {
        return sourceMatcher.apply(new BlockMatcher.BlockFragment(block, metadata));
    }

    @Override
    public void transformBlock(NBTNone instanceData, Phase phase, StructureSpawnContext context, BlockCoord coord, Block sourceBlock, int sourceMetadata)
    {
        WeightedBlockState blockState;
        if (destination.list.size() > 0)
            blockState = WeightedSelector.selectItem(context.random, destination.list);
        else
            blockState = new WeightedBlockState(null, null, 0, "");

        NBTTagCompound parsedTileEntityInfo = blockState.tileEntityInfo.trim().length() > 0
                ? tryParse(blockState.tileEntityInfo)
                : null;

        setBlockWith(context, coord, context.world, blockState, parsedTileEntityInfo);
    }

    public static void setBlockWith(StructureSpawnContext context, BlockCoord coord, World world, WeightedBlockState blockState, NBTTagCompound parsedTileEntityInfo)
    {
        if (blockState.block != null && MCRegistrySpecial.INSTANCE.isSafe(blockState.block))
        {
            context.setBlock(coord.x, coord.y, coord.z, blockState.block, blockState.metadata);

            // Behavior as in CommandSetBlock
            if (parsedTileEntityInfo != null && blockState.block.hasTileEntity(blockState.metadata))
            {
                NBTTagCompound nbtTagCompound = positionedCopy(parsedTileEntityInfo, coord);
                if (nbtTagCompound != null)
                {
                    TileEntity tileentity = world.getTileEntity(coord.x, coord.y, coord.z);
                    if (tileentity != null)
                        tileentity.readFromNBT(nbtTagCompound);
                }
            }
        }
    }

    @Override
    public NBTNone prepareInstanceData(StructurePrepareContext context)
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
        return "Replace: " + sourceMatcher.getDisplayString();
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
                String sourceBlock = JsonUtils.getJsonObjectStringFieldValue(jsonObject, blockKey);
                int sourceMeta = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, metadataKey, -1);
                return sourceMeta >= 0 ? String.format("%s & %s%d", sourceBlock, BlockMatcher.METADATA_PREFIX, sourceMeta) : sourceBlock;
            }

            return null;
        }

        @Override
        public TransformerReplace deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(jsonElement, "transformerReplace");

            String expression = readLegacyMatcher(jsonObject, "source", "sourceMetadata"); // Legacy
            if (expression == null)
                expression = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "sourceExpression", "");

            TransformerReplace transformer = new TransformerReplace(expression);

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
        public JsonElement serialize(TransformerReplace transformer, Type par2Type, JsonSerializationContext context)
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
