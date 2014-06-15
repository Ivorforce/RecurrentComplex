/***************************************************************************************************
 * Copyright (c) 2014, Lukas Tenbrink.
 * http://lukas.axxim.net
 **************************************************************************************************/

package ivorius.structuregen;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import ivorius.structuregen.blocks.*;
import ivorius.structuregen.commands.*;
import ivorius.structuregen.events.SGFMLEventHandler;
import ivorius.structuregen.events.SGForgeEventHandler;
import ivorius.structuregen.gui.SGGuiHandler;
import ivorius.structuregen.items.*;
import ivorius.structuregen.ivtoolkit.ChannelHandlerExtendedEntityPropertiesData;
import ivorius.structuregen.ivtoolkit.ChannelHandlerGuiAction;
import ivorius.structuregen.network.ChannelHandlerEditInventoryGenerator;
import ivorius.structuregen.network.ChannelHandlerEditStructure;
import ivorius.structuregen.network.ChannelHandlerEditStructureBlock;
import ivorius.structuregen.worldgen.StructureHandler;
import ivorius.structuregen.worldgen.StructureSaveHandler;
import ivorius.structuregen.worldgen.WorldGenStructures;
import ivorius.structuregen.worldgen.blockTransformers.*;
import ivorius.structuregen.worldgen.genericStructures.SGStructures;
import ivorius.structuregen.worldgen.inventory.*;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import org.apache.logging.log4j.Logger;

@Mod(modid = StructureGen.MODID, version = StructureGen.VERSION)
public class StructureGen
{
    public static final String MODID = "structuregen";
    public static final String VERSION = "1.0";

    public static final boolean USE_JSON_FOR_NBT = true;
    public static final boolean USE_ZIP_FOR_STRUCTURE_FILES = true;

    @Instance(value = MODID)
    public static StructureGen instance;

    @SidedProxy(clientSide = "ivorius.structuregen.client.ClientProxy", serverSide = "ivorius.structuregen.server.ServerProxy")
    public static SGProxy proxy;

    public static String filePathTexturesFull = "structuregen:textures/mod/";
    public static String filePathTextures = "textures/mod/";
    public static String textureBase = "structuregen:";

    public static Logger logger;

    public static CreativeTabs tabStructureTools = new CreativeTabs("structureTools")
    {
        @Override
        public Item getTabIconItem()
        {
            return SGItems.blockSelector;
        }
    };

    public static CreativeTabs tabInventoryGenerators = new CreativeTabs("inventoryGenerators")
    {
        @Override
        public Item getTabIconItem()
        {
            return SGItems.inventoryGenerationTag;
        }
    };

    public static SGForgeEventHandler forgeEventHandler;
    public static SGFMLEventHandler fmlEventHandler;

    public static ChannelHandlerExtendedEntityPropertiesData chExtendedEntityPropertiesData;
    public static ChannelHandlerEditInventoryGenerator chEditInventoryGenerator;
    public static ChannelHandlerEditStructure chEditStructure;
    public static ChannelHandlerGuiAction chGuiAction;
    public static ChannelHandlerEditStructureBlock chEditStructureBlock;

    public static SGGuiHandler guiHandler;

    public static SGCommunicationHandler communicationHandler;

    public static Material materialNegativeSpace;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();

//        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
//
//        config.load();
//
//        config.save();

        forgeEventHandler = new SGForgeEventHandler();
        forgeEventHandler.register();

        fmlEventHandler = new SGFMLEventHandler();
        fmlEventHandler.register();

        guiHandler = new SGGuiHandler();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, guiHandler);

        chExtendedEntityPropertiesData = new ChannelHandlerExtendedEntityPropertiesData("SG|eepData");
        NetworkRegistry.INSTANCE.newChannel(chExtendedEntityPropertiesData.packetChannel, chExtendedEntityPropertiesData);

        chEditInventoryGenerator = new ChannelHandlerEditInventoryGenerator("SG|editIG");
        NetworkRegistry.INSTANCE.newChannel(chEditInventoryGenerator.packetChannel, chEditInventoryGenerator);

        chEditStructure = new ChannelHandlerEditStructure("SG|editStruc");
        NetworkRegistry.INSTANCE.newChannel(chEditStructure.packetChannel, chEditStructure);

        chGuiAction = new ChannelHandlerGuiAction("SG|guiAct");
        NetworkRegistry.INSTANCE.newChannel(chGuiAction.packetChannel, chGuiAction);

        chEditStructureBlock = new ChannelHandlerEditStructureBlock("SG|editStrucB");
        NetworkRegistry.INSTANCE.newChannel(chEditStructureBlock.packetChannel, chEditStructureBlock);

        communicationHandler = new SGCommunicationHandler(logger, MODID, instance);

        materialNegativeSpace = new MaterialNegativeSpace();

        SGItems.blockSelector = new ItemBlockSelector().setUnlocalizedName("blockSelector").setTextureName(textureBase + "blockSelector");
        SGItems.blockSelector.setCreativeTab(tabStructureTools);
        GameRegistry.registerItem(SGItems.blockSelector, "blockSelector", MODID);

        SGItems.blockSelectorFloating = new ItemBlockSelectorFloating(2.0f).setUnlocalizedName("blockSelectorFloating").setTextureName(textureBase + "blockSelectorFloating");
        SGItems.blockSelectorFloating.setCreativeTab(tabStructureTools);
        GameRegistry.registerItem(SGItems.blockSelectorFloating, "blockSelectorFloating", MODID);

        SGItems.inventoryGenerationTag = new ItemInventoryGenerationMultiTag().setUnlocalizedName("inventoryGenerationTag").setTextureName(textureBase + "inventoryGenerationTag");
        SGItems.inventoryGenerationTag.setCreativeTab(tabInventoryGenerators);
        GameRegistry.registerItem(SGItems.inventoryGenerationTag, "inventoryGenerationTag", MODID);

        SGItems.inventoryGenerationSingleTag = new ItemInventoryGenerationSingleTag().setUnlocalizedName("inventoryGenerationSingleTag").setTextureName(textureBase + "inventoryGenerationSingleTag");
        SGItems.inventoryGenerationSingleTag.setCreativeTab(tabInventoryGenerators);
        GameRegistry.registerItem(SGItems.inventoryGenerationSingleTag, "inventoryGenerationSingleTag", MODID);

        SGBlocks.negativeSpace = new BlockNegativeSpace().setBlockName("negativeSpace").setBlockTextureName(textureBase + "negativeSpace");
        SGBlocks.negativeSpace.setCreativeTab(tabStructureTools);
        GameRegistry.registerBlock(SGBlocks.negativeSpace, ItemBlockNegativeSpace.class, "negativeSpace");

        SGBlocks.naturalFloor = new BlockNaturalFloor().setBlockName("naturalFloor").setBlockTextureName(textureBase + "naturalFloor");
        SGBlocks.naturalFloor.setCreativeTab(tabStructureTools);
        GameRegistry.registerBlock(SGBlocks.naturalFloor, ItemBlock.class, "naturalFloor");

        SGBlocks.structureGenerator = new BlockStructureGenerator().setBlockName("structureGenerator").setBlockTextureName(textureBase + "structureGenerator");
        SGBlocks.structureGenerator.setCreativeTab(tabStructureTools);
        GameRegistry.registerBlock(SGBlocks.structureGenerator, ItemStructureGenerator.class, "structureGenerator");
        GameRegistry.registerTileEntity(TileEntityStructureGenerator.class, "SGStructureGenerator");
    }

    @EventHandler
    public void load(FMLInitializationEvent event)
    {
        StructureHandler.registerBlockTransformer("natural", BlockTransformerNatural.class, new BTProviderNatural());
        StructureHandler.registerBlockTransformer("naturalAir", BlockTransformerNaturalAir.class, new BTProviderNaturalAir());
        StructureHandler.registerBlockTransformer("pillar", BlockTransformerPillar.class, new BTProviderPillar());
        StructureHandler.registerBlockTransformer("replace", BlockTransformerReplace.class, new BTProviderReplace());
        StructureHandler.registerBlockTransformer("negativeSpace", BlockTransformerNegativeSpace.class, new BTProviderNegativeSpace());

        GameRegistry.registerWorldGenerator(new WorldGenStructures(), 100);

        InventoryGeneratorSaveHandler.reloadAllCustomInventoryGenerators();
        SGInventoryGenerators.registerVanillaInventoryGenerators();
        SGInventoryGenerators.registerModInventoryGenerators();

        StructureSaveHandler.reloadAllCustomStructures();
        SGStructures.registerModStructures();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {

    }

    @EventHandler
    public void onServerStart(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandExportStructure());
        event.registerServerCommand(new CommandEditStructure());
        event.registerServerCommand(new CommandGenerateStructure());
        event.registerServerCommand(new CommandImportStructure());
        event.registerServerCommand(new CommandReloadStructures());
        event.registerServerCommand(new CommandSelectPoint());
        event.registerServerCommand(new CommandSelectFill());
        event.registerServerCommand(new CommandSelectReplace());
        event.registerServerCommand(new CommandSelectFillSphere());
        event.registerServerCommand(new CommandSelectNatural());
    }
}