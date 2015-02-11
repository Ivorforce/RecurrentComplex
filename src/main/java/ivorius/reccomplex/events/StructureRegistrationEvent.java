/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.events;

import cpw.mods.fml.common.eventhandler.Event;
import ivorius.reccomplex.structures.StructureInfo;

/**
 * Created by lukas on 21.01.15.
 */
@Event.HasResult
public class StructureRegistrationEvent extends Event
{
    public final String structureID;
    public final StructureInfo structureInfo;
    public final boolean originalShouldGenerate;

    public StructureRegistrationEvent(String structureID, StructureInfo structureInfo, boolean originalShouldGenerate)
    {
        this.structureID = structureID;
        this.structureInfo = structureInfo;
        this.originalShouldGenerate = originalShouldGenerate;
    }

    public static class Pre extends StructureRegistrationEvent
    {
        public boolean shouldGenerate;

        public Pre(String structureID, StructureInfo structureInfo, boolean originalShouldGenerate)
        {
            super(structureID, structureInfo, originalShouldGenerate);
            this.shouldGenerate = originalShouldGenerate;
        }
    }

    public static class Post extends StructureRegistrationEvent
    {
        public Post(String structureID, StructureInfo structureInfo, boolean originalShouldGenerate)
        {
            super(structureID, structureInfo, originalShouldGenerate);
        }
    }
}
