/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex;

import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.ivtoolkit.tools.MCRegistryDefault;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.events.handlers.RCForgeEventHandler;
import ivorius.reccomplex.events.handlers.RCRecurrentComplexEventHandler;
import ivorius.reccomplex.events.handlers.RCTerrainGenEventHandler;
import ivorius.reccomplex.files.loading.FileLoader;
import ivorius.reccomplex.files.loading.LeveledRegistry;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.files.saving.FileSaver;
import ivorius.reccomplex.gui.RCGuiHandler;
import ivorius.reccomplex.gui.container.IvGuiRegistry;
import ivorius.mcopts.translation.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.structure.registry.MCRegistrySpecial;
import ivorius.reccomplex.world.gen.feature.structure.schematics.SchematicLoader;
import ivorius.reccomplex.utils.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
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
    public static final String VERSION = "1.4.8.2";

    public static final boolean USE_JSON_FOR_NBT = true;
    public static final boolean USE_ZIP_FOR_STRUCTURE_FILES = true;

    public static final boolean PARTIALLY_SPAWN_NATURAL_STRUCTURES = true;

    @Instance(value = MOD_ID)
    public static RecurrentComplex instance;

    @SidedProxy(clientSide = "ivorius.reccomplex.client.ClientProxy", serverSide = "ivorius.reccomplex.server.ServerProxy")
    public static RCProxy proxy;

    public static String filePathTextures = "textures/mod/";
    public static String textureBase = "reccomplex:";

    public static Logger logger;
    public static Configuration config;

    public static FileLoader loader;
    public static FileSaver saver;
    public static MCRegistry mcRegistry;

    public static FMLRemapper remapper;
    public static FMLRemapperConvenience cremapper;
    public static MCRegistrySpecial specialRegistry;
    public static FMLMissingRemapper missingRemapper;

    public static RCForgeEventHandler forgeEventHandler;
    public static RCTerrainGenEventHandler terrainEventHandler;
    public static RCRecurrentComplexEventHandler recurrentComplexEventHandler;

    public static SimpleNetworkWrapper network;

    public static RCGuiHandler guiHandler;

    public static RCCommunicationHandler communicationHandler;

    public static ServerTranslations translations = new ServerTranslations() {
        @Override
        public boolean translateServerSide()
        {
            return isLite();
        }
    };

    public static boolean isLite()
    {
        return RCConfig.isLightweightMode();
    }

    public static boolean checkPerms(EntityPlayer player)
    {
        boolean b = canHandleSaving(player);
        if (!b)
            translations.get("reccomplex.save.permission");
        return !b;
    }

    public static boolean canHandleSaving(EntityPlayer player)
    {
        return player.canUseCommand(2, "setblock");
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
        // After loading config, re-test vanilla acceptance, because of lightweight mode
        NetworkRegistry.INSTANCE.registry().get(Loader.instance().getIndexedModList().get(MOD_ID)).testVanillaAcceptance();

        logger.trace(isLite() ? "Entering lightweight mode!" : "Entering default mode!");

        loader = new FileLoader();
        saver = new FileSaver();

        remapper = new FMLRemapper();
        specialRegistry = new MCRegistrySpecial(mcRegistry = new MCRegistryRemapping(new MCRegistryDefault(), remapper), remapper);
        cremapper = new FMLRemapperConvenience(MOD_ID, specialRegistry, remapper);
        missingRemapper = new FMLMissingRemapper(new MCRegistryDefault(), remapper);

        forgeEventHandler = new RCForgeEventHandler();
        forgeEventHandler.register();
        terrainEventHandler = new RCTerrainGenEventHandler();
        terrainEventHandler.register();
        recurrentComplexEventHandler = new RCRecurrentComplexEventHandler();
        recurrentComplexEventHandler.register();

        guiHandler = new RCGuiHandler();
        IvGuiRegistry.INSTANCE.register(MOD_ID, guiHandler);

        communicationHandler = new RCCommunicationHandler(logger, MOD_ID, instance);

        RCRegistryHandler.preInit(event, this);
        proxy.preInit(event);
        proxy.registerRenderers(); // Needs to be called during preInit now
    }

    @EventHandler
    public void load(FMLInitializationEvent event)
    {
        network = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);

        RCRegistryHandler.load(event, this);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        ResourceDirectory.tryReload(loader, LeveledRegistry.Level.MODDED);
        ResourceDirectory.tryReload(loader, LeveledRegistry.Level.CUSTOM);

        SchematicLoader.initializeFolder();
    }

    @EventHandler
    public void onServerStart(FMLServerStartingEvent event)
    {
        RCCommands.onServerStart(event);
    }

    @EventHandler
    public void onServerStarted(FMLServerStartedEvent event)
    {
        ResourceDirectory.tryReload(loader, LeveledRegistry.Level.SERVER);
    }
}