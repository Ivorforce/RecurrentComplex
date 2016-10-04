/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.cell;

/**
 * Created by lukas on 03.06.14.
 */
public interface TableCellProperty<P> extends TableCell
{
    P getPropertyValue();

    void setPropertyValue(P value);
}
