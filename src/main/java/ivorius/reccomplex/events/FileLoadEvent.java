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

    public FileLoadEvent(S s, String fileSuffix, String id, String domain, Path path)
    {
        this.s = s;
        this.fileSuffix = fileSuffix;
        this.id = id;
        this.domain = domain;
        this.path = path;
    }

    public static class Pre<S> extends FileLoadEvent<S>
    {
        public final boolean originalActive;
        public boolean active;

        public Pre(S s, String fileSuffix, String id, String domain, Path path, boolean active)
        {
            super(s, fileSuffix, id, domain, path);
            this.originalActive = active;
            this.active = active;
        }
    }

    public static class Post<S> extends FileLoadEvent<S>
    {
        public final boolean active;

        public Post(S s, String fileSuffix, String id, String domain, Path path, boolean active)
        {
            super(s, fileSuffix, id, domain, path);
            this.active = active;
        }
    }
}
