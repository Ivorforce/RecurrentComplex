/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCellInteger;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.script.WorldScriptMazeGenerator;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class TableDataSourceMazeInfo extends TableDataSourceSegmented
{
    public WorldScriptMazeGenerator script;

    protected TableDelegate delegate;
    protected TableNavigator navigator;

    public TableDataSourceMazeInfo(WorldScriptMazeGenerator script, TableDelegate delegate, TableNavigator navigator)
    {
        this.script = script;
        this.delegate = delegate;
        this.navigator = navigator;

        addSegment(0, () -> {
            TableCellInteger cell = new TableCellInteger("roomSizeX", script.getRoomSize()[0], 1, 64);
            cell.addListener(roomSizeConsumer(0));
            return new TitledCell(IvTranslations.get("reccomplex.maze.rooms.size.x"), cell);
        }, () -> {
            TableCellInteger cell = new TableCellInteger("roomSizeY", script.getRoomSize()[1], 1, 64);
            cell.addListener(roomSizeConsumer(1));
            return new TitledCell(IvTranslations.get("reccomplex.maze.rooms.size.y"), cell);
        }, () -> {
            TableCellInteger cell = new TableCellInteger("roomSizeZ", script.getRoomSize()[2], 1, 64);
            cell.addListener(roomSizeConsumer(2));
            return new TitledCell(IvTranslations.get("reccomplex.maze.rooms.size.z"), cell);
        });
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Maze";
    }

    private Consumer<Integer> roomSizeConsumer(int index)
    {
        return val -> {
            int[] size = script.getRoomSize();
            size[index] = val;
            script.setRoomSize(size);
        };
    }
}
