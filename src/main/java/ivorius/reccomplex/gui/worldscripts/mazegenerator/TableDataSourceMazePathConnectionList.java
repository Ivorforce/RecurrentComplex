/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import net.minecraft.util.text.TextFormatting;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceList;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.ConnectorStrategy;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.SavedMazePathConnection;

import java.util.Arrays;
import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceMazePathConnectionList extends TableDataSourceList<SavedMazePathConnection, List<SavedMazePathConnection>>
{
    private List<IntegerRange> bounds;

    public TableDataSourceMazePathConnectionList(List<SavedMazePathConnection> list, TableDelegate tableDelegate, TableNavigator navigator, List<IntegerRange> bounds)
    {
        super(list, tableDelegate, navigator);
        this.bounds = bounds;
        setEarlierTitle(IvTranslations.get("gui.up"));
        setLaterTitle(IvTranslations.get("gui.down"));
    }

    @Override
    public String getDisplayString(SavedMazePathConnection mazePath)
    {
        return String.format("%s %s%s%s", Arrays.toString(mazePath.path.sourceRoom.getCoordinates()),
                TextFormatting.BLUE, TableDataSourceMazePath.directionFromPath(mazePath.path).toString(), TextFormatting.RESET);
    }

    @Override
    public SavedMazePathConnection newEntry(String actionID)
    {
        return new SavedMazePathConnection(2, new MazeRoom(new int[bounds.size()]), false, ConnectorStrategy.DEFAULT_PATH);
    }

    @Override
    public TableDataSource editEntryDataSource(SavedMazePathConnection entry)
    {
        return new TableDataSourceMazePathConnection(entry, bounds, tableDelegate);
    }
}
