/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.scripts.world;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import ivorius.reccomplex.utils.NBTStringTypeRegistry;

import java.util.Set;

/**
 * Created by lukas on 14.09.15.
 */
public class WorldScriptRegistry extends NBTStringTypeRegistry<WorldScript>
{
    public static final WorldScriptRegistry INSTANCE = new WorldScriptRegistry("script", "id");

    public WorldScriptRegistry(String objectKey, String typeKey)
    {
        super(objectKey, typeKey);
    }

    public WorldScriptRegistry()
    {
    }
}
