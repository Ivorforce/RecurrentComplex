/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.gentypes;

import com.google.gson.*;
import net.minecraft.util.BlockPos;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.gui.editstructure.gentypes.TableDataSourceVanillaStructureGenerationInfo;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.generic.matchers.BiomeMatcher;
import ivorius.ivtoolkit.blocks.Directions;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;

/**
 * Created by lukas on 19.01.15.
 */
public class VanillaStructureGenerationInfo extends StructureGenerationInfo
{
    public String id = "";

    public Double generationWeight;
    public double minBaseLimit;
    public double maxBaseLimit;
    public double minScaledLimit;
    public double maxScaledLimit;

    public EnumFacing front;

    public BlockPos spawnShift;

    public BiomeMatcher biomeMatcher;

    public VanillaStructureGenerationInfo()
    {
        this(randomID("MC"), null, 2, 5, 3, 3, EnumFacing.NORTH, new BlockPos(0, 0, 0), "");
    }

    public VanillaStructureGenerationInfo(String id, Double generationWeight, double minBaseLimit, double maxBaseLimit, double minScaledLimit, double maxScaledLimit, EnumFacing front, BlockPos spawnShift, String biomeExpression)
    {
        this.id = id;
        this.generationWeight = generationWeight;
        this.minBaseLimit = minBaseLimit;
        this.maxBaseLimit = maxBaseLimit;
        this.minScaledLimit = minScaledLimit;
        this.maxScaledLimit = maxScaledLimit;
        this.front = front;
        this.spawnShift = spawnShift;
        biomeMatcher = new BiomeMatcher(biomeExpression);
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
        return StatCollector.translateToLocal("reccomplex.generationInfo.vanilla.title");
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceVanillaStructureGenerationInfo(navigator, delegate, this);
    }

    public boolean generatesIn(BiomeGenBase biome)
    {
        return biomeMatcher.apply(biome);
    }

    public double getActiveWeight()
    {
        return generationWeight != null ? generationWeight : 1.0;
    }

    public int getVanillaWeight()
    {
        return MathHelper.floor_double(getActiveWeight() * RCConfig.baseVillageSpawnWeight + 0.5);
    }

    public static class Serializer implements JsonSerializer<VanillaStructureGenerationInfo>, JsonDeserializer<VanillaStructureGenerationInfo>
    {
        @Override
        public VanillaStructureGenerationInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(json, "vanillaStructureSpawnInfo");

            String id = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "id", "");

            Double spawnWeight = jsonObject.has("generationWeight") ? JsonUtils.getJsonObjectDoubleFieldValue(jsonObject, "generationWeight") : null;

            double minBaseLimit = JsonUtils.getJsonObjectDoubleFieldValueOrDefault(jsonObject, "minBaseLimit", 0);
            double maxBaseLimit = JsonUtils.getJsonObjectDoubleFieldValueOrDefault(jsonObject, "maxBaseLimit", 0);
            double minScaledLimit = JsonUtils.getJsonObjectDoubleFieldValueOrDefault(jsonObject, "minScaledLimit", 0);
            double maxScaledLimit = JsonUtils.getJsonObjectDoubleFieldValueOrDefault(jsonObject, "maxScaledLimit", 0);

            int spawnX = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "spawnShiftX", 0);
            int spawnY = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "spawnShiftY", 0);
            int spawnZ = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "spawnShiftZ", 0);

            EnumFacing front = Directions.deserialize(JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "front", "NORTH"));

            String biomeExpression = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "biomeExpression", "");

            return new VanillaStructureGenerationInfo(id, spawnWeight, minBaseLimit, maxBaseLimit, minScaledLimit, maxScaledLimit, front, new BlockPos(spawnX, spawnY, spawnZ), biomeExpression);
        }

        @Override
        public JsonElement serialize(VanillaStructureGenerationInfo src, Type typeOfSrc, JsonSerializationContext context)
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

            jsonObject.addProperty("front", Directions.serialize(src.front));

            jsonObject.addProperty("biomeExpression", src.biomeMatcher.getExpression());

            return jsonObject;
        }
    }
}
