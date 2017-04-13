/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.events;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
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
    public final Structure structure;

    /**
     * The context in which the structure will spawn.
     */
    public StructureSpawnContext spawnContext;

    public StructureGenerationEvent(Structure structure, StructureSpawnContext spawnContext)
    {
        super(spawnContext.environment.world);
        this.structure = structure;
        this.spawnContext = spawnContext;
    }

    @Cancelable
    public static class Suggest extends StructureGenerationEvent
    {
        public Suggest(Structure structure, StructureSpawnContext spawnContext)
        {
            super(structure, spawnContext);
        }
    }

    public static class Pre extends StructureGenerationEvent
    {
        public Pre(Structure structure, StructureSpawnContext spawnContext)
        {
            super(structure, spawnContext);
        }
    }

    public static class Post extends StructureGenerationEvent
    {
        public Post(Structure structure, StructureSpawnContext spawnContext)
        {
            super(structure, spawnContext);
        }
    }
}
