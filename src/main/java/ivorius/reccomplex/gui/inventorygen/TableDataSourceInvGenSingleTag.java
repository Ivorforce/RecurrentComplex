/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.inventorygen;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCellFloat;
import ivorius.reccomplex.gui.table.cell.TableElementCell;
import ivorius.reccomplex.item.ItemInventoryGenSingleTag;
import ivorius.reccomplex.utils.scale.Scales;

/**
 * Created by lukas on 17.01.15.
 */
public class TableDataSourceInvGenSingleTag extends TableDataSourceItem<ItemInventoryGenSingleTag>
{
    @Override
    public int numberOfSegments()
    {
        return 1;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return 1;
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            if (index == 0)
            {
                TableCellFloat cell = new TableCellFloat("itemCount", item.getItemChance(stack), 0, 1);
                cell.setScale(Scales.pow(5));
                cell.addPropertyConsumer(range -> item.setItemChance(stack, range));
                return new TableElementCell(IvTranslations.get("reccomplex.gui.inventorygen.single.chance"), cell);
            }
        }

        return null;
    }
}
