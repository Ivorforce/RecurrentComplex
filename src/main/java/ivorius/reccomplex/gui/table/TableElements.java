/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

/**
 * Created by lukas on 28.02.15.
 */
public class TableElements
{
    public static Float toFloat(Double value)
    {
        return value != null ? (float) (double) value : null;
    }

    public static Double toDouble(Float value)
    {
        return value != null ? (double) value : null;
    }
}
