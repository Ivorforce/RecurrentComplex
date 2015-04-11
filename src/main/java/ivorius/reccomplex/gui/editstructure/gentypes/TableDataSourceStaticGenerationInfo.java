/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import com.google.common.primitives.Ints;
import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.editstructure.TableDataSourceYSelector;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.gentypes.StaticGenerationInfo;

/**
 * Created by lukas on 07.10.14.
 */
public class TableDataSourceStaticGenerationInfo extends TableDataSourceSegmented implements TableCellPropertyListener
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
        return 4;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 1:
                return 3;
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
                    TableCellString cell = new TableCellString("positionX", String.valueOf(generationInfo.positionX));
                    cell.setShowsValidityState(true);
                    cell.setValidityState(GuiValidityStateIndicator.State.VALID);
                    cell.addPropertyListener(this);
                    return new TableElementCell("Position (x)", cell);
                }
                else if (index == 2)
                {
                    TableCellString cell = new TableCellString("positionZ", String.valueOf(generationInfo.positionZ));
                    cell.setShowsValidityState(true);
                    cell.setValidityState(GuiValidityStateIndicator.State.VALID);
                    cell.addPropertyListener(this);
                    return new TableElementCell("Position (z)", cell);
                }
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
                    Integer val = Ints.tryParse((String) cell.getPropertyValue());
                    generationInfo.positionX = val != null ? val : 0;
                    ((TableCellString) cell).setValidityState(val != null ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.INVALID);
                    break;
                }
                case "positionZ":
                {
                    Integer val = Ints.tryParse((String) cell.getPropertyValue());
                    generationInfo.positionZ = val != null ? val : 0;
                    ((TableCellString) cell).setValidityState(val != null ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.INVALID);
                    break;
                }
                case "relativeToSpawn":
                    generationInfo.relativeToSpawn = (boolean) cell.getPropertyValue();
                    break;
            }
        }
    }
}
