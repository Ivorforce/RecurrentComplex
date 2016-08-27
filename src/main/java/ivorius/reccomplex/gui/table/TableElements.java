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

    public static void reloadExcept(TableDelegate delegate, String... elementIDs)
    {
        for (String element : elementIDs)
            delegate.setLocked(element, true);

        delegate.reloadData();

        for (String element : elementIDs)
            delegate.setLocked(element, false);
    }
}
