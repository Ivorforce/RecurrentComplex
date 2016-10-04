/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.storage.loot;

/**
 * Created by lukas on 29.09.14.
 */
public class InventoryLoadException extends Exception
{
    public InventoryLoadException()
    {
    }

    public InventoryLoadException(String message)
    {
        super(message);
    }

    public InventoryLoadException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public InventoryLoadException(Throwable cause)
    {
        super(cause);
    }

    public InventoryLoadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
