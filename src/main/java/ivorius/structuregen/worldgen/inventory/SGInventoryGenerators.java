package ivorius.structuregen.worldgen.inventory;

import ivorius.structuregen.StructureGen;
import net.minecraftforge.common.ChestGenHooks;

/**
 * Created by lukas on 07.06.14.
 */
public class SGInventoryGenerators
{
    public static void registerModInventoryGenerators()
    {
        InventoryGenerationHandler.registerInventoryHandlers(StructureGen.MODID, "AbandonedChest", "PirateChest", "PeacefulCrypt");
        InventoryGenerationHandler.registerInventoryHandlers(StructureGen.MODID, "AirShrine", "EarthShrine", "FireShrine", "UnderwaterShrine");
    }

    public static void registerVanillaInventoryGenerators()
    {
        registerVanillaInventoryGenerators(ChestGenHooks.MINESHAFT_CORRIDOR, ChestGenHooks.PYRAMID_DESERT_CHEST, ChestGenHooks.PYRAMID_JUNGLE_CHEST, ChestGenHooks.PYRAMID_JUNGLE_DISPENSER, ChestGenHooks.STRONGHOLD_CORRIDOR, ChestGenHooks.STRONGHOLD_LIBRARY, ChestGenHooks.STRONGHOLD_CROSSING, ChestGenHooks.VILLAGE_BLACKSMITH, ChestGenHooks.BONUS_CHEST, ChestGenHooks.DUNGEON_CHEST);
    }

    private static void registerVanillaInventoryGenerators(String... keys)
    {
        for (String key : keys)
        {
            InventoryGenerationHandler.registerInventoryGenerator(new VanillaInventoryGenerator(key), key);
        }
    }
}
