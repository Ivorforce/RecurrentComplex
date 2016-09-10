/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.structures.generic.transformers;

import com.google.gson.*;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.ivtoolkit.transform.PosTransformer;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.editstructure.transformers.TableDataSourceBTBiomeSpecific;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.Environment;
import ivorius.reccomplex.structures.StructureLoadContext;
import ivorius.reccomplex.structures.StructurePrepareContext;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.structures.generic.WeightedBlockState;
import ivorius.reccomplex.structures.generic.matchers.BlockMatcher;
import ivorius.reccomplex.utils.NBTNone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureVillagePieces;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public class TransformerVillageSpecific extends TransformerSingleBlock<NBTNone>
{
    public final MyVillage village = new MyVillage(); // Accessor for biome specific replacements
    public BlockMatcher sourceMatcher;

    public TransformerVillageSpecific()
    {
        this(randomID(TransformerVillageSpecific.class), "");
    }

    public TransformerVillageSpecific(String id, String sourceExpression)
    {
        super(id);
        this.sourceMatcher = new BlockMatcher(RecurrentComplex.specialRegistry, sourceExpression);
    }

    @Override
    public boolean matches(Environment environment, NBTNone instanceData, IBlockState state)
    {
        return village.trySetType(environment.villageType) && sourceMatcher.test(state) && village.getBiomeSpecificBlockState(state) != state;
    }

    @Override
    public void transformBlock(NBTNone instanceData, Phase phase, StructureSpawnContext context, BlockPos coord, IBlockState sourceState)
    {
        if (village.trySetType(context.environment.villageType))
        {
            IBlockState state = village.getBiomeSpecificBlockState(sourceState);
            if (RecurrentComplex.specialRegistry.isSafe(state.getBlock()))
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

    @Override
    public String getDisplayString()
    {
        return IvTranslations.get("reccomplex.transformer.villagereplace");
    }

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
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(jsonElement, "transformerReplace");

            String id = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "id", randomID(TransformerVillageSpecific.class));

            String expression = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "sourceExpression", "");

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
                this.field_189928_h = type;
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
