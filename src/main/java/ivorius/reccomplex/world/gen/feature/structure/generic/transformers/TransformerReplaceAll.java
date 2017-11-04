/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.transformers;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import ivorius.ivtoolkit.blocks.BlockStates;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.utils.algebra.ExpressionCache;
import ivorius.reccomplex.world.gen.feature.structure.Environment;
import ivorius.reccomplex.utils.presets.PresettedObjects;
import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.random.WeightedSelector;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.editstructure.transformers.TableDataSourceBTReplaceAll;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureLoadContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructurePrepareContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;
import ivorius.reccomplex.world.gen.feature.structure.generic.WeightedBlockState;
import ivorius.reccomplex.utils.expression.BlockExpression;
import ivorius.reccomplex.world.gen.feature.structure.generic.presets.WeightedBlockStatePresets;
import net.minecraft.block.state.IBlockState;
import ivorius.reccomplex.nbt.NBTStorable;
import ivorius.reccomplex.utils.presets.PresettedList;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by lukas on 25.05.14.
 */
public class TransformerReplaceAll extends TransformerSingleBlock<TransformerReplaceAll.InstanceData>
{
    public final PresettedList<WeightedBlockState> destination = new PresettedList<>(WeightedBlockStatePresets.instance(), null);

    public BlockExpression sourceMatcher;

    public TransformerReplaceAll()
    {
        this(null, BlockExpression.of(RecurrentComplex.specialRegistry, Blocks.WOOL, new IntegerRange(0, 15)));
        destination.setPreset("air");
    }

    public TransformerReplaceAll(@Nullable String id, String sourceExpression)
    {
        super(id != null ? id : randomID(TransformerReplaceAll.class));
        this.sourceMatcher = ExpressionCache.of(new BlockExpression(RecurrentComplex.specialRegistry), sourceExpression);
    }

    public TransformerReplaceAll replaceWith(WeightedBlockState... states)
    {
        destination.setContents(Arrays.asList(states));
        return this;
    }

    @Override
    public boolean matches(Environment environment, InstanceData instanceData, IBlockState state)
    {
        return sourceMatcher.test(state);
    }

    @Override
    public void transformBlock(InstanceData instanceData, Phase phase, StructureSpawnContext context, RunTransformer transformer, int[] areaSize, BlockPos coord, IBlockState sourceState)
    {
        TransformerReplace.setBlock(context, areaSize, coord, instanceData.blockState, () -> instanceData.tileEntityInfo);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public String getDisplayString()
    {
        return "Replace All: " + sourceMatcher.getDisplayString(null);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceBTReplaceAll(this, navigator, delegate);
    }

    @Override
    public InstanceData prepareInstanceData(StructurePrepareContext context, IvWorldData worldData)
    {
        WeightedBlockState blockState;

        if (destination.getContents().size() > 0)
            blockState = WeightedSelector.selectItem(context.random, destination.getContents());
        else
            blockState = new WeightedBlockState(null, Blocks.AIR.getDefaultState(), null);

        return new InstanceData(blockState, blockState.tileEntityInfo != null ? blockState.tileEntityInfo.copy() : null);
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
            JsonObject jsonObject = JsonUtils.asJsonObject(jsonElement, "transformerReplace");

            String id = readID(jsonObject);

            String expression = TransformerReplace.Serializer.readLegacyMatcher(jsonObject, "source", "sourceMetadata"); // Legacy
            if (expression == null)
                expression = JsonUtils.getString(jsonObject, "sourceExpression", "");

            TransformerReplaceAll transformer = new TransformerReplaceAll(id, expression);

            PresettedObjects.read(jsonObject, gson, transformer.destination, "destinationPreset", "destination", new TypeToken<ArrayList<WeightedBlockState>>(){}.getType());

            if (jsonObject.has("dest"))
            {
                // Legacy
                String destBlock = JsonUtils.getString(jsonObject, "dest");
                Block dest = registry.blockFromID(new ResourceLocation(destBlock));
                byte[] destMeta = context.deserialize(jsonObject.get("destMetadata"), byte[].class);

                transformer.destination.setToCustom();
                for (byte b : destMeta)
                    transformer.destination.getContents().add(new WeightedBlockState(null, BlockStates.fromMetadata(dest, b), null));
            }

            return transformer;
        }

        @Override
        public JsonElement serialize(TransformerReplaceAll transformer, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", transformer.id());
            jsonObject.addProperty("sourceExpression", transformer.sourceMatcher.getExpression());

            PresettedObjects.write(jsonObject, gson, transformer.destination, "destinationPreset", "destination");

            return jsonObject;
        }
    }
}
