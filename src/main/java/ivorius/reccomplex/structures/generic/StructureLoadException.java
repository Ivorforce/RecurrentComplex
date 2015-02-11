/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic;

/**
* Created by lukas on 29.09.14.
*/
public class StructureLoadException extends Exception
{
    public StructureLoadException()
    {
    }

    public StructureLoadException(String message)
    {
        super(message);
    }

    public StructureLoadException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public StructureLoadException(Throwable cause)
    {
        super(cause);
    }

    public StructureLoadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
