/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.files.loading;

/**
 * Created by lukas on 18.09.15.
 */
public class FileLoadContext
{
    public final String domain;
    public final boolean active;
    public final LeveledRegistry.Level level;

    public FileLoadContext(String domain, boolean active, LeveledRegistry.Level level)
    {
        this.domain = domain;
        this.active = active;
        this.level = level;
    }

}
