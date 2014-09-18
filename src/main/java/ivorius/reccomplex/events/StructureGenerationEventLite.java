/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.events;

import cpw.mods.fml.common.eventhandler.Event;

/**
 * Created by lukas on 18.09.14.
 */
public class StructureGenerationEventLite extends Event
{
    /**
     * The name of the structure info to be spawned.
     */
    public final String structureName;

    /**
     * The (lower) coordinates from which the structure spawns.
     */
    public final int[] coordinates;

    /**
     * The (expected) generated size of the structure.
     */
    public final int[] size;

    /**
     * The depth at which the structure spawns. Example: 0 = spawned as such, 1 = spawned within another structure, etc.
     */
    public final int generationLayer;

    public StructureGenerationEventLite(String structureName, int[] coordinates, int[] size, int generationLayer)
    {
        this.structureName = structureName;
        this.coordinates = coordinates;
        this.size = size;
        this.generationLayer = generationLayer;
    }

    public static class Pre extends StructureGenerationEventLite
    {
        public Pre(String structureName, int[] coordinates, int[] size, int generationLayer)
        {
            super(structureName, coordinates, size, generationLayer);
        }
    }

    public static class Post extends StructureGenerationEventLite
    {
        public Post(String structureName, int[] coordinates, int[] size, int generationLayer)
        {
            super(structureName, coordinates, size, generationLayer);
        }
    }
}
