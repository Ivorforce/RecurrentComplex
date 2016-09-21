/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.TableDataSourceBlockPos;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.worldscripts.mazegenerator.rules.TableDataSourceMazeRuleList;
import ivorius.reccomplex.scripts.world.WorldScriptMazeGenerator;

import java.util.function.Consumer;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceWorldScriptMazeGenerator extends TableDataSourceSegmented
{
    private WorldScriptMazeGenerator script;

    private TableDelegate delegate;
    private TableNavigator navigator;

    public TableDataSourceWorldScriptMazeGenerator(WorldScriptMazeGenerator script, TableDelegate delegate, TableNavigator navigator)
    {
        this.script = script;
        this.delegate = delegate;
        this.navigator = navigator;

        addManagedSegment(1, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceMazeComponent(script.mazeComponent, false, navigator, delegate)
                ).buildDataSource(IvTranslations.get("reccomplex.maze")));
        addManagedSegment(2, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceMazeRuleList(script.rules, delegate, navigator, script.mazeComponent.exitPaths, script.mazeComponent.rooms.bounds())
                ).buildDataSource(IvTranslations.get("reccomplex.worldscript.mazeGen.rules")));
        addManagedSegment(3, new TableDataSourceBlockPos(script.getStructureShift(), script::setStructureShift,
                new IntegerRange(-50, 50), new IntegerRange(-50, 50), new IntegerRange(-50, 50),
                IvTranslations.get("reccomplex.worldscript.mazeGen.shift.x"), IvTranslations.get("reccomplex.worldscript.mazeGen.shift.y"), IvTranslations.get("reccomplex.worldscript.mazeGen.shift.z")));
    }

    public WorldScriptMazeGenerator getScript()
    {
        return script;
    }

    public void setScript(WorldScriptMazeGenerator script)
    {
        this.script = script;
    }

    public TableDelegate getTableDelegate()
    {
        return delegate;
    }

    public void setTableDelegate(TableDelegate tableDelegate)
    {
        this.delegate = tableDelegate;
    }

    public TableNavigator getNavigator()
    {
        return navigator;
    }

    public void setNavigator(TableNavigator navigator)
    {
        this.navigator = navigator;
    }

    @Override
    public int numberOfSegments()
    {
        return 5;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 0:
                return 1;
            case 4:
                return 3;
            default:
                return super.sizeOfSegment(segment);
        }
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        switch (segment)
        {
            case 0:
            {
                TableCellString cell = new TableCellString("mazeID", script.getMazeID());
                cell.addPropertyConsumer(script::setMazeID);
                return new TableElementCell(IvTranslations.get("reccomplex.maze.id"), cell);
            }
            case 4:
                switch (index)
                {
                    case 0:
                    {
                        TableCellInteger cell = new TableCellInteger("roomSizeX", script.getRoomSize()[0], 1, 64);
                        cell.addPropertyConsumer(roomSizeConsumer(0));
                        return new TableElementCell(IvTranslations.get("reccomplex.maze.rooms.size.x"), cell);
                    }
                    case 1:
                    {
                        TableCellInteger cell = new TableCellInteger("roomSizeY", script.getRoomSize()[1], 1, 64);
                        cell.addPropertyConsumer(roomSizeConsumer(1));
                        return new TableElementCell(IvTranslations.get("reccomplex.maze.rooms.size.y"), cell);
                    }
                    case 2:
                    {
                        TableCellInteger cell = new TableCellInteger("roomSizeZ", script.getRoomSize()[2], 1, 64);
                        cell.addPropertyConsumer(roomSizeConsumer(2));
                        return new TableElementCell(IvTranslations.get("reccomplex.maze.rooms.size.z"), cell);
                    }
                }
                break;
        }

        return super.elementForIndexInSegment(table, index, segment);
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
