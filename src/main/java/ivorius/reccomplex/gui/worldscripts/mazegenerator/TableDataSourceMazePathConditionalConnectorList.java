/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import ivorius.reccomplex.gui.table.TableCells;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceList;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.ConnectorStrategy;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.SavedMazePathConnection;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceMazePathConditionalConnectorList extends TableDataSourceList<SavedMazePathConnection.ConditionalConnector, List<SavedMazePathConnection.ConditionalConnector>>
{
    public TableDataSourceMazePathConditionalConnectorList(List<SavedMazePathConnection.ConditionalConnector> list, TableDelegate tableDelegate, TableNavigator navigator)
    {
        super(list, tableDelegate, navigator);
    }

    @Override
    public String getDisplayString(SavedMazePathConnection.ConditionalConnector conditionalConnector)
    {
        return conditionalConnector.connector.id;
    }

    @Override
    public SavedMazePathConnection.ConditionalConnector newEntry(int addIndex, String actionID)
    {
        return new SavedMazePathConnection.ConditionalConnector("", ConnectorStrategy.DEFAULT_PATH);
    }

    @Nonnull
    @Override
    public TableCell entryCell(boolean enabled, SavedMazePathConnection.ConditionalConnector conditionalConnector)
    {
        return TableCells.edit(enabled, navigator, tableDelegate, () -> new TableDataSourceConditionalConnector(conditionalConnector));
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Conditional Connectors";
    }
}
