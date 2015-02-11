/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import ivorius.reccomplex.blocks.*;
import ivorius.reccomplex.client.rendering.RCBlockRendering;
import ivorius.reccomplex.items.*;
import ivorius.reccomplex.blocks.materials.MaterialNegativeSpace;
import ivorius.reccomplex.blocks.materials.RCMaterials;
import ivorius.reccomplex.operation.OperationRegistry;
import ivorius.reccomplex.random.Poem;
import ivorius.reccomplex.schematics.OperationGenerateSchematic;
import ivorius.reccomplex.schematics.OperationGenerateStructure;
import ivorius.reccomplex.worldgen.StructureRegistry;
import ivorius.reccomplex.worldgen.StructureSelector;
import ivorius.reccomplex.worldgen.blockTransformers.*;
import ivorius.reccomplex.worldgen.inventory.RCInventoryGenerators;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import java.util.Collections;

import static ivorius.reccomplex.RecurrentComplex.*;
import static ivorius.reccomplex.blocks.RCBlocks.*;
import static ivorius.reccomplex.gui.RCCreativeTabs.*;
import static ivorius.reccomplex.items.RCItems.*;

/**
 * Created by lukas on 18.01.15.
 */
public class RCRegistryHandler
{
    public static void preInit(FMLPreInitializationEvent event, RecurrentComplex mod)
    {
        tabStructureTools = new CreativeTabs("structureTools")
        {
            @Override
            public Item getTabIconItem()
            {
                return RCItems.blockSelector;
            }
        };
        tabInventoryGenerators = new CreativeTabs("inventoryGenerators")
        {
            @Override
            public Item getTabIconItem()
            {
                return RCItems.inventoryGenerationTag;
            }
        };

        RCMaterials.materialNegativeSpace = new MaterialNegativeSpace();
        RCMaterials.materialGenericSolid = (new Material(MapColor.stoneColor));

        blockSelector = new ItemBlockSelector().setUnlocalizedName("blockSelector").setTextureName(textureBase + "blockSelector");
        blockSelector.setCreativeTab(tabStructureTools);
        GameRegistry.registerItem(blockSelector, "blockSelector", MODID);

        blockSelectorFloating = new ItemBlockSelectorFloating(2.0f).setUnlocalizedName("blockSelectorFloating").setTextureName(textureBase + "blockSelectorFloating");
        blockSelectorFloating.setCreativeTab(tabStructureTools);
        GameRegistry.registerItem(blockSelectorFloating, "blockSelectorFloating", MODID);

        inventoryGenerationTag = (ItemInventoryGenMultiTag) new ItemInventoryGenMultiTag().setUnlocalizedName("inventoryGenerationTag").setTextureName(textureBase + "inventoryGenerationTag");
        inventoryGenerationTag.setCreativeTab(tabInventoryGenerators);
        GameRegistry.registerItem(inventoryGenerationTag, "inventoryGenerationTag", MODID);

        inventoryGenerationSingleTag = (ItemInventoryGenSingleTag) new ItemInventoryGenSingleTag().setUnlocalizedName("inventoryGenerationSingleTag").setTextureName(textureBase + "inventoryGenerationSingleTag");
        inventoryGenerationSingleTag.setCreativeTab(tabInventoryGenerators);
        GameRegistry.registerItem(inventoryGenerationSingleTag, "inventoryGenerationSingleTag", MODID);

        inventoryGenerationComponentTag = (ItemInventoryGenComponentTag) new ItemInventoryGenComponentTag().setUnlocalizedName("inventoryGenerationComponentTag").setTextureName(textureBase + "inventoryGenerationComponentTag");
        inventoryGenerationComponentTag.setCreativeTab(tabInventoryGenerators);
        GameRegistry.registerItem(inventoryGenerationComponentTag, "inventory_generation_component_tag", MODID);

        artifactGenerationTag = new ItemArtifactGenerator().setUnlocalizedName("artifactGenerationTag").setTextureName(textureBase + "artifactGenerationTag");
        artifactGenerationTag.setCreativeTab(tabInventoryGenerators);
        GameRegistry.registerItem(artifactGenerationTag, "artifactGenerationTag", MODID);

        bookGenerationTag = new ItemBookGenerator().setUnlocalizedName("bookGenerationTag").setTextureName(textureBase + "bookGenerationTag");
        bookGenerationTag.setCreativeTab(tabInventoryGenerators);
        GameRegistry.registerItem(bookGenerationTag, "bookGenerationTag", MODID);

        negativeSpace = new BlockNegativeSpace().setBlockName("negativeSpace").setBlockTextureName(textureBase + "negativeSpace");
        negativeSpace.setCreativeTab(tabStructureTools);
        GameRegistry.registerBlock(negativeSpace, ItemBlockNegativeSpace.class, "negativeSpace");

        naturalFloor = new BlockNaturalFloor().setBlockName("naturalFloor").setBlockTextureName(textureBase + "naturalFloor");
        naturalFloor.setCreativeTab(tabStructureTools);
        GameRegistry.registerBlock(naturalFloor, ItemBlockGenericSolid.class, "naturalFloor");

        structureGenerator = new BlockStructureGenerator().setBlockName("structureGenerator").setBlockTextureName(textureBase + "structureGenerator");
        structureGenerator.setCreativeTab(tabStructureTools);
        GameRegistry.registerBlock(structureGenerator, ItemStructureGenerator.class, "structureGenerator");
        GameRegistry.registerTileEntityWithAlternatives(TileEntityStructureGenerator.class, "RCStructureGenerator", "SGStructureGenerator");

        mazeGenerator = new BlockMazeGenerator().setBlockName("mazeGenerator").setBlockTextureName(textureBase + "mazeGenerator");
        mazeGenerator.setCreativeTab(tabStructureTools);
        GameRegistry.registerBlock(mazeGenerator, ItemMazeGenerator.class, "mazeGenerator");
        GameRegistry.registerTileEntityWithAlternatives(TileEntityMazeGenerator.class, "RCMazeGenerator", "SGMazeGenerator");

        spawnCommands = new BlockSpawnCommand().setBlockName("spawnCommand").setBlockTextureName(textureBase + "spawnCommand");
        spawnCommands.setCreativeTab(tabStructureTools);
        GameRegistry.registerBlock(spawnCommands, ItemMazeGenerator.class, "weighted_command_block");
        GameRegistry.registerTileEntityWithAlternatives(TileEntitySpawnCommand.class, "RCSpawnCommand");
    }

    public static void load(FMLInitializationEvent event, RecurrentComplex mod)
    {
        StructureRegistry.registerBlockTransformer("natural", BlockTransformerNatural.class, new BTProviderNatural());
        StructureRegistry.registerBlockTransformer("naturalAir", BlockTransformerNaturalAir.class, new BTProviderNaturalAir());
        StructureRegistry.registerBlockTransformer("pillar", BlockTransformerPillar.class, new BTProviderPillar());
        StructureRegistry.registerBlockTransformer("replaceAll", BlockTransformerReplaceAll.class, new BTProviderReplaceAll());
        StructureRegistry.registerBlockTransformer("replace", BlockTransformerReplace.class, new BTProviderReplace());
        StructureRegistry.registerBlockTransformer("ruins", BlockTransformerRuins.class, new BTProviderRuins());
        StructureRegistry.registerBlockTransformer("negativeSpace", BlockTransformerNegativeSpace.class, new BTProviderNegativeSpace());

        StructureSelector.registerCategory("decoration", new StructureSelector.SimpleCategory(1.0f / 25.0f, Collections.<StructureSelector.GenerationInfo>emptyList(), true));
        StructureSelector.registerCategory("adventure", new StructureSelector.SimpleCategory(1.0f / 250.0f, Collections.<StructureSelector.GenerationInfo>emptyList(), true));
        StructureSelector.registerCategory("rare", new StructureSelector.SimpleCategory(1.0f / 1250.0f, Collections.<StructureSelector.GenerationInfo>emptyList(), true));

        OperationRegistry.register("strucGen", OperationGenerateStructure.class);
        OperationRegistry.register("schemGen", OperationGenerateSchematic.class);

        Poem.registerThemes(MODID, "love", "summer", "war", "winter", "grief");

//        GameRegistry.registerWorldGenerator(new WorldGenStructures(), 50);
        RCInventoryGenerators.registerVanillaInventoryGenerators();
//        MapGenStructureIO.func_143031_a(GenericVillagePiece.class, "RcGSP");
//        VillagerRegistry.instance().registerVillageCreationHandler(new GenericVillageCreationHandler("DesertHut"));

        RCBlockRendering.negativeSpaceRenderID = RenderingRegistry.getNextAvailableRenderId();
    }
}
