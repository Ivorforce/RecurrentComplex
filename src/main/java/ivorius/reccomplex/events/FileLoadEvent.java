/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.events;

import net.minecraftforge.fml.common.eventhandler.Event;

import java.nio.file.Path;

/**
 * Created by lukas on 21.01.15.
 */
@Event.HasResult
public class FileLoadEvent<S> extends Event
{
    public final S s;
    public final String fileSuffix;

    public final Path path;
    public final String id;
    public final String domain;

    public final boolean originalShouldGenerate;

    public FileLoadEvent(S s, String fileSuffix, String id, String domain, Path path, boolean originalShouldGenerate)
    {
        this.s = s;
        this.fileSuffix = fileSuffix;
        this.id = id;
        this.domain = domain;
        this.path = path;
        this.originalShouldGenerate = originalShouldGenerate;
    }

    public static class Pre<S> extends FileLoadEvent<S>
    {
        public boolean shouldGenerate;

        public Pre(S s, String fileSuffix, String id, String domain, Path path, boolean shouldGenerate)
        {
            super(s, fileSuffix, id, domain, path, shouldGenerate);
            this.shouldGenerate = shouldGenerate;
        }
    }

    public static class Post<S> extends FileLoadEvent<S>
    {
        public Post(S s, String fileSuffix, String id, String domain, Path path, boolean originalShouldGenerate)
        {
            super(s, fileSuffix, id, domain, path, originalShouldGenerate);
        }
    }
}
