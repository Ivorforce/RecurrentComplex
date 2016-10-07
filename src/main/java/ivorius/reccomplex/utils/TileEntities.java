/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils;

import ivorius.ivtoolkit.tools.IvSideClient;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * Created by lukas on 29.08.16.
 */
public class TileEntities
{
    /**
     * Do NOT use for anything serious! This is just a temporary (hah!) workaround for Tile Entities requiring a world to load.
     */
    public static World getAnyWorld()
    {
        return FMLCommonHandler.instance().getSide().isClient()
                ? IvSideClient.getClientWorld()
                : FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld();
    }
}
