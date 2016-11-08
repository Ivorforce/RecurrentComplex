/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.pattern;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.cell.TableCellBoolean;
import ivorius.reccomplex.gui.table.cell.TableCellString;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.structure.generic.BlockPattern;

import javax.annotation.Nonnull;

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

    @Nonnull
    @Override
    public String title()
    {
        return "Ingredient";
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
    public TableCell cellForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            TableCellString cell = new TableCellString("", ingredient.identifier);
            cell.addPropertyConsumer(s -> ingredient.identifier = s);
            return new TitledCell(IvTranslations.get("reccomplex.blockpattern.ingredient.identifier"), cell);
        }
        else if (segment == 2)
        {
            TableCellBoolean cell = new TableCellBoolean("", ingredient.delete);
            cell.addPropertyConsumer(d -> ingredient.delete = d);
            return new TitledCell(IvTranslations.get("reccomplex.blockpattern.ingredient.delete"), cell);
        }

        return super.cellForIndexInSegment(table, index, segment);
    }
}
