/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.gentypes;

import com.google.gson.*;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.reccomplex.gui.editstructure.gentypes.TableDataSourceVanillaStructureGenerationInfo;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.utils.Directions;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;

/**
 * Created by lukas on 19.01.15.
 */
public class VanillaStructureGenerationInfo extends StructureGenerationInfo
{
    public String id = "";

    public Double spawnWeight;
    public int minBaseLimit;
    public int maxBaseLimit;
    public int minScaledLimit;
    public int maxScaledLimit;

    public ForgeDirection front;

    public BlockCoord spawnShift;

    public VanillaStructureGenerationInfo()
    {
        this("VanillaGen1", null, 2, 5, 3, 3, ForgeDirection.NORTH, new BlockCoord(0, 0, 0));
    }

    public VanillaStructureGenerationInfo(String id, Double spawnWeight, int minBaseLimit, int maxBaseLimit, int minScaledLimit, int maxScaledLimit, ForgeDirection front, BlockCoord spawnShift)
    {
        this.id = id;
        this.spawnWeight = spawnWeight;
        this.minBaseLimit = minBaseLimit;
        this.maxBaseLimit = maxBaseLimit;
        this.minScaledLimit = minScaledLimit;
        this.maxScaledLimit = maxScaledLimit;
        this.front = front;
        this.spawnShift = spawnShift;
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

    public int getVanillaWeight()
    {
        return MathHelper.floor_double((spawnWeight != null ? spawnWeight : 1.0) * 10 + 0.5);
    }

    public static class Serializer implements JsonSerializer<VanillaStructureGenerationInfo>, JsonDeserializer<VanillaStructureGenerationInfo>
    {
        @Override
        public VanillaStructureGenerationInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(json, "vanillaStructureSpawnInfo");

            String id = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "id", "");

            Double spawnWeight = jsonObject.has("spawnWeight") ? JsonUtils.getJsonObjectDoubleFieldValue(jsonObject, "spawnWeight") : null;

            int minBaseLimit = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "minBaseLimit", 0);
            int maxBaseLimit = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "maxBaseLimit", 0);
            int minScaledLimit = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "minScaledLimit", 0);
            int maxScaledLimit = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "maxScaledLimit", 0);

            int spawnX = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "spawnShiftX", 0);
            int spawnY = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "spawnShiftY", 0);
            int spawnZ = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "spawnShiftZ", 0);

            ForgeDirection front = Directions.deserialize(JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "front", "NORTH"));

            return new VanillaStructureGenerationInfo(id, spawnWeight, minBaseLimit, maxBaseLimit, minScaledLimit, maxScaledLimit, front, new BlockCoord(spawnX, spawnY, spawnZ));
        }

        @Override
        public JsonElement serialize(VanillaStructureGenerationInfo src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", src.id);

            if (src.spawnWeight != null)
                jsonObject.addProperty("spawnWeight", src.spawnWeight);

            jsonObject.addProperty("minBaseLimit", src.minBaseLimit);
            jsonObject.addProperty("maxBaseLimit", src.maxBaseLimit);
            jsonObject.addProperty("minScaledLimit", src.minScaledLimit);
            jsonObject.addProperty("maxScaledLimit", src.maxScaledLimit);

            jsonObject.addProperty("spawnShiftX", src.spawnShift.x);
            jsonObject.addProperty("spawnShiftY", src.spawnShift.y);
            jsonObject.addProperty("spawnShiftZ", src.spawnShift.z);

            jsonObject.addProperty("front", Directions.serialize(src.front));

            return jsonObject;
        }
    }
}
