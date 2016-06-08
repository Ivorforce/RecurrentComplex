/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import ivorius.reccomplex.json.JsonUtils;

import java.util.Collections;

/**
 * Created by lukas on 08.06.16.
 */
public class PresettedLists
{
    public static <T> void read(JsonObject jsonObject, Gson gson, PresettedList<T> list, String presetKey, String listKey, Class<T[]> clazz)
    {
        if (!list.setPreset(JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, presetKey, null)))
        {
            if (jsonObject.has(listKey))
                Collections.addAll(list.getList(), gson.fromJson(jsonObject.get(listKey), clazz));
            else
                list.setToDefault();
        }
    }

    public static <T> void write(JsonObject jsonObject, Gson gson, PresettedList<T> list, String presetKey, String listKey)
    {
        if (list.getPreset() != null)
            jsonObject.addProperty(presetKey, list.getPreset());
        jsonObject.add(listKey, gson.toJsonTree(list.getList()));
    }
}
