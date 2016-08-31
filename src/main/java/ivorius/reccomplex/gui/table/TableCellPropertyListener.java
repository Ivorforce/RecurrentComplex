/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

/**
 * Created by lukas on 05.06.14.
 */
public interface TableCellPropertyListener<P>
{
    void valueChanged(TableCellPropertyDefault<? extends P> cell);
}
