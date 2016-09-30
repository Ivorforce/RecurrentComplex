/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.files;

import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.file.Path;

/**
 * Created by lukas on 18.09.15.
 */
public abstract class FileTypeHandler
{
    protected String suffix;

    public FileTypeHandler(String suffix)
    {
        this.suffix = suffix;
    }

    public String getSuffix()
    {
        return suffix;
    }

    public void setSuffix(String suffix)
    {
        this.suffix = suffix;
    }

    @ParametersAreNonnullByDefault
    abstract boolean loadFile(Path path, String id, FileLoadContext context) throws Exception;

    @ParametersAreNonnullByDefault
    abstract void writeFile(Path path, String id) throws Exception;

    @ParametersAreNonnullByDefault
    abstract void clearFiles(LeveledRegistry.Level level);
}
