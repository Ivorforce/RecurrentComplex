/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.gentypes;

import com.google.gson.*;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.generic.SavedMazeComponent;

import java.lang.reflect.Type;

/**
 * Created by lukas on 07.10.14.
 */
public class MazeGenerationInfo
{
    public String mazeID;
    public SavedMazeComponent mazeComponent;

    public MazeGenerationInfo(String mazeID, SavedMazeComponent mazeComponent)
    {
        this.mazeID = mazeID;
        this.mazeComponent = mazeComponent;
    }

    public static class Serializer implements JsonSerializer<MazeGenerationInfo>, JsonDeserializer<MazeGenerationInfo>
    {
        @Override
        public MazeGenerationInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(json, "MazeGenerationInfo");

            String mazeID = JsonUtils.getJsonObjectStringFieldValue(jsonObject, "mazeID");
            SavedMazeComponent mazeComponent = context.deserialize(jsonObject.get("component"), SavedMazeComponent.class);

            return new MazeGenerationInfo(mazeID, mazeComponent);
        }

        @Override
        public JsonElement serialize(MazeGenerationInfo src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("mazeID", src.mazeID);
            jsonObject.add("component", context.serialize(src.mazeComponent));

            return jsonObject;
        }
    }
}
