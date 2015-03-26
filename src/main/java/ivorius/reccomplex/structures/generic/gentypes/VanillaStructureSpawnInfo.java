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
import net.minecraft.util.StatCollector;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;

/**
 * Created by lukas on 19.01.15.
 */
public class VanillaStructureSpawnInfo extends StructureGenerationInfo
{
    public String id = "";

    public int spawnWeight;
    public int minBaseLimit;
    public int maxBaseLimit;
    public int minScaledLimit;
    public int maxScaledLimit;

    public BlockCoord spawnShift;

    public VanillaStructureSpawnInfo()
    {
        this("VanillaGen1", 20, 2, 5, 3, 3, new BlockCoord(0, 0, 0));
    }

    public VanillaStructureSpawnInfo(String id, int spawnWeight, int minBaseLimit, int maxBaseLimit, int minScaledLimit, int maxScaledLimit, BlockCoord spawnShift)
    {
        this.id = id;
        this.spawnWeight = spawnWeight;
        this.minBaseLimit = minBaseLimit;
        this.maxBaseLimit = maxBaseLimit;
        this.minScaledLimit = minScaledLimit;
        this.maxScaledLimit = maxScaledLimit;
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
        return StatCollector.translateToLocal("reccomplex.generationInfo.vanillaStructure");
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceVanillaStructureGenerationInfo(navigator, delegate, this);
    }

    public static class Serializer implements JsonSerializer<VanillaStructureSpawnInfo>, JsonDeserializer<VanillaStructureSpawnInfo>
    {
        @Override
        public VanillaStructureSpawnInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(json, "vanillaStructureSpawnInfo");

            String id = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "id", "");

            int spawnWeight = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "spawnWeight", 0);

            int minBaseLimit = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "minBaseLimit", 0);
            int maxBaseLimit = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "maxBaseLimit", 0);
            int minScaledLimit = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "minScaledLimit", 0);
            int maxScaledLimit = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "maxScaledLimit", 0);

            int spawnX = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "spawnShiftX", 0);
            int spawnY = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "spawnShiftY", 0);
            int spawnZ = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "spawnShiftZ", 0);

            return new VanillaStructureSpawnInfo(id, spawnWeight, minBaseLimit, maxBaseLimit, minScaledLimit, maxScaledLimit, new BlockCoord(spawnX, spawnY, spawnZ));
        }

        @Override
        public JsonElement serialize(VanillaStructureSpawnInfo src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", src.id);

            jsonObject.addProperty("spawnWeight", src.spawnWeight);

            jsonObject.addProperty("minBaseLimit", src.minBaseLimit);
            jsonObject.addProperty("maxBaseLimit", src.maxBaseLimit);
            jsonObject.addProperty("minScaledLimit", src.minScaledLimit);
            jsonObject.addProperty("maxScaledLimit", src.maxScaledLimit);

            jsonObject.addProperty("spawnShiftX", src.spawnShift.x);
            jsonObject.addProperty("spawnShiftY", src.spawnShift.y);
            jsonObject.addProperty("spawnShiftZ", src.spawnShift.z);

            return jsonObject;
        }
    }
}
