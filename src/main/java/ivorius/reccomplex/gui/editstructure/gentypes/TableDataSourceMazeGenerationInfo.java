/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import ivorius.reccomplex.gui.editmazeblock.TableDataSourceMazePathList;
import ivorius.reccomplex.gui.editmazeblock.TableDataSourceSelection;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import ivorius.reccomplex.structures.generic.SavedMazeComponent;
import ivorius.reccomplex.structures.generic.gentypes.MazeGenerationInfo;

/**
 * Created by lukas on 07.10.14.
 */
public class TableDataSourceMazeGenerationInfo extends TableDataSourceSegmented implements TableElementButton.Listener, TableElementPropertyListener
{
    public static final int[] DEFAULT_MAX_COMPONENT_SIZE = {100, 100, 100};

    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    private MazeGenerationInfo mazeGenerationInfo;

    public TableDataSourceMazeGenerationInfo(TableNavigator navigator, TableDelegate tableDelegate, MazeGenerationInfo mazeGenerationInfo)
    {
        this.navigator = navigator;
        this.tableDelegate = tableDelegate;
        this.mazeGenerationInfo = mazeGenerationInfo;
    }

    @Override
    public int numberOfSegments()
    {
        return 3;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 0:
                return 3;
            case 1:
                return 1;
            case 2:
                return 1;
        }

        return 0;
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            if (index == 0)
            {
                TableElementString element = new TableElementString("mazeID", "Maze ID", mazeGenerationInfo.mazeID);
                element.addPropertyListener(this);
                return element;
            }
            else if (index == 1)
            {
                TableElementBoolean element = new TableElementBoolean("defaultWeight", "Use Default Weight", mazeGenerationInfo.mazeComponent.hasDefaultWeight());
                element.addPropertyListener(this);
                return element;
            }
            else if (index == 2)
            {
                TableElementFloat element = new TableElementFloat("weight", "Weight", (float) mazeGenerationInfo.mazeComponent.getWeight(), 0, 10);
                element.setEnabled(!mazeGenerationInfo.mazeComponent.hasDefaultWeight());
                element.addPropertyListener(this);
                return element;
            }
        }
        else if (segment == 1)
        {
            TableElementButton element = new TableElementButton("rooms", "Rooms", new TableElementButton.Action("edit", "Edit"));
            element.addListener(this);
            return element;
        }
        else if (segment == 2)
        {
            TableElementButton element = new TableElementButton("exits", "Exits", new TableElementButton.Action("edit", "Edit"));
            element.addListener(this);
            return element;
        }

        return null;
    }

    @Override
    public void actionPerformed(TableElementButton tableElementButton, String actionID)
    {
        if ("rooms".equals(tableElementButton.getID()))
        {
            navigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceSelection(mazeComponent().rooms, DEFAULT_MAX_COMPONENT_SIZE, tableDelegate, navigator)));
        }
        else if ("exits".equals(tableElementButton.getID()))
        {
            navigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceMazePathList(mazeComponent().exitPaths, mazeComponent().rooms.boundsHigher(), tableDelegate, navigator)));
        }
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        if ("mazeID".equals(element.getID()))
        {
            mazeGenerationInfo.mazeID = (String) element.getPropertyValue();
        }
        else if ("weight".equals(element.getID()))
        {
            mazeGenerationInfo.mazeComponent.weight = (double) (Float) element.getPropertyValue();
        }
        else if ("defaultWeight".equals(element.getID()))
        {
            boolean useDefault = (boolean) element.getPropertyValue();
            mazeGenerationInfo.mazeComponent.weight = useDefault ? null : mazeGenerationInfo.mazeComponent.getWeight();
            tableDelegate.reloadData();
        }
    }

    private SavedMazeComponent mazeComponent()
    {
        return mazeGenerationInfo.mazeComponent;
    }
}
