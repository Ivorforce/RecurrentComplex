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
import ivorius.reccomplex.dimensions.DimensionDictionary;
import ivorius.reccomplex.items.*;
import ivorius.reccomplex.blocks.materials.MaterialNegativeSpace;
import ivorius.reccomplex.blocks.materials.RCMaterials;
import ivorius.reccomplex.json.SerializableStringTypeRegistry;
import ivorius.reccomplex.operation.OperationRegistry;
import ivorius.reccomplex.random.Poem;
import ivorius.reccomplex.structures.MCRegistrySpecial;
import ivorius.reccomplex.structures.OperationMoveStructure;
import ivorius.reccomplex.structures.generic.BiomeGenerationInfo;
import ivorius.reccomplex.structures.generic.DimensionGenerationInfo;
import ivorius.reccomplex.structures.generic.WeightedBlockState;
import ivorius.reccomplex.structures.generic.blocktransformers.*;
import ivorius.reccomplex.structures.generic.gentypes.*;
import ivorius.reccomplex.structures.generic.matchers.BiomeMatcher;
import ivorius.reccomplex.structures.generic.matchers.BlockMatcher;
import ivorius.reccomplex.structures.generic.matchers.DimensionMatcher;
import ivorius.reccomplex.structures.generic.presets.BiomeMatcherPresets;
import ivorius.reccomplex.structures.generic.presets.DimensionMatcherPresets;
import ivorius.reccomplex.structures.generic.presets.WeightedBlockStatePresets;
import ivorius.reccomplex.structures.schematics.OperationGenerateSchematic;
import ivorius.reccomplex.structures.OperationGenerateStructure;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.worldgen.StructureSelector;
import ivorius.reccomplex.worldgen.inventory.RCInventoryGenerators;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraftforge.common.BiomeDictionary;

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

        blockSelector = new ItemBlockSelectorBlock().setUnlocalizedName("blockSelector").setTextureName(textureBase + "blockSelector");
        blockSelector.setCreativeTab(tabStructureTools);
        GameRegistry.registerItem(blockSelector, "blockSelector", MODID);

        blockSelectorFloating = new ItemBlockSelectorFloating().setUnlocalizedName("blockSelectorFloating").setTextureName(textureBase + "blockSelectorFloating");
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

        // Register early to allow proper loading
        registerDimensionPresets();
        registerBiomePresets();
        registerBlockStatePresets();
    }

    public static void load(FMLInitializationEvent event, RecurrentComplex mod)
    {
        MCRegistrySpecial mcRegistry = MCRegistrySpecial.INSTANCE;

        SerializableStringTypeRegistry<BlockTransformer> transformerRegistry = StructureRegistry.getBlockTransformerRegistry();
        transformerRegistry.registerType("natural", BlockTransformerNatural.class, new BlockTransformerNatural.Serializer(mcRegistry));
        transformerRegistry.registerType("naturalAir", BlockTransformerNaturalAir.class, new BlockTransformerNaturalAir.Serializer(mcRegistry));
        transformerRegistry.registerType("pillar", BlockTransformerPillar.class, new BlockTransformerPillar.Serializer(mcRegistry));
        transformerRegistry.registerType("replaceAll", BlockTransformerReplaceAll.class, new BlockTransformerReplaceAll.Serializer(mcRegistry));
        transformerRegistry.registerType("replace", BlockTransformerReplace.class, new BlockTransformerReplace.Serializer(mcRegistry));
        transformerRegistry.registerType("ruins", BlockTransformerRuins.class, new BlockTransformerRuins.Serializer(mcRegistry));
        transformerRegistry.registerType("negativeSpace", BlockTransformerNegativeSpace.class, new BlockTransformerNegativeSpace.Serializer(mcRegistry));

        SerializableStringTypeRegistry<StructureGenerationInfo> genInfoRegistry = StructureRegistry.getStructureGenerationInfoRegistry();
        genInfoRegistry.registerType("natural", NaturalGenerationInfo.class, new NaturalGenerationInfo.Serializer());
        genInfoRegistry.registerType("mazeComponent", MazeGenerationInfo.class, new MazeGenerationInfo.Serializer());
        genInfoRegistry.registerType("static", StaticGenerationInfo.class, new StaticGenerationInfo.Serializer());
//        genInfoRegistry.registerType("vanilla", VanillaStructureSpawnInfo.class, new VanillaStructureSpawnInfo.Serializer());

        StructureSelector.registerCategory("frequent", new StructureSelector.SimpleCategory(1.0f / 10.0f, Collections.<StructureSelector.GenerationInfo>emptyList(), true));
        StructureSelector.registerCategory("decoration", new StructureSelector.SimpleCategory(1.0f / 30.0f, Collections.<StructureSelector.GenerationInfo>emptyList(), true));
        StructureSelector.registerCategory("rare", new StructureSelector.SimpleCategory(1.0f / 1250.0f, Collections.<StructureSelector.GenerationInfo>emptyList(), true));
        StructureSelector.registerCategory("ultrarare", new StructureSelector.SimpleCategory(1.0f / 5000.0f, Collections.<StructureSelector.GenerationInfo>emptyList(), true));
        StructureSelector.registerCategory("adventure", new StructureSelector.SimpleCategory(1.0f / 250.0f, Collections.<StructureSelector.GenerationInfo>emptyList(), true));

        OperationRegistry.register("strucGen", OperationGenerateStructure.class);
        OperationRegistry.register("schemGen", OperationGenerateSchematic.class);
        OperationRegistry.register("strucMove", OperationMoveStructure.class);

        Poem.registerThemes(MODID, "love", "summer", "war", "winter", "grief");

//        GameRegistry.registerWorldGenerator(new WorldGenStructures(), 50);
        RCInventoryGenerators.registerVanillaInventoryGenerators();
//        MapGenStructureIO.func_143031_a(GenericVillagePiece.class, "RcGSP");
//        VillagerRegistry.instance().registerVillageCreationHandler(new GenericVillageCreationHandler("DesertHut"));

        RCBlockRendering.negativeSpaceRenderID = RenderingRegistry.getNextAvailableRenderId();
    }

    protected static void registerDimensionPresets()
    {
        DimensionMatcherPresets.instance().register("clear");

        DimensionMatcherPresets.instance().register("overworld",
                new DimensionGenerationInfo(DimensionMatcher.ofTypes(DimensionDictionary.UNCATEGORIZED), null),
                new DimensionGenerationInfo(DimensionMatcher.ofTypes(DimensionDictionary.NO_TOP_LIMIT, DimensionDictionary.BOTTOM_LIMIT, DimensionDictionary.INFINITE), null)
        );
        DimensionMatcherPresets.instance().setDefault("overworld");

        DimensionMatcherPresets.instance().register("nether",
                new DimensionGenerationInfo(DimensionMatcher.ofTypes(DimensionDictionary.HELL, DimensionDictionary.TOP_LIMIT, DimensionDictionary.BOTTOM_LIMIT), null)
        );

        DimensionMatcherPresets.instance().register("end",
                new DimensionGenerationInfo(DimensionMatcher.ofTypes(DimensionDictionary.ENDER, DimensionDictionary.NO_TOP_LIMIT, DimensionDictionary.NO_BOTTOM_LIMIT), null)
        );
    }

    protected static void registerBiomePresets()
    {
        BiomeMatcherPresets.instance().register("clear");

        BiomeMatcherPresets.instance().register("overworld",
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.WATER), 0.0),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.PLAINS), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.FOREST), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.MOUNTAIN), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.HILLS), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.SWAMP), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.SANDY), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.MESA), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.SAVANNA), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.WASTELAND), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.MUSHROOM), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.JUNGLE), null));
        BiomeMatcherPresets.instance().setDefault("overworld");

        BiomeMatcherPresets.instance().register("underground",
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.PLAINS), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.FOREST), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.MOUNTAIN), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.HILLS), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.SWAMP), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.SANDY), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.MESA), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.SAVANNA), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.RIVER), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.OCEAN), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.WASTELAND), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.MUSHROOM), null),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.JUNGLE), null));

        BiomeMatcherPresets.instance().register("ocean",
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.OCEAN, BiomeDictionary.Type.SNOWY), 0.0),
                new BiomeGenerationInfo(BiomeMatcher.ofTypes(BiomeDictionary.Type.OCEAN), null));
    }

    protected static void registerBlockStatePresets()
    {
        WeightedBlockStatePresets.instance().register("clear");

        WeightedBlockState[] wools = new WeightedBlockState[16];
        for (int i = 0; i < wools.length; i++)
            wools[i] = new WeightedBlockState(null, Blocks.wool, i, "");
        WeightedBlockStatePresets.instance().register("allWool", wools);
        WeightedBlockStatePresets.instance().setDefault("allWool");
    }
}
