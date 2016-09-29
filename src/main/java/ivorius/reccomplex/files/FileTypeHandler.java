/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.files;

import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nullable;
import java.nio.file.Path;

/**
 * Created by lukas on 18.09.15.
 */
public interface FileTypeHandler
{
    static String defaultName(Path path, String customID)
    {
        return customID != null ? customID : FilenameUtils.getBaseName(path.getFileName().toString());
    }

    boolean loadFile(Path path, @Nullable String customID, FileLoadContext context) throws Exception;

    void clearFiles(LeveledRegistry.Level level);
}
