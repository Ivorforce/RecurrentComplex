/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.client;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RCProxy;
import ivorius.reccomplex.RecurrentComplex;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.io.File;

import static ivorius.reccomplex.blocks.RCBlocks.*;
import static ivorius.reccomplex.items.RCItems.*;

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
        if (!RecurrentComplex.isLite())
        {
            registerItemsForDefaultRender(blockSelector, blockSelectorFloating);
            registerItemsForDefaultRender(inventoryGenerationTag, inventoryGenerationSingleTag, inventoryGenerationComponentTag);
            registerItemsForDefaultRender(artifactGenerationTag, bookGenerationTag);
            registerItemsForDefaultRender(inspector);

            registerTypeItemsForDefaultRender(genericSpace, genericSolid);
            registerItemsForDefaultRender(structureGenerator, mazeGenerator, spawnCommands, spawnScript);
        }
    }

    protected void registerTypeItemsForDefaultRender(Block... blocks)
    {
        for (Block block : blocks)
            registerTypeItemForDefaultRender(block);
    }

    protected void registerItemsForDefaultRender(Block... blocks)
    {
        for (Block block : blocks)
            registerItemForDefaultRender(block);
    }

    protected void registerItemsForDefaultRender(Item... items)
    {
        for (Item item : items)
            registerItemForDefaultRender(item);
    }

    protected void registerItemForDefaultRender(Block block)
    {
        registerItemForDefaultRender(Item.getItemFromBlock(block));
    }

    protected void registerTypeItemForDefaultRender(Block block)
    {
        Item item = Item.getItemFromBlock(block);
        ResourceLocation id = RecurrentComplex.mcRegistry.idFromItem(item);
        for (int i = 0; i < 16; i++)
        {
            Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, i, new ModelResourceLocation(String.format("%s_%d", id, i), "inventory"));
            ModelBakery.registerItemVariants(item, new ResourceLocation(String.format("%s_%d", id, i)));
        }
    }

    protected void registerItemForDefaultRender(Item item)
    {
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, new ModelResourceLocation(RecurrentComplex.mcRegistry.idFromItem(item), "inventory"));
    }
}
