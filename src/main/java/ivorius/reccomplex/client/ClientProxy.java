/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.client;

import cpw.mods.fml.client.registry.RenderingRegistry;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RCProxy;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.client.rendering.RCBlockRendering;
import ivorius.reccomplex.client.rendering.RenderNegativeSpace;
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

    @Override
    public void loadConfig(String configID)
    {
        if (configID == null || configID.equals(RCConfig.CATEGORY_VISUAL))
        {
            RCConfig.hideRedundantNegativeSpace = RecurrentComplex.config.getBoolean("hideRedundantNegativeSpace", RCConfig.CATEGORY_VISUAL, true, "Only show the edges of negative space blocks? (Improves performance in big builds)");
        }
    }

    @Override
    public void registerRenderers()
    {
        RenderingRegistry.registerBlockHandler(RCBlockRendering.negativeSpaceRenderID, new RenderNegativeSpace());
    }
}
