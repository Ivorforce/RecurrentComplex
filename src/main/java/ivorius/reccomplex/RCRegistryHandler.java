/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex;

import ivorius.ivtoolkit.network.*;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.ivtoolkit.tools.NBTCompoundObjectCapabilityStorage;
import ivorius.reccomplex.biome.RCBiomeDictionary;
import ivorius.reccomplex.block.*;
import ivorius.reccomplex.block.legacy.BlockMazeGenerator;
import ivorius.reccomplex.block.legacy.BlockSpawnCommand;
import ivorius.reccomplex.block.legacy.BlockStructureGenerator;
import ivorius.reccomplex.block.materials.MaterialNegativeSpace;
import ivorius.reccomplex.block.materials.RCMaterials;
import ivorius.reccomplex.capability.CapabilitySelection;
import ivorius.reccomplex.capability.RCEntityInfo;
import ivorius.reccomplex.files.RCFileSaver;
import ivorius.reccomplex.files.loading.FileLoaderRegistryString;
import ivorius.reccomplex.files.loading.LeveledRegistry;
import ivorius.reccomplex.files.loading.RCFileSuffix;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.files.saving.FileSaverString;
import ivorius.reccomplex.item.*;
import ivorius.reccomplex.json.SerializableStringTypeRegistry;
import ivorius.reccomplex.network.*;
import ivorius.reccomplex.operation.*;
import ivorius.reccomplex.random.Poem;
import ivorius.reccomplex.utils.FMLUtils;
import ivorius.reccomplex.utils.presets.PresetRegistry;
import ivorius.reccomplex.world.gen.feature.GenerationSanityChecker;
import ivorius.reccomplex.world.gen.feature.RCWorldgenMonitor;
import ivorius.reccomplex.world.gen.feature.selector.NaturalStructureSelector;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.StructureSaveHandler;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.*;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.rules.MazeRuleRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.rules.saved.MazeRuleConnect;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.rules.saved.MazeRuleConnectAll;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.FactorLimit;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.FactorMatch;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.FactorRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.GenericPlacer;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.rays.*;
import ivorius.reccomplex.world.gen.feature.structure.generic.presets.*;
import ivorius.reccomplex.world.gen.feature.structure.generic.transformers.*;
import ivorius.reccomplex.world.gen.script.*;
import ivorius.reccomplex.world.storage.loot.GenericItemCollectionRegistry;
import ivorius.reccomplex.world.storage.loot.ItemCollectionSaveHandler;
import ivorius.reccomplex.world.storage.loot.RCLoot;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;

import static ivorius.reccomplex.RecurrentComplex.*;
import static ivorius.reccomplex.block.RCBlocks.*;
import static ivorius.reccomplex.gui.RCCreativeTabs.tabLoot;
import static ivorius.reccomplex.gui.RCCreativeTabs.tabStructureTools;
import static ivorius.reccomplex.item.RCItems.*;

/**
 * Created by lukas on 18.01.15.
 */
public class RCRegistryHandler
{

    public static void preInit(FMLPreInitializationEvent event, RecurrentComplex mod)
    {
        if (!RecurrentComplex.isLite()) {
            tabStructureTools = new CreativeTabs("structureTools")
            {
                @Override
                public ItemStack getTabIconItem()
                {
                    return new ItemStack(RCItems.blockSelector);
                }
            };
            tabLoot = new CreativeTabs("inventoryGenerators")
            {
                @Override
                public ItemStack getTabIconItem()
                {
                    return new ItemStack(RCItems.lootGenerationTag);
                }
            };
        }

        RCMaterials.materialNegativeSpace = new MaterialNegativeSpace();
        RCMaterials.materialGenericSolid = (new Material(MapColor.STONE));

        CapabilityManager.INSTANCE.register(RCEntityInfo.class, new NBTCompoundObjectCapabilityStorage<>(RCEntityInfo.class), RCEntityInfo::new);
        CapabilityManager.INSTANCE.register(CapabilitySelection.class, new NBTCompoundObjectCapabilityStorage<>(CapabilitySelection.class), CapabilitySelection::new);

        blockSelector = new ItemBlockSelectorBlock().setUnlocalizedName("blockSelector");
        blockSelector.setCreativeTab(tabStructureTools);
        register(blockSelector, "block_selector");
        RecurrentComplex.cremapper.registerLegacyIDs(blockSelector, "blockSelector");

        blockSelectorFloating = new ItemBlockSelectorFloating().setUnlocalizedName("blockSelectorFloating");
        blockSelectorFloating.setCreativeTab(tabStructureTools);
        register(blockSelectorFloating, "block_selector_floating");
        RecurrentComplex.cremapper.registerLegacyIDs(blockSelectorFloating, "blockSelectorFloating");

        lootGenerationTag = (ItemLootGenMultiTag) new ItemLootGenMultiTag().setUnlocalizedName("inventoryGenerationTag");
        lootGenerationTag.setCreativeTab(tabLoot);
        register(lootGenerationTag, "inventory_generation_tag");
        RecurrentComplex.cremapper.registerLegacyIDs(lootGenerationTag, "inventoryGenerationTag");

        lootGenerationSingleTag = (ItemLootGenSingleTag) new ItemLootGenSingleTag().setUnlocalizedName("inventoryGenerationSingleTag");
        lootGenerationSingleTag.setCreativeTab(tabLoot);
        register(lootGenerationSingleTag, "inventory_generation_single_tag");
        RecurrentComplex.cremapper.registerLegacyIDs(lootGenerationSingleTag, "inventoryGenerationSingleTag");

        lootGenerationComponentTag = (ItemLootTableComponentTag) new ItemLootTableComponentTag().setUnlocalizedName("inventoryGenerationComponentTag");
        lootGenerationComponentTag.setCreativeTab(tabLoot);
        register(lootGenerationComponentTag, "inventory_generation_component_tag");

        artifactGenerationTag = new ItemArtifactGenerator().setUnlocalizedName("artifactGenerationTag");
        artifactGenerationTag.setCreativeTab(tabLoot);
        register(artifactGenerationTag, "artifact_generation_tag");
        RecurrentComplex.cremapper.registerLegacyIDs(artifactGenerationTag, "artifactGenerationTag");

        bookGenerationTag = new ItemBookGenerator().setUnlocalizedName("bookGenerationTag");
        bookGenerationTag.setCreativeTab(tabLoot);
        register(bookGenerationTag, "book_generation_tag");
        RecurrentComplex.cremapper.registerLegacyIDs(bookGenerationTag, "bookGenerationTag");

        genericSpace = (BlockGenericSpace) new BlockGenericSpace().setUnlocalizedName("negativeSpace");
        genericSpace.setCreativeTab(tabStructureTools);
        register(genericSpace, "generic_space", new ItemBlockGenericSpace(genericSpace));
        RecurrentComplex.cremapper.registerLegacyIDs(genericSpace, true, "negativeSpace");

        genericSolid = new BlockGenericSolid().setUnlocalizedName("naturalFloor");
        genericSolid.setCreativeTab(tabStructureTools);
        register(genericSolid, "generic_solid", new ItemBlockGenericSolid(genericSolid));
        RecurrentComplex.cremapper.registerLegacyIDs(genericSolid, true, "naturalFloor");

        structureGenerator = new BlockStructureGenerator().setUnlocalizedName("structureGenerator");
        register(structureGenerator, "structure_generator");
        register(BlockStructureGenerator.TileEntityStructureGenerator.class, "RCStructureGenerator", "SGStructureGenerator");
        RecurrentComplex.cremapper.registerLegacyIDs(structureGenerator, true, "structureGenerator");

        mazeGenerator = new BlockMazeGenerator().setUnlocalizedName("mazeGenerator");
        register(mazeGenerator, "maze_generator");
        register(BlockMazeGenerator.TileEntityMazeGenerator.class, "RCMazeGenerator", "SGMazeGenerator");
        RecurrentComplex.cremapper.registerLegacyIDs(mazeGenerator, true, "mazeGenerator");

        spawnCommands = new BlockSpawnCommand().setUnlocalizedName("spawn_command");
        register(spawnCommands, "weighted_command_block");
        register(BlockSpawnCommand.TileEntitySpawnCommand.class, "RCSpawnCommand");
        RecurrentComplex.cremapper.registerLegacyIDs(spawnCommands, true, "spawnCommand");

        spawnScript = new BlockScript().setUnlocalizedName("spawn_script");
        spawnScript.setCreativeTab(tabStructureTools);
        register(spawnScript, "spawn_script");
        register(TileEntityBlockScript.class, "RCSpawnScript");

        inspector = new ItemInspector().setUnlocalizedName("recinspector");
        inspector.setCreativeTab(tabStructureTools);
        register(inspector, "inspector");

        // Set preset defaults
        GenericPlacerPresets.instance().getRegistry().register("clear", MOD_ID, PresetRegistry.fullPreset("clear", new GenericPlacer(), new PresetRegistry.Metadata("Clear", "Do not place anywhere")), true, LeveledRegistry.Level.INTERNAL);
        GenericPlacerPresets.instance().setDefault("clear");

        DimensionMatcherPresets.instance().getRegistry().register("clear", MOD_ID, PresetRegistry.fullPreset("clear", new ArrayList<>(), new PresetRegistry.Metadata("None", "No dimensions")), true, LeveledRegistry.Level.INTERNAL);
        DimensionMatcherPresets.instance().setDefault("clear");

        BiomeMatcherPresets.instance().getRegistry().register("clear", MOD_ID, PresetRegistry.fullPreset("clear", new ArrayList<>(), new PresetRegistry.Metadata("None", "No biomes")), true, LeveledRegistry.Level.INTERNAL);
        BiomeMatcherPresets.instance().setDefault("clear");

        WeightedBlockStatePresets.instance().getRegistry().register("clear", MOD_ID, PresetRegistry.fullPreset("clear", new ArrayList<>(), new PresetRegistry.Metadata("None", "No blocks")), true, LeveledRegistry.Level.INTERNAL);
        WeightedBlockStatePresets.instance().setDefault("clear");

        TransfomerPresets.instance().getRegistry().register("clear", MOD_ID, PresetRegistry.fullPreset("clear", new TransformerMulti.Data(), new PresetRegistry.Metadata("None", "No transformers")), true, LeveledRegistry.Level.INTERNAL);
        TransfomerPresets.instance().setDefault("clear");

        GenerationSanityChecker.init();
    }

    public static void register(Item item, String id)
    {
        item.setRegistryName(id);

        if (!RecurrentComplex.isLite())
            ForgeRegistries.ITEMS.register(item);
        else
            specialRegistry.register(item.getRegistryName(), item);
    }

    public static void register(Block block, String id, ItemBlock item)
    {
        block.setRegistryName(id);
        item.setRegistryName(id);

        if (!RecurrentComplex.isLite()) {
            ForgeRegistries.BLOCKS.register(block);
            ForgeRegistries.ITEMS.register(item);
        }
        else {
            specialRegistry.register(block.getRegistryName(), block);
            specialRegistry.register(item.getRegistryName(), item);
        }
    }

    public static void register(Block block, String id)
    {
        register(block, id, new ItemBlock(block));
    }

    @Deprecated
    public static void register(Block block, String id, Class<? extends ItemBlock> itemClass, Object... itemArgs)
    {
        ItemBlock item = FMLUtils.constructItem(block, itemClass, itemArgs);
        register(block, id, item != null ? item : new ItemBlock(block));
    }

    public static void register(Class<? extends TileEntity> tileEntity, String id, String... alternatives)
    {
        if (!RecurrentComplex.isLite())
            GameRegistry.registerTileEntity(tileEntity, id);
        else
            specialRegistry.register(new ResourceLocation(id), tileEntity);

        // TODO Register alternatives in the game if not lite
        for (String aid : alternatives) specialRegistry.register(new ResourceLocation(aid), tileEntity);
    }

    public static void load(FMLInitializationEvent event, RecurrentComplex mod)
    {
        MCRegistry mcRegistry = RecurrentComplex.specialRegistry;

        registerPackets(event);

        CapabilityUpdateRegistry.INSTANCE.register(RCEntityInfo.CAPABILITY_KEY, RCEntityInfo.CAPABILITY);
        CapabilityUpdateRegistry.INSTANCE.register(CapabilitySelection.CAPABILITY_KEY, CapabilitySelection.CAPABILITY);

        RCBiomeDictionary.registerTypes();

        loader.register(StructureSaveHandler.INSTANCE.new Loader());
        loader.register(new FileLoaderRegistryString<>(RCFileSuffix.INVENTORY_GENERATION_COMPONENT,
                GenericItemCollectionRegistry.INSTANCE, ItemCollectionSaveHandler.INSTANCE::fromJSON));
        loader.register(new FileLoaderRegistryString<>(RCFileSuffix.POEM_THEME,
                Poem.THEME_REGISTRY, Poem.Theme::fromFile));
        loader.register(new FileLoaderRegistryString<>(RCFileSuffix.NATURAL_CATEGORY,
                NaturalStructureSelector.CATEGORY_REGISTRY, NaturalStructureSelector.SimpleCategory.class));
        loader.register(BiomeMatcherPresets.instance().loader());
        loader.register(DimensionMatcherPresets.instance().loader());
        loader.register(WeightedBlockStatePresets.instance().loader());
        loader.register(GenericPlacerPresets.instance().loader());
        loader.register(TransfomerPresets.instance().loader());

        saver.register(StructureSaveHandler.INSTANCE.new Saver(RCFileSaver.STRUCTURE));
        saver.register(new FileSaverString<>(RCFileSaver.INVENTORY_GENERATION_COMPONENT, RCFileSuffix.INVENTORY_GENERATION_COMPONENT,
                GenericItemCollectionRegistry.INSTANCE, ItemCollectionSaveHandler.INSTANCE::toJSON));
        saver.register(new FileSaverString<>(RCFileSaver.NATURAL_GENERATION_CATEGORY, RCFileSuffix.NATURAL_CATEGORY,
                NaturalStructureSelector.CATEGORY_REGISTRY, NaturalStructureSelector.SimpleCategory.class));
        saver.register(BiomeMatcherPresets.instance().saver(RCFileSaver.BIOME_PRESET));
        saver.register(DimensionMatcherPresets.instance().saver(RCFileSaver.DIMENSION_PRESET));
        saver.register(WeightedBlockStatePresets.instance().saver(RCFileSaver.BLOCK_PRESET));
        saver.register(GenericPlacerPresets.instance().saver(RCFileSaver.PLACER_PRESET));
        saver.register(TransfomerPresets.instance().saver(RCFileSaver.TRANSFORMER_PRESET));

        WorldScriptRegistry worldScriptRegistry = WorldScriptRegistry.INSTANCE;
        worldScriptRegistry.register("multi", WorldScriptMulti.class);
        worldScriptRegistry.register("strucGen", WorldScriptStructureGenerator.class);
        worldScriptRegistry.register("mazeGen", WorldScriptMazeGenerator.class);
        worldScriptRegistry.register("command", WorldScriptCommand.class);
        worldScriptRegistry.register("holder", WorldScriptHolder.class);

        SerializableStringTypeRegistry<Transformer> transformerRegistry = StructureRegistry.TRANSFORMERS;
        transformerRegistry.registerType("multi", TransformerMulti.class, new TransformerMulti.Serializer());
        transformerRegistry.registerType("worldscript", TransformerWorldScript.class, new TransformerWorldScript.Serializer(mcRegistry));
        transformerRegistry.registerType("villagereplace", TransformerVillageSpecific.class, new TransformerVillageSpecific.Serializer(mcRegistry));
        transformerRegistry.registerType("natural", TransformerNatural.class, new TransformerNatural.Serializer(mcRegistry));
        transformerRegistry.registerType("naturalAir", TransformerNaturalAir.class, new TransformerNaturalAir.Serializer(mcRegistry));
        transformerRegistry.registerType("pillar", TransformerPillar.class, new TransformerPillar.Serializer(mcRegistry));
        TransformerReplace.Serializer replaceSerializer = new TransformerReplace.Serializer(mcRegistry);
        transformerRegistry.registerType("replaceAll", TransformerReplace.class, replaceSerializer);
        transformerRegistry.registerLegacy("replace", TransformerReplace.class, new TransformerReplace.NonUniformSerializer(replaceSerializer));
        transformerRegistry.registerType("ruins", TransformerRuins.class, new TransformerRuins.Serializer(mcRegistry));
        transformerRegistry.registerType("negativeSpace", TransformerNegativeSpace.class, new TransformerNegativeSpace.Serializer(mcRegistry));
        transformerRegistry.registerType("ensureBlocks", TransformerEnsureBlocks.class, new TransformerEnsureBlocks.Serializer(mcRegistry));
        transformerRegistry.registerType("propertyReplace", TransformerProperty.class, new TransformerProperty.Serializer(mcRegistry));

        SerializableStringTypeRegistry<GenerationType> genInfoRegistry = StructureRegistry.GENERATION_TYPES;
        genInfoRegistry.registerType("natural", NaturalGeneration.class, new NaturalGeneration.Serializer());
        genInfoRegistry.registerType("structureList", ListGeneration.class, new ListGeneration.Serializer());
        genInfoRegistry.registerType("mazeComponent", MazeGeneration.class, new MazeGeneration.Serializer());
        genInfoRegistry.registerType("static", StaticGeneration.class, new StaticGeneration.Serializer());
        genInfoRegistry.registerType("vanilla", VanillaGeneration.class, new VanillaGeneration.Serializer());
        genInfoRegistry.registerType("sapling", SaplingGeneration.class, new SaplingGeneration.Serializer());
        genInfoRegistry.registerType("decoration", VanillaDecorationGeneration.class, new VanillaDecorationGeneration.Serializer());

        StructureRegistry.INSTANCE.registerModule(new NaturalGeneration.Cache());
        StructureRegistry.INSTANCE.registerModule(new VanillaDecorationGeneration.Cache());
        StructureRegistry.INSTANCE.registerModule(new VanillaGeneration.Cache());

        SerializableStringTypeRegistry<GenericPlacer.Factor> placerFactorRegistry = FactorRegistry.INSTANCE.getTypeRegistry();
        placerFactorRegistry.registerType("limit", FactorLimit.class, new FactorLimit.Serializer());
        placerFactorRegistry.registerType("match", FactorMatch.class, new FactorMatch.Serializer());

        SerializableStringTypeRegistry<FactorLimit.Ray> rayRegistry = FactorLimit.getRayRegistry();
        rayRegistry.registerType("dynpos", RayDynamicPosition.class, null);
        rayRegistry.registerType("move", RayMove.class, null);
        rayRegistry.registerType("matcher", RayMatcher.class, new RayMatcher.Serializer());
        rayRegistry.registerType("average", RayAverageMatcher.class, new RayAverageMatcher.Serializer());
        rayRegistry.registerType("dynmove", RayDynamicMove.class, null);

        MazeRuleRegistry mazeRuleRegistry = MazeRuleRegistry.INSTANCE;
        mazeRuleRegistry.register("connect", MazeRuleConnect.class);
        mazeRuleRegistry.register("connectall", MazeRuleConnectAll.class);

        OperationRegistry.register("multi", OperationMulti.class);
        OperationRegistry.register("strucGen", OperationGenerateStructure.class);
        OperationRegistry.register("schemGen", OperationGenerateSchematic.class);
        OperationRegistry.register("clearArea", OperationClearArea.class);

//        GameRegistry.registerWorldGenerator(new WorldGenStructures(), 50);
        RCLoot.registerVanillaLootTables();
//        MapGenStructureIO.func_143031_a(GenericVillagePiece.class, "RcGSP");
//        VillagerRegistry.instance().registerVillageCreationHandler(new GenericVillageCreationHandler("DesertHut"));

        // So it's placeable in furnaces
        GameRegistry.registerFuelHandler(fuel -> fuel.getItem() instanceof GeneratingItem ? 1 : 0);

        RCWorldgenMonitor.create();
    }

    protected static <T> void dumpAll(PresetRegistry<T> presets)
    {
        // TODO Broken
        presets.allIDs().forEach(s -> saver.trySave(ResourceDirectory.ACTIVE.toPath(), presets.getFileSuffix(), s));
    }

    public static void registerPackets(FMLInitializationEvent event)
    {
        registerClientPackets();
        registerServerPackets();
    }

    protected static void registerServerPackets()
    {
        network.registerMessage(PacketGuiActionHandler.class, PacketGuiAction.class, 1, Side.SERVER);
        network.registerMessage(PacketSaveLootTableHandler.class, PacketSaveLootTable.class, 2, Side.SERVER);
        network.registerMessage(PacketEditTileEntityHandler.class, PacketEditTileEntity.class, 5, Side.SERVER);
        network.registerMessage(PacketSaveStructureHandler.class, PacketSaveStructure.class, 7, Side.SERVER);
        network.registerMessage(PacketSyncItemHandler.class, PacketSyncItem.class, 9, Side.SERVER);
        network.registerMessage(PacketItemEventHandler.class, PacketItemEvent.class, 11, Side.SERVER);
        network.registerMessage(PacketInspectBlockHandler.class, PacketInspectBlock.class, 12, Side.SERVER);
        network.registerMessage(PacketOpenGuiHandler.class, PacketOpenGui.class, 15, Side.SERVER);
        network.registerMessage(PacketInspectEntityHandler.class, PacketInspectEntity.class, 17, Side.SERVER);
        network.registerMessage(PacketWorldDataHandler.class, PacketWorldData.class, 20, Side.SERVER);

        network.registerMessage(PacketSpawnTweaksHandler.class, PacketSpawnTweaks.class, 21, Side.SERVER);
    }

    protected static void registerClientPackets()
    {
        network.registerMessage(PacketEntityCapabilityDataHandler.class, PacketEntityCapabilityData.class, 0, Side.CLIENT);
        network.registerMessage(PacketEditLootTableHandler.class, PacketEditLootTable.class, 3, Side.CLIENT);
        network.registerMessage(PacketEditTileEntityHandler.class, PacketEditTileEntity.class, 4, Side.CLIENT);
        network.registerMessage(PacketEditStructureHandler.class, PacketEditStructure.class, 6, Side.CLIENT);
        network.registerMessage(PacketSyncItemHandler.class, PacketSyncItem.class, 8, Side.CLIENT);
        network.registerMessage(PacketItemEventHandler.class, PacketItemEvent.class, 10, Side.CLIENT);
        network.registerMessage(PacketInspectBlockHandler.class, PacketInspectBlock.class, 13, Side.CLIENT);
        network.registerMessage(PacketOpenGuiHandler.class, PacketOpenGui.class, 14, Side.CLIENT);
        network.registerMessage(PacketReopenGuiHandler.class, PacketReopenGui.class, 16, Side.CLIENT);
        network.registerMessage(PacketInspectEntityHandler.class, PacketInspectEntity.class, 18, Side.CLIENT);
        network.registerMessage(PacketWorldDataHandler.class, PacketWorldData.class, 19, Side.CLIENT);

        network.registerMessage(PacketSpawnTweaksHandler.class, PacketSpawnTweaks.class, 22, Side.CLIENT);
    }

}
