/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.random;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.files.FileLoadContext;
import ivorius.reccomplex.files.FileTypeHandler;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukas on 28.09.15.
 */
public class PoemLoader implements FileTypeHandler
{
    public static final String FILE_SUFFIX = "rcpt";

    @Override
    public boolean loadFile(Path path, FileLoadContext context)
    {
        Poem.Theme theme = null;
        String name = context.customID != null ? context.customID : FilenameUtils.getBaseName(path.getFileName().toString());

        try
        {
            theme = Poem.Theme.fromFile(new String(Files.readAllBytes(path)));
        }
        catch (IOException e)
        {
            RecurrentComplex.logger.warn("Error reading poem theme", e);
        }

        if (theme != null)
        {
            Poem.registerTheme(name, theme, context.custom);

            return true;
        }

        return false;
    }

    @Override
    public void clearCustomFiles()
    {
        Poem.clearCustom();
    }
}
