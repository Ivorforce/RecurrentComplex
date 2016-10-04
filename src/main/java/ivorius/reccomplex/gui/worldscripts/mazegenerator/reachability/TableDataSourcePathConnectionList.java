/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator.reachability;

import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.worldscripts.mazegenerator.TableDataSourceMazePath;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.SavedMazePath;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.List;

/**
 * Created by lukas on 16.03.16.
 */
public class TableDataSourcePathConnectionList extends TableDataSourceList<ImmutablePair<SavedMazePath, SavedMazePath>, List<ImmutablePair<SavedMazePath, SavedMazePath>>>
{
    private List<IntegerRange> bounds;

    public TableDataSourcePathConnectionList(List<ImmutablePair<SavedMazePath, SavedMazePath>> list, TableDelegate tableDelegate, TableNavigator navigator, List<IntegerRange> bounds)
    {
        super(list, tableDelegate, navigator);
        this.bounds = bounds;
    }

    public TableDelegate getTableDelegate()
    {
        return tableDelegate;
    }

    public void setTableDelegate(TableDelegate tableDelegate)
    {
        this.tableDelegate = tableDelegate;
    }

    @Override
    public String getDisplayString(ImmutablePair<SavedMazePath, SavedMazePath> pair)
    {
        return pair.toString();
    }

    @Override
    public ImmutablePair<SavedMazePath, SavedMazePath> newEntry(String actionID)
    {
        return ImmutablePair.of(new SavedMazePath(0, new MazeRoom(0, 0, 0), true), new SavedMazePath(0, new MazeRoom(0, 0, 0), false));
    }

    @Override
    public TableDataSource editEntryDataSource(ImmutablePair<SavedMazePath, SavedMazePath> pair)
    {
        return new TableDataSourceMulti(
                new TableDataSourcePreloaded(new TableElementCell(new TableCellTitle("", IvTranslations.get("reccomplex.gui.source")))),
                new TableDataSourceMazePath(pair.getLeft(), bounds, tableDelegate),
                new TableDataSourcePreloaded(new TableElementCell(new TableCellTitle("", IvTranslations.get("reccomplex.gui.destination")))),
                new TableDataSourceMazePath(pair.getRight(), bounds, tableDelegate)
        );
    }
}
