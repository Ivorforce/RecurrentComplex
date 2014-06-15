package ivorius.structuregen.worldgen.genericStructures;

import ivorius.structuregen.StructureGen;
import ivorius.structuregen.worldgen.StructureHandler;
import net.minecraft.util.ResourceLocation;

/**
 * Created by lukas on 07.06.14.
 */
public class SGStructures
{
    public static void registerModStructures()
    {
        StructureHandler.registerStructures(StructureGen.MODID, true, "MeteorSite",
                "PirateChest", "OldWatchtower", "JokerTower", "DesertBeacon", "ForestBeacon", "DesertWatchtower", "PeacefulCrypt", "SmallAbandonedMine",
                "SmallWoodenCottage", "SmallWoodenCottage1", "SmallWoodenCottage2", "DesertHut",
                "ShrineSmallAir", "ShrineSmallEarth", "ShrineSmallFire", "ShrineSmallWater",
                "MysticalTree", "MysticalTree1", "MysticalTree2");
//        StructureHandler.registerStructures(StructureGen.MODID, false);
    }
}
