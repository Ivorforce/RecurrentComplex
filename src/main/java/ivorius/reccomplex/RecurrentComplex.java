/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import ivorius.ivtoolkit.network.PacketExtendedEntityPropertiesData;
import ivorius.ivtoolkit.network.PacketExtendedEntityPropertiesDataHandler;
import ivorius.ivtoolkit.network.PacketGuiAction;
import ivorius.ivtoolkit.network.PacketGuiActionHandler;
import ivorius.ivtoolkit.tools.MCRegistryDefault;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.events.RCFMLEventHandler;
import ivorius.reccomplex.events.RCForgeEventHandler;
import ivorius.reccomplex.files.RCFileTypeRegistry;
import ivorius.reccomplex.gui.RCGuiHandler;
import ivorius.reccomplex.network.*;
import ivorius.reccomplex.structures.registry.MCRegistrySpecial;
import ivorius.reccomplex.structures.schematics.SchematicLoader;
import ivorius.reccomplex.utils.FMLRemapper;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Logger;

import java.util.Map;

@Mod(modid = RecurrentComplex.MODID, version = RecurrentComplex.VERSION, name = RecurrentComplex.NAME, guiFactory = "ivorius.reccomplex.gui.RCConfigGuiFactory",
        dependencies = "required-after:ivtoolkit")
public class RecurrentComplex
{
    public static final String NAME = "Recurrent Complex";
    public static final String MODID = "reccomplex";
    public static final String VERSION = "0.9.7";

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

    public static RCFileTypeRegistry fileTypeRegistry;
    public static FMLRemapper remapper;
    public static MCRegistrySpecial mcregistry;

    public static RCForgeEventHandler forgeEventHandler;
    public static RCFMLEventHandler fmlEventHandler;

    public static SimpleNetworkWrapper network;

    public static RCGuiHandler guiHandler;

    public static RCCommunicationHandler communicationHandler;

    public static boolean isLite()
    {
        return RCConfig.isLightweightMode();
    }

    @NetworkCheckHandler
    public boolean checkNetwork(Map<String, String> mods, Side side)
    {
        return isLite() || mods.containsKey(MODID); // If Lite, it's considered server-side only
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();

        config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        RCConfig.loadConfig(null);
        config.save();

        fileTypeRegistry = new RCFileTypeRegistry();
        remapper = new FMLRemapper(MODID, new MCRegistryDefault());
        mcregistry = new MCRegistrySpecial(remapper);

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
        network.registerMessage(PacketEditTileEntityHandler.class, PacketEditTileEntity.class, 4, Side.CLIENT);
        network.registerMessage(PacketEditTileEntityHandler.class, PacketEditTileEntity.class, 5, Side.SERVER);
        network.registerMessage(PacketEditStructureHandler.class, PacketEditStructure.class, 6, Side.CLIENT);
        network.registerMessage(PacketEditStructureHandler.class, PacketEditStructure.class, 7, Side.SERVER);
        network.registerMessage(PacketSyncItemHandler.class, PacketSyncItem.class, 8, Side.CLIENT);
        network.registerMessage(PacketSyncItemHandler.class, PacketSyncItem.class, 9, Side.SERVER);
        network.registerMessage(PacketItemEventHandler.class, PacketItemEvent.class, 10, Side.CLIENT);
        network.registerMessage(PacketItemEventHandler.class, PacketItemEvent.class, 11, Side.SERVER);

        RCRegistryHandler.load(event, this);
        proxy.registerRenderers();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        loadAllModData();

        fileTypeRegistry.reloadCustomFiles();
        SchematicLoader.initializeFolder();
    }

    @EventHandler
    public void onMissingMapping(FMLMissingMappingsEvent event)
    {
        remapper.onMissingMapping(event);
    }

    @EventHandler
    public void onServerStart(FMLServerStartingEvent event)
    {
        RCCommands.onServerStart(event);
    }

    public void loadAllModData()
    {
        for (String modid : Loader.instance().getIndexedModList().keySet())
            fileTypeRegistry.loadFilesFromMod(modid);
    }
}