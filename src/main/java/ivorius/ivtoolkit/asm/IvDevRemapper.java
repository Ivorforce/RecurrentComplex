/*
 * Copyright (c) 2014, Lukas Tenbrink.
 * http://lukas.axxim.net
 *
 * You are free to:
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes, unless you have a permit by the creator.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package ivorius.ivtoolkit.asm;

import java.util.Hashtable;

/**
 * Created by lukas on 25.02.14.
 */
public class IvDevRemapper
{
    public static Hashtable<String, String> fakeMappings = new Hashtable<String, String>();

    private static boolean isSetUp;

    // TODO Read actual DEV files
    public static void setUp()
    {
        isSetUp = true;
    }

    public boolean isSetUp()
    {
        return isSetUp;
    }

    // Hurr fake and gay
    public static String getSRGName(String name)
    {
        String mapping = fakeMappings.get(name);

        return mapping != null ? mapping : name;
    }
}
