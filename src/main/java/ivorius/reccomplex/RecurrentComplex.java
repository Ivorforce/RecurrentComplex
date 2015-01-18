/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import ivorius.ivtoolkit.network.PacketExtendedEntityPropertiesData;
import ivorius.ivtoolkit.network.PacketExtendedEntityPropertiesDataHandler;
import ivorius.ivtoolkit.network.PacketGuiAction;
import ivorius.ivtoolkit.network.PacketGuiActionHandler;
import ivorius.reccomplex.client.rendering.RCBlockRendering;
import ivorius.reccomplex.commands.*;
import ivorius.reccomplex.events.RCFMLEventHandler;
import ivorius.reccomplex.events.RCForgeEventHandler;
import ivorius.reccomplex.gui.RCGuiHandler;
import ivorius.reccomplex.items.*;
import ivorius.reccomplex.network.*;
import ivorius.reccomplex.random.Poem;
import ivorius.reccomplex.worldgen.StructureHandler;
import ivorius.reccomplex.worldgen.StructureSaveHandler;
import ivorius.reccomplex.worldgen.StructureSelector;
import ivorius.reccomplex.worldgen.blockTransformers.*;
import ivorius.reccomplex.worldgen.inventory.CustomGenericItemCollectionHandler;
import ivorius.reccomplex.worldgen.inventory.RCInventoryGenerators;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Logger;

import java.util.Collections;

@Mod(modid = RecurrentComplex.MODID, version = RecurrentComplex.VERSION, name = RecurrentComplex.NAME, guiFactory = "ivorius.reccomplex.gui.RCConfigGuiFactory",
        dependencies = "required-after:ivtoolkit@[1.0.1,)")
public class RecurrentComplex
{
    public static final String NAME = "Recurrent Complex";
    public static final String MODID = "reccomplex";
    public static final String VERSION = "0.9.3";

    public static final boolean USE_JSON_FOR_NBT = true;
    public static final boolean USE_ZIP_FOR_STRUCTURE_FILES = true;

    @Instance(value = MODID)
    public static RecurrentComplex instance;

    @SidedProxy(clientSide = "ivorius.reccomplex.client.ClientProxy", serverSide = "ivorius.reccomplex.server.ServerProxy")
    public static RCProxy proxy;

    public static String filePathTexturesFull = "reccomplex:textures/mod/";
    public static String filePathTextures = "textures/mod/";
    public static String textureBase = "reccomplex:";

    public static Logger logger;
    public static Configuration config;

    public static RCForgeEventHandler forgeEventHandler;
    public static RCFMLEventHandler fmlEventHandler;

    public static SimpleNetworkWrapper network;

    public static RCGuiHandler guiHandler;

    public static RCCommunicationHandler communicationHandler;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();

        config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        RCConfig.loadConfig(null);
        config.save();

        forgeEventHandler = new RCForgeEventHandler();
        forgeEventHandler.register();

        fmlEventHandler = new RCFMLEventHandler();
        fmlEventHandler.register();

        guiHandler = new RCGuiHandler();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, guiHandler);

        communicationHandler = new RCCommunicationHandler(logger, MODID, instance);

        RCRegistryHandler.preInit(event, this);
    }

    @EventHandler
    public void load(FMLInitializationEvent event)
    {
        network = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        network.registerMessage(PacketExtendedEntityPropertiesDataHandler.class, PacketExtendedEntityPropertiesData.class, 0, Side.CLIENT);
        network.registerMessage(PacketGuiActionHandler.class, PacketGuiAction.class, 1, Side.SERVER);
        network.registerMessage(PacketEditInventoryGeneratorHandler.class, PacketEditInventoryGenerator.class, 2, Side.CLIENT);
        network.registerMessage(PacketEditInventoryGeneratorHandler.class, PacketEditInventoryGenerator.class, 3, Side.SERVER);
        network.registerMessage(PacketEditMazeBlockHandler.class, PacketEditMazeBlock.class, 4, Side.CLIENT);
        network.registerMessage(PacketEditMazeBlockHandler.class, PacketEditMazeBlock.class, 5, Side.SERVER);
        network.registerMessage(PacketEditStructureHandler.class, PacketEditStructure.class, 6, Side.CLIENT);
        network.registerMessage(PacketEditStructureHandler.class, PacketEditStructure.class, 7, Side.SERVER);
        network.registerMessage(PacketEditStructureBlockHandler.class, PacketEditStructureBlock.class, 8, Side.CLIENT);
        network.registerMessage(PacketEditStructureBlockHandler.class, PacketEditStructureBlock.class, 9, Side.SERVER);
        network.registerMessage(PacketEditInvGenMultiTagHandler.class, PacketEditInvGenMultiTag.class, 9, Side.SERVER);

        RCRegistryHandler.load(event, this);
        proxy.registerRenderers();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        loadAllModData();

        CustomGenericItemCollectionHandler.reloadAllCustomInventoryGenerators();
        StructureSaveHandler.reloadAllCustomStructures();
    }

    @EventHandler
    public void onServerStart(FMLServerStartingEvent event)
    {
        RCCommands.onServerStart(event);
    }

    public static void loadAllModData()
    {
        for (String modid : Loader.instance().getIndexedModList().keySet())
        {
            CustomGenericItemCollectionHandler.loadInventoryGeneratorsFromMod(modid);
            StructureSaveHandler.loadStructuresFromMod(modid);
        }
    }
}