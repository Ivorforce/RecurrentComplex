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
public class TableDataSourceMazeGenerationInfo extends TableDataSourceSegmented implements TableElementActionListener, TableElementPropertyListener
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
                    TableElementString element = new TableElementString("mazeID", "Maze ID", generationInfo.mazeID);
                    element.addPropertyListener(this);
                    return element;
                }
                else if (index == 1)
                {
                    TableElementFloatNullable element = new TableElementFloatNullable("weight", "Weight", TableElements.toFloat(generationInfo.mazeComponent.weight), 1.0f, 0, 10, "D", "C");
                    element.addPropertyListener(this);
                    return element;
                }
                break;
            case 2:
            {
                TableElementButton element = new TableElementButton("rooms", "Rooms", new TableElementButton.Action("edit", "Edit"));
                element.addListener(this);
                return element;
            }
            case 3:
            {
                TableElementButton element = new TableElementButton("exits", "Exits", new TableElementButton.Action("edit", "Edit"));
                element.addListener(this);
                return element;
            }
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    @Override
    public void actionPerformed(TableElement element, String actionID)
    {
        if ("rooms".equals(element.getID()))
        {
            navigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceSelection(mazeComponent().rooms, DEFAULT_MAX_COMPONENT_SIZE, tableDelegate, navigator)));
        }
        else if ("exits".equals(element.getID()))
        {
            navigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceMazePathList(mazeComponent().exitPaths, tableDelegate, navigator, mazeComponent().rooms.boundsLower(), mazeComponent().rooms.boundsHigher())));
        }
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        if ("mazeID".equals(element.getID()))
        {
            generationInfo.mazeID = (String) element.getPropertyValue();
        }
        else if ("weight".equals(element.getID()))
        {
            generationInfo.mazeComponent.weight = TableElements.toDouble((Float) element.getPropertyValue());
        }
    }

    private SavedMazeComponent mazeComponent()
    {
        return generationInfo.mazeComponent;
    }
}
