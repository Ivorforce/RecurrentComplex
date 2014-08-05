/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.genericStructures;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.worldgen.StructureHandler;

/**
 * Created by lukas on 07.06.14.
 */
public class RCStructures
{
    public static void generateDefaultStructures(boolean generateDefaultStructures)
    {
        StructureHandler.registerStructures(RecurrentComplex.MODID, false,
                "StoneMaze3Way", "StoneMazeCorridor", "StoneMazeCrossing", "StoneMazeEnd", "StoneMazeEndChest", "StoneMazeLitRoom", "StoneMazeSpiderRoom", "StoneMazeTurn",
                "ForestMaze3Way", "ForestMazeClearing", "ForestMazeCorridor", "ForestMazeCrossing", "ForestMazeEnd", "ForestMazeSlimes", "ForestMazeTurn"
        );
        StructureHandler.registerStructures(RecurrentComplex.MODID, generateDefaultStructures,
                "MeteorSite",
                "DesertFortress",
                "PirateChest", "OldWatchtower", "JokerTower", "DesertBeacon", "ForestBeacon", "DesertWatchtower", "PeacefulCrypt", "SmallAbandonedMine",
                "SmallWoodenCottage", "SmallWoodenCottage1", "SmallWoodenCottage2", "DesertHut",
                "ShrineSmallAir", "ShrineSmallEarth", "ShrineSmallFire", "ShrineSmallWater",
                "MysticalTree", "MysticalTree1", "MysticalTree2",
                "StoneMaze", "ForestMaze",
                "DinosaurSkeleton",
                "SmallClockworkSite", "BigClockworkSite"
        );
    }
}
