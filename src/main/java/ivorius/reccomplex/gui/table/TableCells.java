/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

/**
 * Created by lukas on 28.02.15.
 */
public class TableCells
{
    public static Float toFloat(Double value)
    {
        return value != null ? (float) (double) value : null;
    }

    public static Double toDouble(Float value)
    {
        return value != null ? (double) value : null;
    }

    public static void reloadExcept(TableDelegate delegate, String... cellIDs)
    {
        for (String cell : cellIDs)
            delegate.setLocked(cell, true);

        delegate.reloadData();

        for (String cell : cellIDs)
            delegate.setLocked(cell, false);
    }
}
