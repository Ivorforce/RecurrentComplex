/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.client;

import ivorius.structuregen.SGProxy;
import net.minecraft.client.Minecraft;

import java.io.File;

/**
 * Created by lukas on 24.05.14.
 */
public class ClientProxy implements SGProxy
{
    @Override
    public File getBaseFolderFile(String filename)
    {
        return new File(Minecraft.getMinecraft().mcDataDir, filename);
    }
}
