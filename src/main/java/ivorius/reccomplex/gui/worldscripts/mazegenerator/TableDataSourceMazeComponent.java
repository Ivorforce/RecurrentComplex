/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.worldscripts.mazegenerator.reachability.TableDataSourceMazeReachability;
import ivorius.reccomplex.structures.generic.maze.SavedMazeComponent;
import ivorius.reccomplex.structures.generic.maze.SavedMazeReachability;
import ivorius.reccomplex.utils.IvTranslations;
import ivorius.reccomplex.utils.scale.Scales;

/**
 * Created by lukas on 26.04.15.
 */
public class TableDataSourceMazeComponent extends TableDataSourceSegmented implements TableCellPropertyListener, TableCellActionListener
{
    public static final int[] DEFAULT_MAX_COMPONENT_SIZE = {100, 100, 100};

    private SavedMazeComponent component;

    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    public TableDataSourceMazeComponent(SavedMazeComponent component, TableNavigator navigator, TableDelegate tableDelegate)
    {
        this.component = component;
        this.navigator = navigator;
        this.tableDelegate = tableDelegate;
        addManagedSection(0, new TableDataSourceConnector(component.defaultConnector, IvTranslations.get("reccomplex.maze.connector.default")));
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
            case 1:
            case 2:
            case 3:
            case 4:
                return 1;
        }

        return super.sizeOfSegment(segment);
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        switch (segment)
        {
            case 1:
            {
                TableCellFloatNullable cell = new TableCellFloatNullable("weight", TableElements.toFloat(component.weight), 1.0f, 0, 1000, "D", "C");
                cell.setScale(Scales.pow(5));
                cell.addPropertyListener(this);
                cell.setTooltip(IvTranslations.formatLines("structures.gui.random.weight.tooltip"));
                return new TableElementCell(IvTranslations.get("structures.gui.random.weight"), cell);
            }
            case 2:
            {
                TableCellButton cell = new TableCellButton("rooms", new TableCellButton.Action("edit", "Edit"));
                cell.addListener(this);
                return new TableElementCell("Rooms", cell);
            }
            case 3:
            {
                TableCellButton cell = new TableCellButton("exits", new TableCellButton.Action("edit", "Edit"));
                cell.addListener(this);
                return new TableElementCell("Exits", cell);
            }
            case 4:
            {
                TableCellButton cell = new TableCellButton("reachability", new TableCellButton.Action("edit", "Edit"));
                cell.setTooltip(IvTranslations.formatLines("reccomplex.reachability.tooltip"));
                cell.addListener(this);
                return new TableElementCell("Reachability", cell);
            }
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    @Override
    public void actionPerformed(TableCell cell, String actionID)
    {
        if ("rooms".equals(cell.getID()))
        {
            navigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceSelection(component.rooms, DEFAULT_MAX_COMPONENT_SIZE, tableDelegate, navigator)));
        }
        else if ("exits".equals(cell.getID()))
        {
            navigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceMazePathConnectionList(component.exitPaths, tableDelegate, navigator, component.rooms.boundsLower(), component.rooms.boundsHigher())));
        }
        else if ("reachability".equals(cell.getID()))
        {
            navigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceMazeReachability(component.reachability, tableDelegate, navigator, SavedMazeReachability.buildExpected(component), component.rooms.boundsLower(), component.rooms.boundsHigher())));
        }
    }

    @Override
    public void valueChanged(TableCellPropertyDefault cell)
    {
        if ("weight".equals(cell.getID()))
        {
            component.weight = TableElements.toDouble((Float) cell.getPropertyValue());
        }
    }
}
