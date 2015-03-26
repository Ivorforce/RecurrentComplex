/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import com.google.common.primitives.Ints;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.GenerationYSelector;
import ivorius.reccomplex.structures.generic.gentypes.StaticGenerationInfo;
import ivorius.reccomplex.utils.IvTranslations;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by lukas on 07.10.14.
 */
public class TableDataSourceStaticGenerationInfo extends TableDataSourceSegmented implements TableElementPropertyListener
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
        addManagedSection(3, new TableDataSourceExpression<>("Dimensions", "reccomplex.expression.dimension.tooltip", generationInfo.dimensionMatcher));
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
            case 2:
                return 2;
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
                    TableElementBoolean element = new TableElementBoolean("relativeToSpawn", "At Spawn", generationInfo.relativeToSpawn);
                    element.addPropertyListener(this);
                    return element;
                }
                else if (index == 1)
                {
                    TableElementString element = new TableElementString("positionX", "Position (x)", String.valueOf(generationInfo.positionX));
                    element.setShowsValidityState(true);
                    element.setValidityState(GuiValidityStateIndicator.State.VALID);
                    element.addPropertyListener(this);
                    return element;
                }
                else if (index == 2)
                {
                    TableElementString element = new TableElementString("positionZ", "Position (z)", String.valueOf(generationInfo.positionZ));
                    element.setShowsValidityState(true);
                    element.setValidityState(GuiValidityStateIndicator.State.VALID);
                    element.addPropertyListener(this);
                    return element;
                }
            }
            case 2:
            {
                if (index == 0)
                {
                    TableElementList element = new TableElementList("ySelType", "Generation Base", generationInfo.ySelector.selectionMode.serializedName(), TableDataSourceNaturalGenerationInfo.allGenerationOptions());
                    element.addPropertyListener(this);
                    return element;
                }
                else if (index == 1)
                {
                    TableElementIntegerRange element = new TableElementIntegerRange("ySelShift", "Y Shift", new IntegerRange(generationInfo.ySelector.minY, generationInfo.ySelector.maxY), -100, 100);
                    element.addPropertyListener(this);
                    return element;
                }

                break;
            }
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        switch (element.getID())
        {
            case "positionX":
            {
                Integer val = Ints.tryParse((String) element.getPropertyValue());
                generationInfo.positionX = val != null ? val : 0;
                ((TableElementString) element).setValidityState(val != null ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.INVALID);
                break;
            }
            case "positionZ":
            {
                Integer val = Ints.tryParse((String) element.getPropertyValue());
                generationInfo.positionZ = val != null ? val : 0;
                ((TableElementString) element).setValidityState(val != null ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.INVALID);
                break;
            }
            case "relativeToSpawn":
                generationInfo.relativeToSpawn = (boolean) element.getPropertyValue();
                break;
            case "ySelType":
                GenerationYSelector.SelectionMode selectionMode = GenerationYSelector.SelectionMode.selectionMode((String) element.getPropertyValue());
                generationInfo.ySelector.selectionMode = selectionMode != null ? selectionMode : GenerationYSelector.SelectionMode.SURFACE;
                break;
            case "ySelShift":
                IntegerRange range = ((IntegerRange) element.getPropertyValue());
                generationInfo.ySelector.minY = range.getMin();
                generationInfo.ySelector.maxY = range.getMax();
                break;
        }
    }
}
