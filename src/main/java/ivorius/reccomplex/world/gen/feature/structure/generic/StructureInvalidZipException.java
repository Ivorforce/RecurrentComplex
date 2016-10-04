/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic;

/**
* Created by lukas on 29.09.14.
*/
public class StructureInvalidZipException extends StructureLoadException
{
    public boolean jsonExists;
    public boolean worldDataExists;

    public StructureInvalidZipException(boolean jsonExists, boolean worldDataExists)
    {
        super("Cannot load structure! Found Json: " + jsonExists + ", World Data: " + worldDataExists);
        this.jsonExists = jsonExists;
        this.worldDataExists = worldDataExists;
    }
}
