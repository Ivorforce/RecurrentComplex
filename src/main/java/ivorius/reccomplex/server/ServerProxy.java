/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.server;

import ivorius.reccomplex.RCProxy;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.File;

/**
 * Created by lukas on 24.05.14.
 */
public class ServerProxy implements RCProxy
{
    @Override
    public File getDataDirectory()
    {
        return FMLCommonHandler.instance().getSidedDelegate().getServer().getDataDirectory();
    }

    @Override
    public void loadConfig(String configID)
    {

    }

    @Override
    public void registerRenderers()
    {

    }
}
