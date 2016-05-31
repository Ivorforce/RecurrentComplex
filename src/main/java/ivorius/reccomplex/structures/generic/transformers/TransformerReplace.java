/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.transformers;

import com.google.gson.*;
import net.minecraft.util.BlockPos;
import ivorius.ivtoolkit.random.WeightedSelector;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.editstructure.transformers.TableDataSourceBTReplace;
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
import net.minecraft.block.state.IBlockState;
import ivorius.reccomplex.utils.BlockStates;
import ivorius.reccomplex.utils.NBTNone;
import ivorius.reccomplex.utils.PresettedList;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
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
        this(BlockMatcher.of(RecurrentComplex.specialRegistry, Blocks.wool));
        destination.setToDefault();
    }

    public TransformerReplace(String sourceExpression)
    {
        this.sourceMatcher = new BlockMatcher(RecurrentComplex.specialRegistry, sourceExpression);
    }

    public static NBTTagCompound tryParse(String json)
    {
        NBTTagCompound nbt = null;

        try
        {
            nbt = JsonToNBT.getTagFromJson(json);
        }
        catch (NBTException ignored)
        {

        }

        return nbt;
    }

    public static NBTTagCompound positionedCopy(NBTTagCompound compound, BlockPos teCoord)
    {
        NBTTagCompound positioned = (NBTTagCompound) compound.copy();

        positioned.setInteger("x", teCoord.getX());
        positioned.setInteger("y", teCoord.getY());
        positioned.setInteger("z", teCoord.getZ());

        return positioned;
    }

    public TransformerReplace replaceWith(WeightedBlockState... states)
    {
        destination.setContents(Arrays.asList(states));
        return this;
    }

    @Override
    public boolean matches(NBTNone instanceData, IBlockState state)
    {
        return sourceMatcher.apply(state);
    }

    @Override
    public void transformBlock(NBTNone instanceData, Phase phase, StructureSpawnContext context, BlockPos coord, IBlockState sourceState)
    {
        WeightedBlockState blockState;
        if (destination.getList().size() > 0)
            blockState = WeightedSelector.selectItem(context.random, destination.getList());
        else
            blockState = new WeightedBlockState(null, null, "");

        NBTTagCompound parsedTileEntityInfo = blockState.tileEntityInfo.trim().length() > 0
                ? tryParse(blockState.tileEntityInfo)
                : null;

        setBlockWith(context, coord, context.world, blockState, parsedTileEntityInfo);
    }

    public static void setBlockWith(StructureSpawnContext context, BlockPos coord, World world, WeightedBlockState entry, NBTTagCompound parsedTileEntityInfo)
    {
        if (entry.state != null && RecurrentComplex.specialRegistry.isSafe(entry.state.getBlock()))
        {
            context.setBlock(coord, entry.state);

            // Behavior as in CommandSetBlock
            if (parsedTileEntityInfo != null && entry.state.getBlock().hasTileEntity(entry.state))
            {
                NBTTagCompound nbtTagCompound = positionedCopy(parsedTileEntityInfo, coord);
                if (nbtTagCompound != null)
                {
                    TileEntity tileentity = world.getTileEntity(coord);
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
                    Collections.addAll(transformer.destination.getList(), gson.fromJson(jsonObject.get("destination"), WeightedBlockState[].class));
            }

            if (jsonObject.has("dest"))
            {
                // Legacy
                Block dest = registry.blockFromID(new ResourceLocation(JsonUtils.getJsonObjectStringFieldValue(jsonObject, "dest")));
                byte[] destMeta = context.deserialize(jsonObject.get("destMetadata"), byte[].class);

                transformer.destination.setToCustom();
                for (byte b : destMeta)
                    transformer.destination.getList().add(new WeightedBlockState(null, dest.getStateFromMeta(b), ""));
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
            jsonObject.add("destination", gson.toJsonTree(transformer.destination.getList()));

            return jsonObject;
        }
    }
}
