/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import com.google.common.primitives.Ints;
import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.TableDirections;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.gentypes.StructureListGenerationInfo;
import ivorius.ivtoolkit.blocks.Directions;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.utils.scale.Scales;
import net.minecraft.util.EnumFacing;

/**
 * Created by lukas on 07.10.14.
 */
public class TableDataSourceStructureListGenerationInfo extends TableDataSourceSegmented implements TableCellPropertyListener
{
    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    private StructureListGenerationInfo generationInfo;

    public TableDataSourceStructureListGenerationInfo(TableNavigator navigator, TableDelegate tableDelegate, StructureListGenerationInfo generationInfo)
    {
        this.navigator = navigator;
        this.tableDelegate = tableDelegate;
        this.generationInfo = generationInfo;

        addManagedSection(0, new TableDataSourceGenerationInfo(generationInfo));
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
                return 1;
            case 2:
                return 1;
            case 3:
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
                TableCellString cell = new TableCellString("listID", generationInfo.listID);
                cell.addPropertyListener(this);
                return new TableElementCell("List ID", cell);
            }
            case 2:
            {
                TableCellFloatNullable cell = new TableCellFloatNullable("weight", TableElements.toFloat(generationInfo.weight), 1, 0, 1000, "D", "C");
                cell.setScale(Scales.pow(5));
                cell.addPropertyListener(this);
                cell.setTooltip(IvTranslations.formatLines("structures.gui.random.weight.tooltip"));
                return new TableElementCell(IvTranslations.get("structures.gui.random.weight"), cell);
            }
            case 3:
            {
                if (index == 0)
                {
                    TableCellString cell = new TableCellString("positionX", String.valueOf(generationInfo.shiftX));
                    cell.setShowsValidityState(true);
                    cell.setValidityState(GuiValidityStateIndicator.State.VALID);
                    cell.addPropertyListener(this);
                    return new TableElementCell("Shift (x)", cell);
                }
                else if (index == 1)
                {
                    TableCellString cell = new TableCellString("positionY", String.valueOf(generationInfo.shiftY));
                    cell.setShowsValidityState(true);
                    cell.setValidityState(GuiValidityStateIndicator.State.VALID);
                    cell.addPropertyListener(this);
                    return new TableElementCell("Shift (y)", cell);
                }
                else if (index == 2)
                {
                    TableCellString cell = new TableCellString("positionZ", String.valueOf(generationInfo.shiftZ));
                    cell.setShowsValidityState(true);
                    cell.setValidityState(GuiValidityStateIndicator.State.VALID);
                    cell.addPropertyListener(this);
                    return new TableElementCell("Shift (z)", cell);
                }
            }
            case 4:
            {
                TableCellEnum cell = new TableCellEnum<>("front", generationInfo.front, TableDirections.getDirectionOptions(Directions.HORIZONTAL));
                cell.addPropertyListener(this);
                return new TableElementCell("Front", cell);
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
                case "listID":
                {
                    generationInfo.listID = (String) cell.getPropertyValue();
                    break;
                }
                case "weight":
                {
                    generationInfo.weight = TableElements.toDouble((Float) cell.getPropertyValue());
                    break;
                }
                case "positionX":
                {
                    Integer val = Ints.tryParse((String) cell.getPropertyValue());
                    generationInfo.shiftX = val != null ? val : 0;
                    ((TableCellString) cell).setValidityState(val != null ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.INVALID);
                    break;
                }
                case "positionY":
                {
                    Integer val = Ints.tryParse((String) cell.getPropertyValue());
                    generationInfo.shiftY = val != null ? val : 0;
                    ((TableCellString) cell).setValidityState(val != null ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.INVALID);
                    break;
                }
                case "positionZ":
                {
                    Integer val = Ints.tryParse((String) cell.getPropertyValue());
                    generationInfo.shiftZ = val != null ? val : 0;
                    ((TableCellString) cell).setValidityState(val != null ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.INVALID);
                    break;
                }
                case "front":
                {
                    generationInfo.front = (EnumFacing) cell.getPropertyValue();
                    break;
                }
            }
        }
    }
}
