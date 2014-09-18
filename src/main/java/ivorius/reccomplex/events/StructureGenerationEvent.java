/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.events;

import cpw.mods.fml.common.eventhandler.Event;
import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.worldgen.StructureInfo;

/**
 * Created by lukas on 18.09.14.
 */
public class StructureGenerationEvent extends Event
{
    /**
     * The structure info to be spawned.
     */
    public final StructureInfo structureInfo;

    /**
     * The transform that affects the structure generation.
     */
    public final AxisAlignedTransform2D transform;

    /**
     * The (expected) generated area.
     */
    public final BlockArea area;

    /**
     * The depth at which the structure spawns. Example: 0 = spawned as such, 1 = spawned within another structure, etc.
     */
    public final int generationLayer;

    public StructureGenerationEvent(StructureInfo structureInfo, AxisAlignedTransform2D transform, BlockArea area, int generationLayer)
    {
        this.structureInfo = structureInfo;
        this.transform = transform;
        this.area = area;
        this.generationLayer = generationLayer;
    }

    public static class Pre extends StructureGenerationEvent
    {
        public Pre(StructureInfo structureInfo, AxisAlignedTransform2D transform, BlockArea area, int generationLayer)
        {
            super(structureInfo, transform, area, generationLayer);
        }
    }

    public static class Post extends StructureGenerationEvent
    {
        public Post(StructureInfo structureInfo, AxisAlignedTransform2D transform, BlockArea area, int generationLayer)
        {
            super(structureInfo, transform, area, generationLayer);
        }
    }
}
