/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.loot;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.cell.TableCellIntegerRange;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.item.ItemLootGenMultiTag;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by lukas on 17.01.15.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceLootGenMultiTag extends TableDataSourceItem<ItemLootGenMultiTag>
{
    public TableDataSourceLootGenMultiTag()
    {
        addSegment(0, () -> {
            TableCellIntegerRange cell = new TableCellIntegerRange("itemCount", item.getGenerationCount(stack), 0, 64);
            cell.addListener(range -> item.setGenerationCount(stack, range));
            return new TitledCell(IvTranslations.get("reccomplex.gui.inventorygen.multi.count"), cell);
        });
    }
}
