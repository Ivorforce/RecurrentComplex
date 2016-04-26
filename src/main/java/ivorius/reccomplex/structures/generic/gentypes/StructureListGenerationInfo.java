/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.gentypes;

import com.google.gson.*;
import ivorius.reccomplex.gui.editstructure.gentypes.TableDataSourceStructureListGenerationInfo;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.ivtoolkit.blocks.Directions;
import ivorius.ivtoolkit.random.WeightedSelector;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;

/**
 * Created by lukas on 21.02.15.
 */
public class StructureListGenerationInfo extends StructureGenerationInfo implements WeightedSelector.Item
{
    public String id = "";

    public String listID;

    public Double weight;

    public int shiftX;
    public int shiftY;
    public int shiftZ;

    public ForgeDirection front;

    public StructureListGenerationInfo()
    {
        this(randomID("List"), "", null, 0, 0, 0, ForgeDirection.NORTH);
    }

    public StructureListGenerationInfo(String id, String listID, Double weight, int shiftX, int shiftY, int shiftZ, ForgeDirection front)
    {
        this.id = id;
        this.listID = listID;
        this.weight = weight;
        this.shiftX = shiftX;
        this.shiftY = shiftY;
        this.shiftZ = shiftZ;
        this.front = front;
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
        return StatCollector.translateToLocalFormatted("reccomplex.generationInfo.structureList.title", listID);
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceStructureListGenerationInfo(navigator, delegate, this);
    }

    @Override
    public double getWeight()
    {
        return weight != null ? weight : 1.0;
    }

    public static class Serializer implements JsonSerializer<StructureListGenerationInfo>, JsonDeserializer<StructureListGenerationInfo>
    {
        @Override
        public StructureListGenerationInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(json, "vanillaStructureSpawnInfo");

            String id = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "id", "");

            String listID = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "listID", "");

            Double weight = jsonObject.has("weight") ? JsonUtils.getJsonObjectDoubleFieldValue(jsonObject, "weight") : null;

            int positionX = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "positionX", 0);
            int positionY = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "positionY", 0);
            int positionZ = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "positionZ", 0);

            ForgeDirection front = Directions.deserialize(JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "front", "NORTH"));

            return new StructureListGenerationInfo(id, listID, weight, positionX, positionY, positionZ, front);
        }

        @Override
        public JsonElement serialize(StructureListGenerationInfo src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", src.id);

            jsonObject.addProperty("listID", src.listID);

            if (src.weight != null)
                jsonObject.addProperty("weight", src.weight);

            jsonObject.addProperty("positionX", src.shiftX);
            jsonObject.addProperty("positionY", src.shiftY);
            jsonObject.addProperty("positionZ", src.shiftZ);

            jsonObject.addProperty("front", Directions.serialize(src.front));

            return jsonObject;
        }
    }
}
