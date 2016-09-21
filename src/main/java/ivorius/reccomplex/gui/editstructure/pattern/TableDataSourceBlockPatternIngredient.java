/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.pattern;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.BlockPattern;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBlockPatternIngredient extends TableDataSourceSegmented
{
    private BlockPattern.Ingredient ingredient;

    private TableDelegate tableDelegate;

    public TableDataSourceBlockPatternIngredient(BlockPattern.Ingredient ingredient, TableDelegate tableDelegate)
    {
        this.ingredient = ingredient;
        this.tableDelegate = tableDelegate;

        addManagedSegment(1, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.gui.blocks"), ingredient.matcher, null));
    }

    @Override
    public int numberOfSegments()
    {
        return 3;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return segment == 0 || segment == 2 ? 1 : super.sizeOfSegment(segment);
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            TableCellString cell = new TableCellString("", ingredient.identifier);
            cell.addPropertyConsumer(s -> ingredient.identifier = s);
            return new TableElementCell(IvTranslations.get("reccomplex.blockpattern.ingredient.identifier"), cell);
        }
        else if (segment == 2)
        {
            TableCellBoolean cell = new TableCellBoolean("", ingredient.delete);
            cell.addPropertyConsumer(d -> ingredient.delete = d);
            return new TableElementCell(IvTranslations.get("reccomplex.blockpattern.ingredient.delete"), cell);
        }

        return super.elementForIndexInSegment(table, index, segment);
    }
}
