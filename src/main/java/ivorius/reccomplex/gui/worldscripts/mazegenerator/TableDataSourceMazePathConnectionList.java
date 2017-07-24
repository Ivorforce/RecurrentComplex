/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.reccomplex.client.rendering.SelectionQuadCache;
import ivorius.reccomplex.gui.GuiHider;
import ivorius.reccomplex.gui.table.TableCells;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceList;
import ivorius.reccomplex.world.gen.feature.structure.generic.Selection;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.ConnectorStrategy;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.SavedMazePathConnection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceMazePathConnectionList extends TableDataSourceList<SavedMazePathConnection, List<SavedMazePathConnection>>
{
    private Selection bounds;

    protected Function<ivorius.ivtoolkit.maze.classic.MazeRoom, BlockPos> realWorldMapper;

    public TableDataSourceMazePathConnectionList(List<SavedMazePathConnection> list, TableDelegate tableDelegate, TableNavigator navigator, Selection bounds)
    {
        super(list, tableDelegate, navigator);
        this.bounds = bounds;
        duplicateTitle = TextFormatting.GREEN + "D";
    }

    public TableDataSourceMazePathConnectionList visualizing(Function<ivorius.ivtoolkit.maze.classic.MazeRoom, BlockPos> realWorldMapper)
    {
        this.realWorldMapper = realWorldMapper;
        return this;
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
        return new SavedMazePathConnection(2, new MazeRoom(new int[bounds.dimensions]), false, ConnectorStrategy.DEFAULT_PATH, Collections.emptyList());
    }

    @Override
    public SavedMazePathConnection copyEntry(SavedMazePathConnection mazePath)
    {
        return mazePath.copy();
    }

    @Nonnull
    @Override
    public TableCell entryCell(boolean enabled, SavedMazePathConnection savedMazePathConnection)
    {
        return TableCells.edit(enabled, navigator, tableDelegate, () -> new TableDataSourceMazePathConnection(savedMazePathConnection, bounds, tableDelegate, navigator));
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Paths";
    }

    @Override
    public boolean canVisualize()
    {
        return realWorldMapper != null;
    }

    @Override
    public GuiHider.Visualizer visualizer()
    {
        Selection selection = new Selection(bounds.dimensions);

        for (SavedMazePathConnection connection : list)
            selection.add(Selection.Area.from(true, connection.path.sourceRoom.getCoordinates(), connection.path.getDestRoom().getCoordinates(), null));

        return new SelectionQuadCache.Visualizer(selection, realWorldMapper);
    }
}
