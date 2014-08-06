/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex;

import cpw.mods.fml.client.registry.RenderingRegistry;
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
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import ivorius.ivtoolkit.network.PacketExtendedEntityPropertiesData;
import ivorius.ivtoolkit.network.PacketExtendedEntityPropertiesDataHandler;
import ivorius.ivtoolkit.network.PacketGuiAction;
import ivorius.ivtoolkit.network.PacketGuiActionHandler;
import ivorius.reccomplex.blocks.*;
import ivorius.reccomplex.commands.*;
import ivorius.reccomplex.events.RCFMLEventHandler;
import ivorius.reccomplex.events.RCForgeEventHandler;
import ivorius.reccomplex.gui.RCGuiHandler;
import ivorius.reccomplex.items.*;
import ivorius.reccomplex.network.*;
import ivorius.reccomplex.random.Poem;
import ivorius.reccomplex.worldgen.StructureHandler;
import ivorius.reccomplex.worldgen.StructureSaveHandler;
import ivorius.reccomplex.worldgen.WorldGenStructures;
import ivorius.reccomplex.worldgen.blockTransformers.*;
import ivorius.reccomplex.worldgen.genericStructures.RCStructures;
import ivorius.reccomplex.worldgen.inventory.InventoryGeneratorSaveHandler;
import ivorius.reccomplex.worldgen.inventory.RCInventoryGenerators;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Logger;

@Mod(modid = RecurrentComplex.MODID, version = RecurrentComplex.VERSION, name = RecurrentComplex.NAME, guiFactory = "ivorius.reccomplex.gui.RCConfigGuiFactory")
public class RecurrentComplex
{
    public static final String NAME = "Recurrent Complex";
    public static final String MODID = "reccomplex";
    public static final String VERSION = "1.0";

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

    public static CreativeTabs tabStructureTools = new CreativeTabs("structureTools")
    {
        @Override
        public Item getTabIconItem()
        {
            return RCItems.blockSelector;
        }
    };

    public static CreativeTabs tabInventoryGenerators = new CreativeTabs("inventoryGenerators")
    {
        @Override
        public Item getTabIconItem()
        {
            return RCItems.inventoryGenerationTag;
        }
    };

    public static RCForgeEventHandler forgeEventHandler;
    public static RCFMLEventHandler fmlEventHandler;

    public static SimpleNetworkWrapper network;

    public static RCGuiHandler guiHandler;

    public static RCCommunicationHandler communicationHandler;

    public static Material materialNegativeSpace;

    public static int negativeSpaceRenderID;

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

        materialNegativeSpace = new MaterialNegativeSpace();

        RCItems.blockSelector = new ItemBlockSelector().setUnlocalizedName("blockSelector").setTextureName(textureBase + "blockSelector");
        RCItems.blockSelector.setCreativeTab(tabStructureTools);
        GameRegistry.registerItem(RCItems.blockSelector, "blockSelector", MODID);

        RCItems.blockSelectorFloating = new ItemBlockSelectorFloating(2.0f).setUnlocalizedName("blockSelectorFloating").setTextureName(textureBase + "blockSelectorFloating");
        RCItems.blockSelectorFloating.setCreativeTab(tabStructureTools);
        GameRegistry.registerItem(RCItems.blockSelectorFloating, "blockSelectorFloating", MODID);

        RCItems.inventoryGenerationTag = new ItemInventoryGenerationMultiTag().setUnlocalizedName("inventoryGenerationTag").setTextureName(textureBase + "inventoryGenerationTag");
        RCItems.inventoryGenerationTag.setCreativeTab(tabInventoryGenerators);
        GameRegistry.registerItem(RCItems.inventoryGenerationTag, "inventoryGenerationTag", MODID);

        RCItems.inventoryGenerationSingleTag = new ItemInventoryGenerationSingleTag().setUnlocalizedName("inventoryGenerationSingleTag").setTextureName(textureBase + "inventoryGenerationSingleTag");
        RCItems.inventoryGenerationSingleTag.setCreativeTab(tabInventoryGenerators);
        GameRegistry.registerItem(RCItems.inventoryGenerationSingleTag, "inventoryGenerationSingleTag", MODID);

        RCItems.artifactGenerationTag = new ItemArtifactGenerator().setUnlocalizedName("artifactGenerationTag").setTextureName(textureBase + "artifactGenerationTag");
        RCItems.artifactGenerationTag.setCreativeTab(tabInventoryGenerators);
        GameRegistry.registerItem(RCItems.artifactGenerationTag, "artifactGenerationTag", MODID);

        RCItems.bookGenerationTag = new ItemBookGenerator().setUnlocalizedName("bookGenerationTag").setTextureName(textureBase + "bookGenerationTag");
        RCItems.bookGenerationTag.setCreativeTab(tabInventoryGenerators);
        GameRegistry.registerItem(RCItems.bookGenerationTag, "bookGenerationTag", MODID);

        RCBlocks.negativeSpace = new BlockNegativeSpace().setBlockName("negativeSpace").setBlockTextureName(textureBase + "negativeSpace");
        RCBlocks.negativeSpace.setCreativeTab(tabStructureTools);
        GameRegistry.registerBlock(RCBlocks.negativeSpace, ItemBlockNegativeSpace.class, "negativeSpace");

        RCBlocks.naturalFloor = new BlockNaturalFloor().setBlockName("naturalFloor").setBlockTextureName(textureBase + "naturalFloor");
        RCBlocks.naturalFloor.setCreativeTab(tabStructureTools);
        GameRegistry.registerBlock(RCBlocks.naturalFloor, ItemBlock.class, "naturalFloor");

        RCBlocks.structureGenerator = new BlockStructureGenerator().setBlockName("structureGenerator").setBlockTextureName(textureBase + "structureGenerator");
        RCBlocks.structureGenerator.setCreativeTab(tabStructureTools);
        GameRegistry.registerBlock(RCBlocks.structureGenerator, ItemStructureGenerator.class, "structureGenerator");
        GameRegistry.registerTileEntityWithAlternatives(TileEntityStructureGenerator.class, "RCStructureGenerator", "SGStructureGenerator");

        RCBlocks.mazeGenerator = new BlockMazeGenerator().setBlockName("mazeGenerator").setBlockTextureName(textureBase + "mazeGenerator");
        RCBlocks.mazeGenerator.setCreativeTab(tabStructureTools);
        GameRegistry.registerBlock(RCBlocks.mazeGenerator, ItemMazeGenerator.class, "mazeGenerator");
        GameRegistry.registerTileEntityWithAlternatives(TileEntityMazeGenerator.class, "RCMazeGenerator", "SGMazeGenerator");
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

        StructureHandler.registerBlockTransformer("natural", BlockTransformerNatural.class, new BTProviderNatural());
        StructureHandler.registerBlockTransformer("naturalAir", BlockTransformerNaturalAir.class, new BTProviderNaturalAir());
        StructureHandler.registerBlockTransformer("pillar", BlockTransformerPillar.class, new BTProviderPillar());
        StructureHandler.registerBlockTransformer("replace", BlockTransformerReplace.class, new BTProviderReplace());
        StructureHandler.registerBlockTransformer("negativeSpace", BlockTransformerNegativeSpace.class, new BTProviderNegativeSpace());

        Poem.registerThemes(MODID, "love", "summer", "war", "winter", "grief");

//        GameRegistry.registerWorldGenerator(new WorldGenStructures(), 50);
        RCInventoryGenerators.registerVanillaInventoryGenerators();
        RCInventoryGenerators.registerModInventoryGenerators();

        RCStructures.generateDefaultStructures(RCConfig.generateDefaultStructures);

        negativeSpaceRenderID = RenderingRegistry.getNextAvailableRenderId();
        proxy.registerRenderers();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        InventoryGeneratorSaveHandler.reloadAllCustomInventoryGenerators();
        StructureSaveHandler.reloadAllCustomStructures();
    }

    @EventHandler
    public void onServerStart(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandExportStructure());
        event.registerServerCommand(new CommandEditStructure());
        event.registerServerCommand(new CommandGenerateStructure());
        event.registerServerCommand(new CommandImportStructure());
        event.registerServerCommand(new CommandStructuresReload());
        event.registerServerCommand(new CommandSelectPoint());
        event.registerServerCommand(new CommandSelectFill());
        event.registerServerCommand(new CommandSelectReplace());
        event.registerServerCommand(new CommandSelectFillSphere());
        event.registerServerCommand(new CommandSelectNatural());
        event.registerServerCommand(new CommandSelectCopy());
        event.registerServerCommand(new CommandPaste());
        event.registerServerCommand(new CommandSelectMove());
        event.registerServerCommand(new CommandBiomeDict());
    }
}