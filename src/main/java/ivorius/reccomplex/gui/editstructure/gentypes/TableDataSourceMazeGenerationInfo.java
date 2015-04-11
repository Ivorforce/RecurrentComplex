/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import ivorius.reccomplex.gui.editmazeblock.TableDataSourceMazePathList;
import ivorius.reccomplex.gui.editmazeblock.TableDataSourceSelection;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.SavedMazeComponent;
import ivorius.reccomplex.structures.generic.gentypes.MazeGenerationInfo;

/**
 * Created by lukas on 07.10.14.
 */
public class TableDataSourceMazeGenerationInfo extends TableDataSourceSegmented implements TableCellActionListener, TableCellPropertyListener
{
    public static final int[] DEFAULT_MAX_COMPONENT_SIZE = {100, 100, 100};

    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    private MazeGenerationInfo generationInfo;

    public TableDataSourceMazeGenerationInfo(TableNavigator navigator, TableDelegate tableDelegate, MazeGenerationInfo generationInfo)
    {
        this.navigator = navigator;
        this.tableDelegate = tableDelegate;
        this.generationInfo = generationInfo;

        addManagedSection(0, new TableDataSourceGenerationInfo(generationInfo));
    }

    @Override
    public int numberOfSegments()
    {
        return 4;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 1:
                return 2;
            case 2:
                return 1;
            case 3:
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
                if (index == 0)
                {
                    TableCellString cell = new TableCellString("mazeID", generationInfo.mazeID);
                    cell.addPropertyListener(this);
                    return new TableElementCell("Maze ID", cell);
                }
                else if (index == 1)
                {
                    TableCellFloatNullable cell = new TableCellFloatNullable("weight", TableElements.toFloat(generationInfo.mazeComponent.weight), 1.0f, 0, 10, "D", "C");
                    cell.addPropertyListener(this);
                    return new TableElementCell("Weight", cell);
                }
                break;
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
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    @Override
    public void actionPerformed(TableCell cell, String actionID)
    {
        if ("rooms".equals(cell.getID()))
        {
            navigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceSelection(mazeComponent().rooms, DEFAULT_MAX_COMPONENT_SIZE, tableDelegate, navigator)));
        }
        else if ("exits".equals(cell.getID()))
        {
            navigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceMazePathList(mazeComponent().exitPaths, tableDelegate, navigator, mazeComponent().rooms.boundsLower(), mazeComponent().rooms.boundsHigher())));
        }
    }

    @Override
    public void valueChanged(TableCellPropertyDefault cell)
    {
        if ("mazeID".equals(cell.getID()))
        {
            generationInfo.mazeID = (String) cell.getPropertyValue();
        }
        else if ("weight".equals(cell.getID()))
        {
            generationInfo.mazeComponent.weight = TableElements.toDouble((Float) cell.getPropertyValue());
        }
    }

    private SavedMazeComponent mazeComponent()
    {
        return generationInfo.mazeComponent;
    }
}
