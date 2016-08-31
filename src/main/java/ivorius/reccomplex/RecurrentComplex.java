/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex;

import ivorius.ivtoolkit.network.PacketEntityCapabilityData;
import ivorius.ivtoolkit.network.PacketEntityCapabilityDataHandler;
import ivorius.ivtoolkit.network.PacketGuiAction;
import ivorius.ivtoolkit.network.PacketGuiActionHandler;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.ivtoolkit.tools.MCRegistryDefault;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.events.RCForgeEventHandler;
import ivorius.reccomplex.files.RCFileTypeRegistry;
import ivorius.reccomplex.gui.RCGuiHandler;
import ivorius.reccomplex.gui.container.IvGuiRegistry;
import ivorius.reccomplex.network.*;
import ivorius.reccomplex.structures.registry.MCRegistrySpecial;
import ivorius.reccomplex.structures.schematics.SchematicLoader;
import ivorius.reccomplex.utils.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

import java.util.Map;

@Mod(modid = RecurrentComplex.MOD_ID, version = RecurrentComplex.VERSION, name = RecurrentComplex.NAME, guiFactory = "ivorius.reccomplex.gui.RCConfigGuiFactory",
        dependencies = "required-after:ivtoolkit")
public class RecurrentComplex
{
    public static final String NAME = "Recurrent Complex";
    public static final String MOD_ID = "reccomplex";
    public static final String VERSION = "1.0.2";

    public static final boolean USE_JSON_FOR_NBT = true;
    public static final boolean USE_ZIP_FOR_STRUCTURE_FILES = true;

    @Instance(value = MOD_ID)
    public static RecurrentComplex instance;

    @SidedProxy(clientSide = "ivorius.reccomplex.client.ClientProxy", serverSide = "ivorius.reccomplex.server.ServerProxy")
    public static RCProxy proxy;

    public static String filePathTexturesFull = "reccomplex:textures/mod/";
    public static String filePathTextures = "textures/mod/";
    public static String textureBase = "reccomplex:";

    public static Logger logger;
    public static Configuration config;

    public static RCFileTypeRegistry fileTypeRegistry;
    public static MCRegistry mcRegistry;

    public static FMLRemapper remapper;
    public static FMLRemapperConvenience cremapper;
    public static MCRegistrySpecial specialRegistry;
    public static FMLMissingRemapper missingRemapper;

    public static RCForgeEventHandler forgeEventHandler;

    public static SimpleNetworkWrapper network;

    public static RCGuiHandler guiHandler;

    public static RCCommunicationHandler communicationHandler;

    public static boolean isLite()
    {
        return RCConfig.isLightweightMode();
    }

    public static boolean checkPerms(EntityPlayer player)
    {
        boolean b = canHandleSaving(player);
        if (!b)
            ServerTranslations.get("reccomplex.save.permission");
        return !b;
    }

    public static boolean canHandleSaving(EntityPlayer player)
    {
        return player.canCommandSenderUseCommand(2, "setblock");
    }

    @NetworkCheckHandler
    public boolean checkNetwork(Map<String, String> mods, Side side)
    {
        return isLite() || mods.containsKey(MOD_ID); // If Lite, it's considered server-side only
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

        remapper = new FMLRemapper();
        specialRegistry = new MCRegistrySpecial(mcRegistry = new MCRegistryRemapping(new MCRegistryDefault(), remapper), remapper);
        cremapper = new FMLRemapperConvenience(MOD_ID, specialRegistry, remapper);
        missingRemapper = new FMLMissingRemapper(new MCRegistryDefault(), remapper);

        forgeEventHandler = new RCForgeEventHandler();
        forgeEventHandler.register();

        guiHandler = new RCGuiHandler();
        IvGuiRegistry.INSTANCE.register(MOD_ID, guiHandler);

        communicationHandler = new RCCommunicationHandler(logger, MOD_ID, instance);

        RCRegistryHandler.preInit(event, this);
    }

    @EventHandler
    public void load(FMLInitializationEvent event)
    {
        network = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);

        if (event.getSide().isClient())
            registerClientPackets();
        registerServerPackets();

        RCRegistryHandler.load(event, this);
        proxy.registerRenderers();
    }

    protected void registerServerPackets()
    {
        network.registerMessage(PacketGuiActionHandler.class, PacketGuiAction.class, 1, Side.SERVER);
        network.registerMessage(PacketSaveInvGenComponentHandler.class, PacketSaveInvGenComponent.class, 2, Side.SERVER);
        network.registerMessage(PacketEditTileEntityHandler.class, PacketEditTileEntity.class, 5, Side.SERVER);
        network.registerMessage(PacketSaveStructureHandler.class, PacketSaveStructure.class, 7, Side.SERVER);
        network.registerMessage(PacketSyncItemHandler.class, PacketSyncItem.class, 9, Side.SERVER);
        network.registerMessage(PacketItemEventHandler.class, PacketItemEvent.class, 11, Side.SERVER);
        network.registerMessage(PacketInspectBlockHandler.class, PacketInspectBlock.class, 12, Side.SERVER);
        network.registerMessage(PacketOpenGuiHandler.class, PacketOpenGui.class, 15, Side.SERVER);
    }

    protected void registerClientPackets()
    {
        network.registerMessage(PacketEntityCapabilityDataHandler.class, PacketEntityCapabilityData.class, 0, Side.CLIENT);
        network.registerMessage(PacketEditInvGenComponentHandler.class, PacketEditInvGenComponent.class, 3, Side.CLIENT);
        network.registerMessage(PacketEditTileEntityHandler.class, PacketEditTileEntity.class, 4, Side.CLIENT);
        network.registerMessage(PacketEditStructureHandler.class, PacketEditStructure.class, 6, Side.CLIENT);
        network.registerMessage(PacketSyncItemHandler.class, PacketSyncItem.class, 8, Side.CLIENT);
        network.registerMessage(PacketItemEventHandler.class, PacketItemEvent.class, 10, Side.CLIENT);
        network.registerMessage(PacketItemEventHandler.class, PacketItemEvent.class, 10, Side.CLIENT);
        network.registerMessage(PacketInspectBlockHandler.class, PacketInspectBlock.class, 13, Side.CLIENT);
        network.registerMessage(PacketOpenGuiHandler.class, PacketOpenGui.class, 14, Side.CLIENT);
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
        missingRemapper.onMissingMapping(event);
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