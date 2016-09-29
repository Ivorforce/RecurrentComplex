/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.files;

/**
 * Created by lukas on 18.09.15.
 */
public class FileLoadContext
{
    public final String domain;
    public final boolean active;
    public final boolean custom;

    public FileLoadContext(String domain, boolean active, boolean custom)
    {
        this.domain = domain;
        this.active = active;
        this.custom = custom;
    }
}
