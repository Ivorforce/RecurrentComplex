/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.gui.FloatRange;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.gentypes.VanillaStructureGenerationInfo;
import ivorius.reccomplex.utils.Directions;

/**
 * Created by lukas on 07.10.14.
 */
public class TableDataSourceVanillaStructureGenerationInfo extends TableDataSourceSegmented implements TableElementPropertyListener
{
    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    private VanillaStructureGenerationInfo generationInfo;

    public TableDataSourceVanillaStructureGenerationInfo(TableNavigator navigator, TableDelegate tableDelegate, VanillaStructureGenerationInfo generationInfo)
    {
        this.navigator = navigator;
        this.tableDelegate = tableDelegate;
        this.generationInfo = generationInfo;

        addManagedSection(0, new TableDataSourceGenerationInfo(generationInfo));
        addManagedSection(4, new TableDataSourceExpression<>("Biomes", "reccomplex.expression.biome.tooltip", generationInfo.biomeMatcher));
    }

    @Override
    public int numberOfSegments()
    {
        return 6;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 2;
            case 5:
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
                TableElementList element = new TableElementList("type", "Type", "village", new TableElementList.Option("village", "Village"));
                element.addPropertyListener(this);
                return element;
            }
            case 2:
            {
                switch (index)
                {
                    case 0:
                    {
                        TableElementFloatNullable element = new TableElementFloatNullable("weight", "Weight", TableElements.toFloat(generationInfo.generationWeight), 1.0f, 0, 10, "D", "C");
                        element.addPropertyListener(this);
                        return element;
                    }
                    case 1:
                    {
                        TableElementList element = new TableElementList("front", "Front", Directions.serialize(generationInfo.front), TableDataSourceStructureListGenerationInfo.getDirectionOptions(Directions.HORIZONTAL));
                        element.addPropertyListener(this);
                        return element;
                    }
                }
            }
            case 3:
                switch (index)
                {
                    case 0:
                    {
                        TableElementFloatRange element = new TableElementFloatRange("baseLimit", "Amount (p. V.)", new FloatRange((float) generationInfo.minBaseLimit, (float) generationInfo.maxBaseLimit), 0, 10, 2);
                        element.addPropertyListener(this);
                        return element;
                    }
                    case 1:
                    {
                        TableElementFloatRange element = new TableElementFloatRange("scaledLimit", "Amount (scaled)", new FloatRange((float) generationInfo.minScaledLimit, (float) generationInfo.maxScaledLimit), 0, 10, 2);
                        element.addPropertyListener(this);
                        return element;
                    }
                }
                break;
            case 5:
                switch (index)
                {
                    case 0:
                    {
                        TableElementInteger element = new TableElementInteger("spawnX", "Spawn Shift X", generationInfo.spawnShift.x, -50, 50);
                        element.addPropertyListener(this);
                        return element;
                    }
                    case 1:
                    {
                        TableElementInteger element = new TableElementInteger("spawnY", "Spawn Shift Y", generationInfo.spawnShift.y, -50, 50);
                        element.addPropertyListener(this);
                        return element;
                    }
                    case 2:
                    {
                        TableElementInteger element = new TableElementInteger("spawnZ", "Spawn Shift Z", generationInfo.spawnShift.z, -50, 50);
                        element.addPropertyListener(this);
                        return element;
                    }
                }
                break;
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        switch (element.getID())
        {
            case "weight":
                generationInfo.generationWeight = TableElements.toDouble((Float) element.getPropertyValue());
                break;
            case "baseLimit":
            {
                FloatRange baseLimit = (FloatRange) element.getPropertyValue();
                generationInfo.minBaseLimit = baseLimit.getMin();
                generationInfo.maxBaseLimit = baseLimit.getMax();
                break;
            }
            case "scaledLimit":
            {
                FloatRange baseLimit = (FloatRange) element.getPropertyValue();
                generationInfo.minScaledLimit = baseLimit.getMin();
                generationInfo.maxScaledLimit = baseLimit.getMax();
                break;
            }
            case "spawnX":
            {
                BlockCoord coord = generationInfo.spawnShift;
                generationInfo.spawnShift = new BlockCoord((int) element.getPropertyValue(), coord.y, coord.z);
                break;
            }
            case "spawnY":
            {
                BlockCoord coord = generationInfo.spawnShift;
                generationInfo.spawnShift = new BlockCoord(coord.x, (int) element.getPropertyValue(), coord.z);
                break;
            }
            case "spawnZ":
            {
                BlockCoord coord = generationInfo.spawnShift;
                generationInfo.spawnShift = new BlockCoord(coord.x, coord.y, (int) element.getPropertyValue());
                break;
            }
            case "front":
                generationInfo.front = Directions.deserializeHorizontal((String) element.getPropertyValue());
                break;
        }
    }
}
