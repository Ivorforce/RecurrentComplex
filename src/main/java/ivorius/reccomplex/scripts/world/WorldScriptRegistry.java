/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.scripts.world;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.Set;

/**
 * Created by lukas on 14.09.15.
 */
public class WorldScriptRegistry
{
    public static final WorldScriptRegistry INSTANCE = new WorldScriptRegistry();

    private BiMap<String, Class<? extends WorldScript>> scripts = HashBiMap.create();

    public Class<? extends WorldScript> register(String key, Class<? extends WorldScript> value)
    {
        return scripts.put(key, value);
    }

    public Class<? extends WorldScript> getScript(String key)
    {
        return scripts.get(key);
    }

    public String getID(Class<? extends WorldScript> script)
    {
        return scripts.inverse().get(script);
    }

    public Set<String> keySet()
    {
        return scripts.keySet();
    }
}
