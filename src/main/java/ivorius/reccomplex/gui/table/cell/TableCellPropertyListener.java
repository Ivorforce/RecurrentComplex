/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.cell;

/**
 * Created by lukas on 05.06.14.
 */
public interface TableCellPropertyListener<P>
{
    void valueChanged(TableCellPropertyDefault<? extends P> cell);
}
