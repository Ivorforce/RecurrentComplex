/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDataSourceList;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.structures.generic.BlockPattern;

import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceBlockPatternIngredientList extends TableDataSourceList<BlockPattern.Ingredient, List<BlockPattern.Ingredient>>
{
    public TableDataSourceBlockPatternIngredientList(List<BlockPattern.Ingredient> list, TableDelegate tableDelegate, TableNavigator navigator)
    {
        super(list, tableDelegate, navigator);
        setUsesPresetActionForAdding(true);
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

    @Override
    public TableDataSource editEntryDataSource(BlockPattern.Ingredient entry)
    {
        return new TableDataSourceBlockPatternIngredient(entry, tableDelegate);
    }
}
