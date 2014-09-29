/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.schematics;

import ivorius.ivtoolkit.tools.IvFileHelper;
import ivorius.reccomplex.RecurrentComplex;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by lukas on 29.09.14.
 */
public class SchematicLoader
{
    public static void initializeFolder()
    {
        getValidatedSchematicsFile();
    }

    public static SchematicFile loadSchematicByName(String name) throws SchematicFile.UnsupportedSchematicFormatException
    {
        if (FilenameUtils.getExtension(name).length() == 0)
            name = name + ".schematic";

        File schematicsFile = getValidatedSchematicsFile();
        return schematicsFile != null ? loadSchematicByNameInDirectory(schematicsFile, name) : null;
    }

    public static SchematicFile loadSchematicByNameInDirectory(File directory, String name) throws SchematicFile.UnsupportedSchematicFormatException
    {
        File[] files = directory.listFiles();

        if (files != null)
        {
            for (File file : files)
            {
                if (file.getName().equals(name))
                    return loadSchematicFromFile(file);
            }
        }

        return null;
    }

    public static SchematicFile loadSchematicFromFile(File file) throws SchematicFile.UnsupportedSchematicFormatException
    {
        NBTTagCompound compound = null;

        try (FileInputStream fileInputStream = new FileInputStream(file))
        {
            compound = CompressedStreamTools.readCompressed(fileInputStream);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (compound != null)
            return new SchematicFile(compound);

        return null;
    }

    public static String[] currentSchematicFiles()
    {
        File schematicsFile = getValidatedSchematicsFile();
        return schematicsFile != null ? schematicsFile.list() : new String[0];
    }

    public static String getLookupFolderName()
    {
        return "structures/schematics";
    }

    public static File getValidatedSchematicsFile()
    {
        File structuresFile = IvFileHelper.getValidatedFolder(RecurrentComplex.proxy.getBaseFolderFile("structures"));
        return structuresFile != null ? IvFileHelper.getValidatedFolder(structuresFile, "schematics") : null;
    }
}
