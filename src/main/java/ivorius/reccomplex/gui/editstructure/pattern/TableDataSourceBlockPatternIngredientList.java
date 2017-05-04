/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.pattern;

import ivorius.reccomplex.gui.table.TableCells;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceList;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.world.gen.feature.structure.generic.BlockPattern;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceBlockPatternIngredientList extends TableDataSourceList<BlockPattern.Ingredient, List<BlockPattern.Ingredient>>
{
    public TableDataSourceBlockPatternIngredientList(List<BlockPattern.Ingredient> list, TableDelegate tableDelegate, TableNavigator navigator)
    {
        super(list, tableDelegate, navigator);
    }

    @Override
    public String getDisplayString(BlockPattern.Ingredient entry)
    {
        return entry.identifier;
    }

    @Override
    public BlockPattern.Ingredient newEntry(String actionID)
    {
        return new BlockPattern.Ingredient();
    }

    @Nonnull
    @Override
    public TableCell entryCell(boolean enabled, BlockPattern.Ingredient ingredient)
    {
        return TableCells.edit(enabled, navigator, tableDelegate, () -> new TableDataSourceBlockPatternIngredient(ingredient, tableDelegate));
    }
}
