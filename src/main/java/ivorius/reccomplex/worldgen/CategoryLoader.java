/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.files.FileLoadContext;
import ivorius.reccomplex.files.FileTypeHandler;
import ivorius.reccomplex.worldgen.selector.NaturalStructureSelector;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by lukas on 29.09.15.
 */
public class CategoryLoader implements FileTypeHandler
{
    public static final String FILE_SUFFIX = "rcnc";

    private Gson gson = createGson();

    public static Gson createGson()
    {
        return new GsonBuilder().create();
    }

    @Override
    public boolean loadFile(Path path, FileLoadContext context)
    {
        NaturalStructureSelector.SimpleCategory category = null;
        String name = context.customID != null ? context.customID : FilenameUtils.getBaseName(path.getFileName().toString());

        try
        {
            category = read(new String(Files.readAllBytes(path)));
        }
        catch (IOException e)
        {
            RecurrentComplex.logger.warn("Error reading natural spawn category", e);
        }

        if (category != null)
        {
            NaturalStructureSelector.registerCategory(name, category, context.custom);

            return true;
        }

        return false;
    }

    @Override
    public void clearCustomFiles()
    {
        NaturalStructureSelector.clearCustom();
    }

    public NaturalStructureSelector.SimpleCategory read(String file)
    {
        return gson.fromJson(file, NaturalStructureSelector.SimpleCategory.class);
    }
}
