/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.*;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.structure.generic.Selection;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;

/**
* Created by lukas on 08.10.14.
*/
public class TableDataSourceSelectionArea extends TableDataSourceSegmented
{
    private Selection.Area area;

    private int[] dimensions;
    private boolean showIdentifier;

    public TableDataSourceSelectionArea(Selection.Area area, int[] dimensions, boolean showIdentifier)
    {
        this.area = area;
        this.dimensions = dimensions;
        this.showIdentifier = showIdentifier;
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Area";
    }

    @Override
    public int numberOfSegments()
    {
        return showIdentifier ? 3 : 2;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 0:
                return 1;
            case 1:
                return 3;
            case 2:
                return 1;
            default:
                return super.sizeOfSegment(segment);
        }
    }

    @Override
    public TableCell cellForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            TableCellBoolean cell = new TableCellBoolean("additive", area.isAdditive(),
                    TextFormatting.GREEN + IvTranslations.get("reccomplex.selection.area.additive"),
                    TextFormatting.GOLD + IvTranslations.get("reccomplex.selection.area.subtractive"));
            cell.addPropertyConsumer(area::setAdditive);
            return new TitledCell(cell);
        }
        else if (segment == 1)
        {
            String title = IvTranslations.get("reccomplex.selection.area.range." + new String[]{"x", "y", "z"}[index]);
            IntegerRange intRange = new IntegerRange(area.getMinCoord()[index], area.getMaxCoord()[index]);
            TableCellIntegerRange cell = new TableCellIntegerRange("area" + index, intRange, 0, dimensions[index] - 1);
            cell.addPropertyConsumer(val -> area.setCoord(index, val.getMin(), val.getMax()));
            return new TitledCell(title, cell);
        }
        else if (segment == 2)
        {
            TableCellString cell = new TableCellString("", area.getIdentifier() != null ? area.getIdentifier() : "");
            cell.addPropertyConsumer(area::setIdentifier);
            return new TitledCell(IvTranslations.get("reccomplex.selection.area.identifier"), cell);
        }

        return super.cellForIndexInSegment(table, index, segment);
    }
}
