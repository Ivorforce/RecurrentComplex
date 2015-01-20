/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.events;

import ivorius.reccomplex.worldgen.StructureInfo;
import ivorius.reccomplex.worldgen.StructureSpawnContext;
import net.minecraft.world.World;
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
        super(spawnContext.world);
        this.structureInfo = structureInfo;
        this.spawnContext = spawnContext;
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
