/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.client;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RCProxy;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.commands.RCCommands;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.File;

import static ivorius.reccomplex.block.RCBlocks.*;
import static ivorius.reccomplex.item.RCItems.*;

/**
 * Created by lukas on 24.05.14.
 */
@SideOnly(Side.CLIENT)
public class ClientProxy implements RCProxy
{
    public static final String[] VALID_MODIFIER_KEYS = new String[]{"ctrl", "lctrl", "rctrl", "shift", "lshift", "rshift"};

    @Override
    public File getDataDirectory()
    {
        return Minecraft.getMinecraft().mcDataDir;
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
            registerItemsForDefaultRender(lootGenerationTag, lootGenerationSingleTag, lootGenerationComponentTag);
            registerItemsForDefaultRender(artifactGenerationTag, bookGenerationTag);
            registerItemsForDefaultRender(inspector);

            registerTypeItemsForDefaultRender(genericSpace, genericSolid);
            registerItemsForDefaultRender(structureGenerator, mazeGenerator, spawnCommands, spawnScript);
        }
    }

    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        RCCommands.registerClientCommands(ClientCommandHandler.instance);
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
            ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(String.format("%s_%d", id, i), "inventory"));
    }

    protected void registerItemForDefaultRender(Item item)
    {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(RecurrentComplex.mcRegistry.idFromItem(item), "inventory"));
    }
}
