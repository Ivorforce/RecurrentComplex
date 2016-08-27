/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.worldscripts.mazegenerator.reachability.TableDataSourceMazeReachability;
import ivorius.reccomplex.structures.generic.maze.SavedMazeComponent;
import ivorius.reccomplex.structures.generic.maze.SavedMazeReachability;

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
        addManagedSection(0, new TableDataSourceConnector(component.defaultConnector, IvTranslations.get("reccomplex.maze.connector.default")));
        addManagedSection(1, TableCellMultiBuilder.create(navigator, tableDelegate)
                .addNavigation(() -> "Edit", null,
                        () -> new GuiTable(tableDelegate, new TableDataSourceSelection(component.rooms, DEFAULT_MAX_COMPONENT_SIZE, tableDelegate, navigator))
                ).buildPreloaded("Rooms"));
        addManagedSection(2, TableCellMultiBuilder.create(navigator, tableDelegate)
                .addNavigation(() -> "Edit", null,
                        () -> new GuiTable(tableDelegate, new TableDataSourceMazePathConnectionList(component.exitPaths, tableDelegate, navigator, component.rooms.boundsLower(), component.rooms.boundsHigher()))
                ).buildPreloaded("Exits"));
        addManagedSection(3, TableCellMultiBuilder.create(navigator, tableDelegate)
                .addNavigation(() -> "Edit", () -> IvTranslations.formatLines("reccomplex.reachability.tooltip"),
                        () -> new GuiTable(tableDelegate, new TableDataSourceMazeReachability(component.reachability, tableDelegate, navigator, SavedMazeReachability.buildExpected(component), component.rooms.boundsLower(), component.rooms.boundsHigher()))
                ).buildPreloaded("Reachability"));

    }

    @Override
    public int numberOfSegments()
    {
        return showReachability ? 4 : 3;
    }
}
