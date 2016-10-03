/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.files.loading;

import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.file.Path;

/**
 * Created by lukas on 18.09.15.
 */
public abstract class FileLoaderAdapter
{
    protected String suffix;

    public FileLoaderAdapter(String suffix)
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
    protected abstract boolean loadFile(Path path, String id, FileLoadContext context) throws Exception;

    @ParametersAreNonnullByDefault
    abstract void clearFiles(LeveledRegistry.Level level);
}
