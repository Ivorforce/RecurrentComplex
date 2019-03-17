/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.transformers;

import com.google.gson.*;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.ivtoolkit.transform.PosTransformer;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.editstructure.transformers.TableDataSourceBTBiomeSpecific;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.utils.algebra.ExpressionCache;
import ivorius.reccomplex.world.gen.feature.structure.Environment;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureLoadContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructurePrepareContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;
import ivorius.reccomplex.world.gen.feature.structure.generic.WeightedBlockState;
import ivorius.reccomplex.utils.expression.BlockExpression;
import ivorius.reccomplex.nbt.NBTNone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public class TransformerVillageSpecific extends TransformerSingleBlock<NBTNone>
{
    public final MyVillage village = new MyVillage(); // Accessor for biome specific replacements
    public BlockExpression sourceMatcher;

    public TransformerVillageSpecific()
    {
        this(null, "");
    }

    public TransformerVillageSpecific(@Nullable String id, String sourceExpression)
    {
        super(id != null ? id : randomID(TransformerVillageSpecific.class));
        this.sourceMatcher = ExpressionCache.of(new BlockExpression(RecurrentComplex.specialRegistry), sourceExpression);
    }

    @Override
    public boolean matches(Environment environment, NBTNone instanceData, BlockPos sourcePos, IBlockState state)
    {
        return village.trySetType(environment.villageType) && sourceMatcher.test(state) && village.getBiomeSpecificBlockState(state) != state;
    }

    @Override
    public void transformBlock(NBTNone instanceData, Phase phase, StructureSpawnContext context, RunTransformer transformer, int[] areaSize, BlockPos coord, IBlockState sourceState)
    {
        if (village.trySetType(context.environment.villageType))
        {
            IBlockState state = village.getBiomeSpecificBlockState(sourceState);
            context.setBlock(coord, PosTransformer.transformBlockState(state, context.transform), 2);
        }
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

    @SideOnly(Side.CLIENT)
    @Override
    public String getDisplayString()
    {
        return IvTranslations.get("reccomplex.transformer.villagereplace");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceBTBiomeSpecific(this, navigator, delegate);
    }

    @Override
    public boolean generatesInPhase(NBTNone instanceData, Phase phase)
    {
        return phase == Phase.BEFORE;
    }

    public static class Serializer implements JsonDeserializer<TransformerVillageSpecific>, JsonSerializer<TransformerVillageSpecific>
    {
        private MCRegistry registry;
        private Gson gson;

        public Serializer(MCRegistry registry)
        {
            this.registry = registry;
            gson = new GsonBuilder().registerTypeAdapter(WeightedBlockState.class, new WeightedBlockState.Serializer(registry)).create();
        }

        @Override
        public TransformerVillageSpecific deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonObject = JsonUtils.asJsonObject(jsonElement, "transformerReplace");

            String id = readID(jsonObject);

            String expression = JsonUtils.getString(jsonObject, "sourceExpression", "");

            return new TransformerVillageSpecific(id, expression);
        }

        @Override
        public JsonElement serialize(TransformerVillageSpecific transformer, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", transformer.id());
            jsonObject.addProperty("sourceExpression", transformer.sourceMatcher.getExpression());

            return jsonObject;
        }
    }

    private static class MyVillage extends StructureVillagePieces.Village
    {
        public boolean trySetType(@Nullable Integer type)
        {
            if (type != null)
                this.structureType = type;
            return type != null;
        }

        @Override
        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            return false;
        }

        @Override
        public IBlockState getBiomeSpecificBlockState(IBlockState blockstateIn)
        {
            return super.getBiomeSpecificBlockState(blockstateIn);
        }
    }
}
