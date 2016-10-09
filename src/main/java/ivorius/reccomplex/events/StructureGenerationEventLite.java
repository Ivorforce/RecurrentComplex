/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.events;

import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;

/**
 * Created by lukas on 18.09.14.
 */
public class StructureGenerationEventLite extends WorldEvent
{
    /**
     * The name of the structure info to be spawned.
     */
    protected final String structureName;

    /**
     * The bounding box of the structure.
     */
    protected final StructureBoundingBox boundingBox;

    /**
     * The depth at which the structure spawns. Example: 0 = spawned as such, 1 = spawned within another structure, etc.
     */
    protected final int generationLayer;

    protected final boolean firstTime;

    public StructureGenerationEventLite(World world, String structureName, StructureBoundingBox boundingBox, int generationLayer, boolean firstTime)
    {
        super(world);
        this.structureName = structureName;
        this.boundingBox = boundingBox;
        this.generationLayer = generationLayer;
        this.firstTime = firstTime;
    }

    public String getStructureName()
    {
        return structureName;
    }

    public StructureBoundingBox getBoundingBox()
    {
        return boundingBox;
    }

    public int getGenerationLayer()
    {
        return generationLayer;
    }

    public boolean isFirstTime()
    {
        return firstTime;
    }

    @Cancelable
    public static class Suggest extends StructureGenerationEventLite
    {
        public Suggest(World world, String structureName, StructureBoundingBox boundingBox, int generationLayer, boolean firstTime)
        {
            super(world, structureName, boundingBox, generationLayer, firstTime);
        }
    }

    public static class Pre extends StructureGenerationEventLite
    {
        public Pre(World world, String structureName, StructureBoundingBox boundingBox, int generationLayer, boolean firstTime)
        {
            super(world, structureName, boundingBox, generationLayer, firstTime);
        }
    }

    public static class Post extends StructureGenerationEventLite
    {
        public Post(World world, String structureName, StructureBoundingBox boundingBox, int generationLayer, boolean firstTime)
        {
            super(world, structureName, boundingBox, generationLayer, firstTime);
        }
    }
}
