/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator.rules;

import ivorius.reccomplex.gui.table.TableCells;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.cell.TableCellButton;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceList;
import ivorius.reccomplex.utils.RCStrings;
import ivorius.reccomplex.world.gen.feature.structure.generic.Selection;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.SavedMazePathConnection;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.rules.MazeRule;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.rules.MazeRuleRegistry;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Created by lukas on 21.03.16.
 */
public class TableDataSourceMazeRuleList extends TableDataSourceList<MazeRule, List<MazeRule>>
{
    private List<SavedMazePathConnection> expected;
    private Selection bounds;

    public TableDataSourceMazeRuleList(List<MazeRule> list, TableDelegate tableDelegate, TableNavigator navigator, List<SavedMazePathConnection> expected, Selection bounds)
    {
        super(list, tableDelegate, navigator);
        this.expected = expected;
        this.bounds = bounds;
        setUsesPresetActionForAdding(true);
    }

    @Override
    public String getDisplayString(MazeRule mazeRule)
    {
        return RCStrings.abbreviateFormatted(mazeRule.displayString(), 24);
    }

    @Override
    public MazeRule newEntry(String actionID)
    {
        return tryInstantiate(actionID, MazeRuleRegistry.INSTANCE.objectClass(actionID), "Failed instantiating maze rule: %s");
    }

    @Nonnull
    @Override
    public TableCell entryCell(boolean enabled, MazeRule mazeRule)
    {
        return TableCells.edit(enabled, navigator, tableDelegate, () -> mazeRule.tableDataSource(navigator, tableDelegate, expected, bounds));
    }

    @Override
    public List<TableCellButton> getAddActions()
    {
        return TableCells.addManyWithBase(MazeRuleRegistry.INSTANCE.allIDs(), "reccomplex.mazerule.", canEditList());
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Rules";
    }
}
