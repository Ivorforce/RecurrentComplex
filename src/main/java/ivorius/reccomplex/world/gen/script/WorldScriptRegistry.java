/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.script;

import ivorius.reccomplex.utils.NBTStringTypeRegistry;

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
