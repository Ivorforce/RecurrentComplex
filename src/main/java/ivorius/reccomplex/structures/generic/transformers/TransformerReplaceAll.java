/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.transformers;

import com.google.gson.*;
import ivorius.reccomplex.utils.PresettedLists;
import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.random.WeightedSelector;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.RecurrentComplex;
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
import net.minecraft.block.state.IBlockState;
import ivorius.reccomplex.utils.NBTStorable;
import ivorius.reccomplex.utils.PresettedList;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * Created by lukas on 25.05.14.
 */
public class TransformerReplaceAll extends TransformerSingleBlock<TransformerReplaceAll.InstanceData>
{
    public final PresettedList<WeightedBlockState> destination = new PresettedList<>(WeightedBlockStatePresets.instance(), null);

    public BlockMatcher sourceMatcher;

    public TransformerReplaceAll()
    {
        this(randomID(TransformerReplaceAll.class), BlockMatcher.of(RecurrentComplex.specialRegistry, Blocks.WOOL, new IntegerRange(0, 15)));
        destination.setToDefault();
    }

    public TransformerReplaceAll(String id, String sourceExpression)
    {
        super(id);
        this.sourceMatcher = new BlockMatcher(RecurrentComplex.specialRegistry, sourceExpression);
    }

    public TransformerReplaceAll replaceWith(WeightedBlockState... states)
    {
        destination.setContents(Arrays.asList(states));
        return this;
    }

    @Override
    public boolean matches(InstanceData instanceData, IBlockState state)
    {
        return sourceMatcher.apply(state);
    }

    @Override
    public void transformBlock(InstanceData instanceData, Phase phase, StructureSpawnContext context, BlockPos coord, IBlockState sourceState)
    {
        TransformerReplace.setBlockWith(context, coord, context.world, instanceData.blockState, instanceData.tileEntityInfo);
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

        if (destination.getList().size() > 0)
            blockState = WeightedSelector.selectItem(context.random, destination.getList());
        else
            blockState = new WeightedBlockState(null, Blocks.AIR.getDefaultState(), "");

        NBTTagCompound tileEntityInfo = blockState.tileEntityInfo.trim().length() > 0 && blockState.state.getBlock().hasTileEntity(blockState.state)
                ? TransformerReplace.tryParse(blockState.tileEntityInfo) : null;

        return new InstanceData(blockState, tileEntityInfo);
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
        public NBTTagCompound tileEntityInfo;

        public InstanceData(WeightedBlockState blockState, NBTTagCompound tileEntityInfo)
        {
            this.blockState = blockState;
            this.tileEntityInfo = tileEntityInfo;
        }

        public InstanceData(NBTTagCompound compound)
        {
            this.blockState = new WeightedBlockState(RecurrentComplex.specialRegistry, compound.getCompoundTag("blockState"));

            this.tileEntityInfo = compound.hasKey("tileEntityInfo", Constants.NBT.TAG_COMPOUND)
                    ? (NBTTagCompound) compound.getCompoundTag("tileEntityInfo").copy()
                    : null;
        }

        @Override
        public NBTBase writeToNBT()
        {
            NBTTagCompound compound = new NBTTagCompound();

            compound.setTag("blockState", blockState.writeToNBT(RecurrentComplex.specialRegistry));

            if (tileEntityInfo != null)
                compound.setTag("tileEntityInfo", tileEntityInfo.copy());

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

            String id = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "id", randomID(TransformerReplaceAll.class));

            String expression = TransformerReplace.Serializer.readLegacyMatcher(jsonObject, "source", "sourceMetadata"); // Legacy
            if (expression == null)
                expression = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "sourceExpression", "");

            TransformerReplaceAll transformer = new TransformerReplaceAll(id, expression);

            PresettedLists.read(jsonObject, gson, transformer.destination, "destinationPreset", "destination", WeightedBlockState[].class);

            if (jsonObject.has("dest"))
            {
                // Legacy
                String destBlock = JsonUtils.getJsonObjectStringFieldValue(jsonObject, "dest");
                Block dest = registry.blockFromID(new ResourceLocation(destBlock));
                byte[] destMeta = context.deserialize(jsonObject.get("destMetadata"), byte[].class);

                transformer.destination.setToCustom();
                for (byte b : destMeta)
                    transformer.destination.getList().add(new WeightedBlockState(null, dest.getStateFromMeta(b), ""));
            }

            return transformer;
        }

        @Override
        public JsonElement serialize(TransformerReplaceAll transformer, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", transformer.id());
            jsonObject.addProperty("sourceExpression", transformer.sourceMatcher.getExpression());

            PresettedLists.write(jsonObject, gson, transformer.destination, "destinationPreset", "destination");

            return jsonObject;
        }
    }
}
