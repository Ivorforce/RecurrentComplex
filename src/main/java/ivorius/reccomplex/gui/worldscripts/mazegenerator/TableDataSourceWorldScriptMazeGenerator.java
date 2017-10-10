/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.client.rendering.MazeVisualizationContext;
import ivorius.reccomplex.gui.TableDataSourceBlockPos;
import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.*;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.worldscripts.TableDataSourceWorldScript;
import ivorius.reccomplex.gui.worldscripts.mazegenerator.rules.TableDataSourceMazeRuleList;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.MazeGeneration;
import ivorius.reccomplex.world.gen.script.WorldScriptMazeGenerator;
import net.minecraft.util.math.BlockPos;

import java.util.function.Consumer;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceWorldScriptMazeGenerator extends TableDataSourceSegmented
{
    private WorldScriptMazeGenerator script;

    protected TableDelegate delegate;
    protected TableNavigator navigator;

    public TableDataSourceWorldScriptMazeGenerator(WorldScriptMazeGenerator script, BlockPos realWorldPos, TableDelegate delegate, TableNavigator navigator)
    {
        this.script = script;
        this.delegate = delegate;
        this.navigator = navigator;

        addManagedSegment(0, new TableDataSourceWorldScript(script));
        addManagedSegment(2, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceMazeComponent(script.mazeComponent, navigator, delegate)
                                .visualizing(new MazeVisualizationContext(script.structureShift.add(realWorldPos), script.roomSize)),
                        () -> IvTranslations.get("reccomplex.maze"))
                .buildDataSource());
        addManagedSegment(3, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceMazeRuleList(script.rules, delegate, navigator, script.mazeComponent.exitPaths, script.mazeComponent.rooms), () -> IvTranslations.get("reccomplex.worldscript.mazeGen.rules"))
                .buildDataSource());
        addManagedSegment(4, new TableDataSourceBlockPos(script.getStructureShift(), script::setStructureShift,
                IvTranslations.get("reccomplex.gui.blockpos.shift"), IvTranslations.getLines("reccomplex.gui.blockpos.shift.tooltip")));
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
        return 6;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 1:
                return 1;
            case 5:
                return 3;
            default:
                return super.sizeOfSegment(segment);
        }
    }

    @Override
    public TableCell cellForIndexInSegment(GuiTable table, int index, int segment)
    {
        switch (segment)
        {
            case 1:
            {
                TableCellString cell = new TableCellString("mazeID", script.getMazeID());
                cell.setShowsValidityState(true);
                cell.setValidityState(MazeGeneration.idValidity(cell.getPropertyValue()));
                cell.addListener((mazeID) -> {
                    script.setMazeID(mazeID);
                    cell.setValidityState(MazeGeneration.idValidity(mazeID));
                });
                return new TitledCell(IvTranslations.get("reccomplex.maze.id"), cell);
            }
            case 5:
                switch (index)
                {
                    case 0:
                    {
                        TableCellInteger cell = new TableCellInteger("roomSizeX", script.getRoomSize()[0], 1, 64);
                        cell.addListener(roomSizeConsumer(0));
                        return new TitledCell(IvTranslations.get("reccomplex.maze.rooms.size.x"), cell);
                    }
                    case 1:
                    {
                        TableCellInteger cell = new TableCellInteger("roomSizeY", script.getRoomSize()[1], 1, 64);
                        cell.addListener(roomSizeConsumer(1));
                        return new TitledCell(IvTranslations.get("reccomplex.maze.rooms.size.y"), cell);
                    }
                    case 2:
                    {
                        TableCellInteger cell = new TableCellInteger("roomSizeZ", script.getRoomSize()[2], 1, 64);
                        cell.addListener(roomSizeConsumer(2));
                        return new TitledCell(IvTranslations.get("reccomplex.maze.rooms.size.z"), cell);
                    }
                }
                break;
        }

        return super.cellForIndexInSegment(table, index, segment);
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
