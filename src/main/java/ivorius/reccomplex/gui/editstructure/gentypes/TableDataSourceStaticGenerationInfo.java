/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import com.google.common.primitives.Ints;
import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.editstructure.TableDataSourceNaturalGenLimitation;
import ivorius.reccomplex.gui.editstructure.TableDataSourceYSelector;
import ivorius.reccomplex.gui.editstructure.gentypes.staticgen.TableDataSourceStaticPattern;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.gentypes.NaturalGenerationInfo;
import ivorius.reccomplex.structures.generic.gentypes.StaticGenerationInfo;

/**
 * Created by lukas on 07.10.14.
 */
public class TableDataSourceStaticGenerationInfo extends TableDataSourceSegmented implements TableCellPropertyListener, TableCellActionListener
{
    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    private StaticGenerationInfo generationInfo;

    public TableDataSourceStaticGenerationInfo(TableNavigator navigator, TableDelegate tableDelegate, StaticGenerationInfo generationInfo)
    {
        this.navigator = navigator;
        this.tableDelegate = tableDelegate;
        this.generationInfo = generationInfo;

        addManagedSection(0, new TableDataSourceGenerationInfo(generationInfo));
        addManagedSection(2, new TableDataSourceYSelector(generationInfo.ySelector));
        addManagedSection(3, TableDataSourceExpression.constructDefault("Dimensions", generationInfo.dimensionMatcher));
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
                return 3;
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
                if (index == 0)
                {
                    TableCellBoolean cell = new TableCellBoolean("relativeToSpawn", generationInfo.relativeToSpawn);
                    cell.addPropertyListener(this);
                    return new TableElementCell("At Spawn", cell);
                }
                else if (index == 1)
                {
                    TableCellStringInt cell = new TableCellStringInt("positionX", generationInfo.positionX);
                    cell.addPropertyListener(this);
                    return new TableElementCell("Position (x)", cell);
                }
                else if (index == 2)
                {
                    TableCellStringInt cell = new TableCellStringInt("positionZ", generationInfo.positionZ);
                    cell.addPropertyListener(this);
                    return new TableElementCell("Position (z)", cell);
                }
                break;
            }
            case 4:
            {
                TableCellButton cell = new TableCellButton("editPattern", new TableCellButton.Action("edit", "Edit", generationInfo.hasPattern()), generationInfo.hasPattern() ? new TableCellButton.Action("remove", "Remove") : new TableCellButton.Action("add", "Add"));
                cell.addListener(this);
                return new TableElementCell("Pattern", cell);
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
                case "positionX":
                {
                    generationInfo.positionX = (Integer) cell.getPropertyValue();
                    break;
                }
                case "positionZ":
                {
                    generationInfo.positionZ = (Integer) cell.getPropertyValue();
                    break;
                }
                case "relativeToSpawn":
                    generationInfo.relativeToSpawn = (boolean) cell.getPropertyValue();
                    break;
            }
        }
    }

    @Override
    public void actionPerformed(TableCell cell, String actionID)
    {
        if ("editPattern".equals(cell.getID()))
        {
            switch (actionID)
            {
                case "edit":
                    GuiTable table = new GuiTable(tableDelegate, new TableDataSourceStaticPattern(generationInfo.pattern, tableDelegate));
                    navigator.pushTable(table);
                    break;
                case "remove":
                    generationInfo.pattern = null;
                    tableDelegate.reloadData();
                    break;
                case "add":
                    generationInfo.pattern = new StaticGenerationInfo.Pattern();
                    tableDelegate.reloadData();
                    break;
            }
        }
    }
}
