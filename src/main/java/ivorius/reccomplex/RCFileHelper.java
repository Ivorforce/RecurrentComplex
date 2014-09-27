/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex;

import net.minecraft.util.ResourceLocation;

import java.io.InputStream;

/**
 * Created by lukas on 27.09.14.
 */
public class RCFileHelper
{
    public static InputStream inputStreamFromResourceLocation(Class clazz, ResourceLocation resourceLocation)
    {
        return clazz.getResourceAsStream("/assets/" + resourceLocation.getResourceDomain() + "/" + resourceLocation.getResourcePath());
    }
}
