/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.events;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import ivorius.reccomplex.world.gen.feature.structure.StructureInfo;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;
import net.minecraftforge.event.world.WorldEvent;

/**
 * Created by lukas on 18.09.14.
 */
public class StructureGenerationEvent extends WorldEvent
{
    /**
     * The structure info to be spawned.
     */
    public final StructureInfo structureInfo;

    /**
     * The context in which the structure will spawn.
     */
    public StructureSpawnContext spawnContext;

    public StructureGenerationEvent(StructureInfo structureInfo, StructureSpawnContext spawnContext)
    {
        super(spawnContext.environment.world);
        this.structureInfo = structureInfo;
        this.spawnContext = spawnContext;
    }

    @Cancelable
    public static class Suggest extends StructureGenerationEvent
    {
        public Suggest(StructureInfo structureInfo, StructureSpawnContext spawnContext)
        {
            super(structureInfo, spawnContext);
        }
    }

    public static class Pre extends StructureGenerationEvent
    {
        public Pre(StructureInfo structureInfo, StructureSpawnContext spawnContext)
        {
            super(structureInfo, spawnContext);
        }
    }

    public static class Post extends StructureGenerationEvent
    {
        public Post(StructureInfo structureInfo, StructureSpawnContext spawnContext)
        {
            super(structureInfo, spawnContext);
        }
    }
}
