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
import org.lwjgl.input.Keyboard;

import java.io.File;

/**
 * Created by lukas on 24.05.14.
 */
public class ClientProxy implements RCProxy
{
    public static final String[] VALID_MODIFIER_KEYS = new String[]{"ctrl", "lctrl", "rctrl", "shift", "lshift", "rshift"};

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

        if (configID == null || configID.equals(RCConfig.CATEGORY_CONTROLS))
        {
            RCConfig.blockSelectorModifierKeys = parseModifierKeys(RecurrentComplex.config.getString("blockSelectorModifierKeys", RCConfig.CATEGORY_CONTROLS, "ctrl", "The key to be held when you want to make a secondary selection with block selectors", VALID_MODIFIER_KEYS));
        }
    }

    private int[] parseModifierKeys(String key)
    {
        switch (key)
        {
            default:
            case "ctrl":
                return new int[]{Keyboard.KEY_LCONTROL, Keyboard.KEY_RCONTROL};
            case "lctrl":
                return new int[]{Keyboard.KEY_LCONTROL};
            case "rctrl":
                return new int[]{Keyboard.KEY_RCONTROL};
            case "shift":
                return new int[]{Keyboard.KEY_LSHIFT, Keyboard.KEY_RSHIFT};
            case "lshift":
                return new int[]{Keyboard.KEY_LSHIFT};
            case "rshift":
                return new int[]{Keyboard.KEY_RSHIFT};
        }
    }

    @Override
    public void registerRenderers()
    {
        RenderingRegistry.registerBlockHandler(RCBlockRendering.negativeSpaceRenderID, new RenderNegativeSpace());
    }
}
