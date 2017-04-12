/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCellMultiBuilder;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.worldscripts.mazegenerator.reachability.TableDataSourceMazeReachability;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.SavedMazeComponent;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.SavedMazeReachability;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 26.04.15.
 */
public class TableDataSourceMazeComponent extends TableDataSourceSegmented
{
    public static final int[] DEFAULT_MAX_COMPONENT_SIZE = {100, 100, 100};

    private SavedMazeComponent component;

    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    private boolean showReachability;

    public TableDataSourceMazeComponent(SavedMazeComponent component, boolean showReachability, TableNavigator navigator, TableDelegate tableDelegate)
    {
        this.component = component;
        this.showReachability = showReachability;
        this.navigator = navigator;
        this.tableDelegate = tableDelegate;
        addManagedSegment(0, new TableDataSourceConnector(component.defaultConnector, IvTranslations.get("reccomplex.maze.connector.default")));

        addManagedSegment(1, TableCellMultiBuilder.create(navigator, tableDelegate)
                .addNavigation(() -> new TableDataSourceSelection(component.rooms, DEFAULT_MAX_COMPONENT_SIZE, tableDelegate, navigator, false))
                .buildDataSource(IvTranslations.get("reccomplex.generationInfo.mazeComponent.rooms"), IvTranslations.getLines("reccomplex.generationInfo.mazeComponent.rooms.tooltip")));

        addManagedSegment(2, TableCellMultiBuilder.create(navigator, tableDelegate)
                .addNavigation(() -> new TableDataSourceMazePathConnectionList(component.exitPaths, tableDelegate, navigator, component.rooms.bounds()))
                .enabled(() -> component.rooms.size() > 0)
                .buildDataSource(IvTranslations.get("reccomplex.generationInfo.mazeComponent.exits"), IvTranslations.getLines("reccomplex.generationInfo.mazeComponent.exits.tooltip")));

        addManagedSegment(3, TableCellMultiBuilder.create(navigator, tableDelegate)
                .addNavigation(() -> new TableDataSourceMazeReachability(component.reachability, tableDelegate, navigator, SavedMazeReachability.buildExpected(component), component.rooms.bounds()))
                .enabled(() -> component.rooms.size() > 0)
                .buildDataSource(IvTranslations.get("reccomplex.generationInfo.mazeComponent.reachability"), IvTranslations.formatLines("reccomplex.reachability.tooltip")));

    }

    @Nonnull
    @Override
    public String title()
    {
        return "Maze Component";
    }

    @Override
    public int numberOfSegments()
    {
        return showReachability ? 4 : 3;
    }
}
