/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.inventorygen;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.item.ItemInventoryGenMultiTag;

/**
 * Created by lukas on 17.01.15.
 */
public class TableDataSourceInvGenMultiTag extends TableDataSourceItem<ItemInventoryGenMultiTag>
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
                TableCellIntegerRange cell = new TableCellIntegerRange("itemCount", item.getGenerationCount(stack), 0, 64);
                cell.addPropertyConsumer(range -> item.setGenerationCount(stack, range));
                return new TableElementCell(IvTranslations.get("reccomplex.gui.inventorygen.multi.count"), cell);
            }
        }

        return null;
    }
}
