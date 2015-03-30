/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import com.google.common.primitives.Ints;
import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.gentypes.StructureListGenerationInfo;
import ivorius.ivtoolkit.blocks.Directions;
import ivorius.reccomplex.utils.DirectionNames;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Created by lukas on 07.10.14.
 */
public class TableDataSourceStructureListGenerationInfo extends TableDataSourceSegmented implements TableElementPropertyListener
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

    public static TableElementList.Option[] getDirectionOptions(ForgeDirection[] directions)
    {
        TableElementList.Option[] options = new TableElementList.Option[directions.length];
        for (int i = 0; i < options.length; i++)
            options[i] = new TableElementList.Option(Directions.serialize(directions[i]), DirectionNames.of(directions[i]));
        return options;
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
                TableElementString element = new TableElementString("listID", "List ID", generationInfo.listID);
                element.addPropertyListener(this);
                return element;
            }
            case 2:
            {
                TableElementFloatNullable element = new TableElementFloatNullable("weight", "Weight", TableElements.toFloat(generationInfo.weight), 1, 0, 10, "D", "C");
                element.addPropertyListener(this);
                return element;
            }
            case 3:
            {
                if (index == 0)
                {
                    TableElementString element = new TableElementString("positionX", "Shift (x)", String.valueOf(generationInfo.shiftX));
                    element.setShowsValidityState(true);
                    element.setValidityState(GuiValidityStateIndicator.State.VALID);
                    element.addPropertyListener(this);
                    return element;
                }
                else if (index == 1)
                {
                    TableElementString element = new TableElementString("positionY", "Shift (y)", String.valueOf(generationInfo.shiftY));
                    element.setShowsValidityState(true);
                    element.setValidityState(GuiValidityStateIndicator.State.VALID);
                    element.addPropertyListener(this);
                    return element;
                }
                else if (index == 2)
                {
                    TableElementString element = new TableElementString("positionZ", "Shift (z)", String.valueOf(generationInfo.shiftZ));
                    element.setShowsValidityState(true);
                    element.setValidityState(GuiValidityStateIndicator.State.VALID);
                    element.addPropertyListener(this);
                    return element;
                }
            }
            case 4:
            {
                TableElementList element = new TableElementList("front", "Front", Directions.serialize(generationInfo.front), getDirectionOptions(Directions.HORIZONTAL));
                element.addPropertyListener(this);
                return element;
            }
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        switch (element.getID())
        {
            case "listID":
            {
                generationInfo.listID = (String) element.getPropertyValue();
                break;
            }
            case "weight":
            {
                generationInfo.weight = TableElements.toDouble((Float) element.getPropertyValue());
                break;
            }
            case "positionX":
            {
                Integer val = Ints.tryParse((String) element.getPropertyValue());
                generationInfo.shiftX = val != null ? val : 0;
                ((TableElementString) element).setValidityState(val != null ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.INVALID);
                break;
            }
            case "positionY":
            {
                Integer val = Ints.tryParse((String) element.getPropertyValue());
                generationInfo.shiftY = val != null ? val : 0;
                ((TableElementString) element).setValidityState(val != null ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.INVALID);
                break;
            }
            case "positionZ":
            {
                Integer val = Ints.tryParse((String) element.getPropertyValue());
                generationInfo.shiftZ = val != null ? val : 0;
                ((TableElementString) element).setValidityState(val != null ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.INVALID);
                break;
            }
            case "front":
            {
                generationInfo.front = Directions.deserializeHorizontal((String) element.getPropertyValue());
                break;
            }
        }
    }
}
