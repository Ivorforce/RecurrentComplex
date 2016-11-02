/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator.rules;

import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.reccomplex.gui.editstructure.preset.TableDataSourcePresettedList;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCellButton;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceList;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.SavedMazePathConnection;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.rules.MazeRule;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.rules.MazeRuleRegistry;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Created by lukas on 21.03.16.
 */
public class TableDataSourceMazeRuleList extends TableDataSourceList<MazeRule, List<MazeRule>>
{
    private List<SavedMazePathConnection> expected;
    private List<IntegerRange> bounds;

    public TableDataSourceMazeRuleList(List<MazeRule> list, TableDelegate tableDelegate, TableNavigator navigator, List<SavedMazePathConnection> expected, List<IntegerRange> bounds)
    {
        super(list, tableDelegate, navigator);
        this.expected = expected;
        this.bounds = bounds;
        setUsesPresetActionForAdding(true);
    }

    @Override
    public String getDisplayString(MazeRule mazeRule)
    {
        return StringUtils.abbreviate(mazeRule.displayString(), 24);
    }

    @Override
    public MazeRule newEntry(String actionID)
    {
        return tryInstantiate(actionID, MazeRuleRegistry.INSTANCE.objectClass(actionID), "Failed instantiating maze rule: %s");
    }

    @Override
    public TableDataSource editEntryDataSource(MazeRule mazeRule)
    {
        return mazeRule.tableDataSource(navigator, tableDelegate, expected, bounds);
    }

    @Override
    public List<TableCellButton> getAddActions()
    {
        return TableDataSourcePresettedList.addActions(MazeRuleRegistry.INSTANCE.allIDs(), "reccomplex.mazerule.", canEditList());
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Rules";
    }
}
