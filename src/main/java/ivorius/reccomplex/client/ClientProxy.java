/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.client;

import ivorius.reccomplex.RCProxy;
import net.minecraft.client.Minecraft;

import java.io.File;

/**
 * Created by lukas on 24.05.14.
 */
public class ClientProxy implements RCProxy
{
    @Override
    public File getBaseFolderFile(String filename)
    {
        return new File(Minecraft.getMinecraft().mcDataDir, filename);
    }
}
