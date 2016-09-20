/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.presets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import ivorius.reccomplex.json.JsonUtils;

import java.lang.reflect.Type;
import java.util.Collections;

/**
 * Created by lukas on 08.06.16.
 */
public class PresettedObjects
{
    public static <T> boolean read(JsonObject jsonObject, Gson gson, PresettedObject<T> object, String presetKey, String objectKey, Type type)
    {
        if (!object.setPreset(JsonUtils.getString(jsonObject, presetKey, null)))
        {
            if (jsonObject.has(objectKey))
                object.setContents(gson.fromJson(jsonObject.get(objectKey), type));
            else
            {
                object.setToDefault();
                return false;
            }
        }

        return true;
    }

    public static <T> void write(JsonObject jsonObject, Gson gson, PresettedObject<T> object, String presetKey, String objectKey)
    {
        if (object.getPreset() != null)
            jsonObject.addProperty(presetKey, object.getPreset());
        jsonObject.add(objectKey, gson.toJsonTree(object.getContents()));
    }
}
