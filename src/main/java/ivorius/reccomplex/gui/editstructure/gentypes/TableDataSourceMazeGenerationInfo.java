/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import ivorius.reccomplex.gui.RCGuiTables;
import ivorius.reccomplex.gui.worldscripts.mazegenerator.TableDataSourceMazeComponent;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.gentypes.MazeGenerationInfo;

/**
 * Created by lukas on 07.10.14.
 */
public class TableDataSourceMazeGenerationInfo extends TableDataSourceSegmented implements TableCellPropertyListener
{
    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    private MazeGenerationInfo generationInfo;

    public TableDataSourceMazeGenerationInfo(TableNavigator navigator, TableDelegate tableDelegate, MazeGenerationInfo generationInfo)
    {
        this.navigator = navigator;
        this.tableDelegate = tableDelegate;
        this.generationInfo = generationInfo;

        addManagedSection(0, new TableDataSourceGenerationInfo(generationInfo));
        addManagedSection(3, new TableDataSourceMazeComponent(generationInfo.mazeComponent, true, navigator, tableDelegate));
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
            case 2:
                return 1;
            default:
                return super.sizeOfSegment(segment);
        }

    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        switch (segment)
        {
            case 1:
            {
                TableCellString cell = new TableCellString("mazeID", generationInfo.mazeID);
                cell.addPropertyListener(this);
                return new TableElementCell("Maze ID", cell);
            }
            case 2:
            {
                return RCGuiTables.defaultWeightElement(this, generationInfo.weight);
            }
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    @Override
    public void valueChanged(TableCellPropertyDefault cell)
    {
        if (cell.getID() != null)
        {
            switch (cell.getID())
            {
                case "mazeID":
                    generationInfo.mazeID = (String) cell.getPropertyValue();
                    break;
                case "weight":
                    generationInfo.weight = TableElements.toDouble((Float) cell.getPropertyValue());
                    break;
            }
        }
    }
}
