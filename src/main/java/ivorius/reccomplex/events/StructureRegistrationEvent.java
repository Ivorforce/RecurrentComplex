/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.events;

import net.minecraftforge.fml.common.eventhandler.Event;
import ivorius.reccomplex.structures.StructureInfo;

import java.nio.file.Path;

/**
 * Created by lukas on 21.01.15.
 */
@Event.HasResult
public class StructureRegistrationEvent extends Event
{
    public final StructureInfo structureInfo;
    public final String structureID;
    public final String domain;
    public final Path path;
    public final boolean originalShouldGenerate;

    public StructureRegistrationEvent(StructureInfo structureInfo, String structureID, String domain, Path path, boolean originalShouldGenerate)
    {
        this.structureInfo = structureInfo;
        this.structureID = structureID;
        this.domain = domain;
        this.path = path;
        this.originalShouldGenerate = originalShouldGenerate;
    }

    public static class Pre extends StructureRegistrationEvent
    {
        public boolean shouldGenerate;

        public Pre(StructureInfo structureInfo, String structureID, String domain, Path path, boolean shouldGenerate)
        {
            super(structureInfo, structureID, domain, path, shouldGenerate);
            this.shouldGenerate = shouldGenerate;
        }
    }

    public static class Post extends StructureRegistrationEvent
    {
        public Post(StructureInfo structureInfo, String structureID, String domain, Path path, boolean originalShouldGenerate)
        {
            super(structureInfo, structureID, domain, path, originalShouldGenerate);
        }
    }
}
