/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.inventorygen;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.cell.TableCellIntegerRange;
import ivorius.reccomplex.gui.table.cell.TitledCell;
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
    public TableCell cellForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            if (index == 0)
            {
                TableCellIntegerRange cell = new TableCellIntegerRange("itemCount", item.getGenerationCount(stack), 0, 64);
                cell.addPropertyConsumer(range -> item.setGenerationCount(stack, range));
                return new TitledCell(IvTranslations.get("reccomplex.gui.inventorygen.multi.count"), cell);
            }
        }

        return null;
    }
}
