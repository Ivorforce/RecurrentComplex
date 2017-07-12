/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.generation;

import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import com.google.gson.*;
import ivorius.ivtoolkit.blocks.Directions;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.gui.editstructure.gentypes.TableDataSourceVanillaGeneration;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.utils.algebra.ExpressionCache;
import ivorius.reccomplex.utils.expression.BiomeExpression;
import ivorius.reccomplex.world.gen.feature.structure.Placer;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.GenericPlacer;
import ivorius.reccomplex.world.gen.feature.villages.GenericVillageCreationHandler;
import ivorius.reccomplex.world.gen.feature.villages.GenericVillagePiece;
import ivorius.reccomplex.world.gen.feature.villages.TemporaryVillagerRegistry;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by lukas on 19.01.15.
 */
public class VanillaGeneration extends GenerationType
{
    public Double generationWeight;
    public double minBaseLimit;
    public double maxBaseLimit;
    public double minScaledLimit;
    public double maxScaledLimit;

    @Nullable
    public EnumFacing front;

    public BlockPos spawnShift;

    public BiomeExpression biomeExpression;

    public VanillaGeneration()
    {
        this(null, null, 0, 2, 1, 1, EnumFacing.NORTH, BlockPos.ORIGIN, "");
    }

    public VanillaGeneration(@Nullable String id, Double generationWeight, double minBaseLimit, double maxBaseLimit, double minScaledLimit, double maxScaledLimit, EnumFacing front, BlockPos spawnShift, String biomeExpression)
    {
        super(id != null ? id : randomID(VanillaGeneration.class));
        this.generationWeight = generationWeight;
        this.minBaseLimit = minBaseLimit;
        this.maxBaseLimit = maxBaseLimit;
        this.minScaledLimit = minScaledLimit;
        this.maxScaledLimit = maxScaledLimit;
        this.front = front;
        this.spawnShift = spawnShift;
        this.biomeExpression = ExpressionCache.of(new BiomeExpression(), biomeExpression);
    }

    @Nonnull
    @Override
    public String id()
    {
        return id;
    }

    @Override
    public void setID(@Nonnull String id)
    {
        this.id = id;
    }

    public void setSpawnShift(BlockPos spawnShift)
    {
        this.spawnShift = spawnShift;
    }

    @Override
    public String displayString()
    {
        return IvTranslations.get("reccomplex.generationInfo.vanilla.title");
    }

    @Nullable
    @Override
    public Placer placer()
    {
        return GenericPlacer.surfacePlacer();
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceVanillaGeneration(navigator, delegate, this);
    }

    public boolean generatesIn(Biome biome)
    {
        return biomeExpression.test(biome);
    }

    public double getActiveWeight()
    {
        return generationWeight != null ? generationWeight : 1.0;
    }

    public int getVanillaWeight(double tweak)
    {
        return MathHelper.floor(getActiveWeight() * tweak * RCConfig.baseVillageSpawnWeight + 0.5);
    }

    public static class Serializer implements JsonSerializer<VanillaGeneration>, JsonDeserializer<VanillaGeneration>
    {
        @Override
        public VanillaGeneration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.asJsonObject(json, "vanillaStructureSpawnInfo");

            String id = readID(jsonObject);

            Double spawnWeight = jsonObject.has("generationWeight") ? JsonUtils.getDouble(jsonObject, "generationWeight") : null;

            double minBaseLimit = JsonUtils.getDouble(jsonObject, "minBaseLimit", 0);
            double maxBaseLimit = JsonUtils.getDouble(jsonObject, "maxBaseLimit", 0);
            double minScaledLimit = JsonUtils.getDouble(jsonObject, "minScaledLimit", 0);
            double maxScaledLimit = JsonUtils.getDouble(jsonObject, "maxScaledLimit", 0);

            int spawnX = JsonUtils.getInt(jsonObject, "spawnShiftX", 0);
            int spawnY = JsonUtils.getInt(jsonObject, "spawnShiftY", 0);
            int spawnZ = JsonUtils.getInt(jsonObject, "spawnShiftZ", 0);

            EnumFacing front = jsonObject.has("front")
                    ? Directions.deserialize(JsonUtils.getString(jsonObject, "front"))
                    : null;

            String biomeExpression = JsonUtils.getString(jsonObject, "biomeExpression", "");

            return new VanillaGeneration(id, spawnWeight, minBaseLimit, maxBaseLimit, minScaledLimit, maxScaledLimit, front, new BlockPos(spawnX, spawnY, spawnZ), biomeExpression);
        }

        @Override
        public JsonElement serialize(VanillaGeneration src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", src.id);

            if (src.generationWeight != null)
                jsonObject.addProperty("generationWeight", src.generationWeight);

            jsonObject.addProperty("minBaseLimit", src.minBaseLimit);
            jsonObject.addProperty("maxBaseLimit", src.maxBaseLimit);
            jsonObject.addProperty("minScaledLimit", src.minScaledLimit);
            jsonObject.addProperty("maxScaledLimit", src.maxScaledLimit);

            jsonObject.addProperty("spawnShiftX", src.spawnShift.getX());
            jsonObject.addProperty("spawnShiftY", src.spawnShift.getY());
            jsonObject.addProperty("spawnShiftZ", src.spawnShift.getZ());

            if (src.front != null)
                jsonObject.addProperty("front", Directions.serialize(src.front));

            jsonObject.addProperty("biomeExpression", src.biomeExpression.getExpression());

            return jsonObject;
        }
    }

    public static class Cache implements StructureRegistry.GenerationCache
    {
        protected StructureRegistry registry;

        @Override
        public void setRegistry(StructureRegistry registry)
        {
            this.registry = registry;
        }

        @Override
        public void clear()
        {
            updateVanillaGenerations();
            for (Pair<Structure<?>, VanillaGeneration> pair : registry.getGenerationTypes(VanillaGeneration.class))
            {
                String structureID = registry.id(pair.getLeft());
                String generationID = pair.getRight().id();
                Class<? extends GenericVillagePiece> clazz = GenericVillageCreationHandler.getPieceClass(structureID, generationID);
                if (clazz != null)
                    MapGenStructureIO.registerStructureComponent(clazz, "Rc:" + structureID + "_" + generationID);
            }
        }

        private void updateVanillaGenerations()
        {
            TemporaryVillagerRegistry.instance().setHandlers(
                    Sets.newHashSet(Collections2.transform(registry.getGenerationTypes(VanillaGeneration.class),
                            input -> GenericVillageCreationHandler.forGeneration(registry.id(input.getLeft()), input.getRight().id())).stream()
                            .filter(Objects::nonNull).collect(Collectors.toList()))
            );
        }
    }
}
