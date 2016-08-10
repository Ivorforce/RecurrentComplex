/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import ivorius.ivtoolkit.gui.FloatRange;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.reccomplex.gui.TableDataSourceBlockPos;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.TableDirections;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.gentypes.VanillaStructureGenerationInfo;
import ivorius.ivtoolkit.blocks.Directions;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.utils.scale.Scales;
import net.minecraft.util.EnumFacing;

/**
 * Created by lukas on 07.10.14.
 */
public class TableDataSourceVanillaStructureGenerationInfo extends TableDataSourceSegmented implements TableCellPropertyListener
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
        addManagedSection(4, TableDataSourceExpression.constructDefault("Biomes", generationInfo.biomeMatcher));
        addManagedSection(5, new TableDataSourceBlockPos(generationInfo.spawnShift, generationInfo::setSpawnShift, new IntegerRange(-50, 50), "Spawn Shift %s"));
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
                TableCellEnum cell = new TableCellEnum<>("type", "village", new TableCellEnum.Option<>("village", "Village"));
                cell.addPropertyListener(this);
                return new TableElementCell("Type", cell);
            }
            case 2:
            {
                switch (index)
                {
                    case 0:
                    {
                        TableCellFloatNullable cell = new TableCellFloatNullable("weight", TableElements.toFloat(generationInfo.generationWeight), 1.0f, 0, 1000, "D", "C");
                        cell.setScale(Scales.pow(5));
                        cell.addPropertyListener(this);
                        cell.setTooltip(IvTranslations.formatLines("structures.gui.random.weight.tooltip"));
                        return new TableElementCell(IvTranslations.get("structures.gui.random.weight"), cell);
                    }
                    case 1:
                    {
                        TableCellEnum cell = new TableCellEnum<>("front", generationInfo.front, TableDirections.getDirectionOptions(Directions.HORIZONTAL));
                        cell.addPropertyListener(this);
                        return new TableElementCell("Front", cell);
                    }
                }
            }
            case 3:
                switch (index)
                {
                    case 0:
                    {
                        TableCellFloatRange cell = new TableCellFloatRange("baseLimit", new FloatRange((float) generationInfo.minBaseLimit, (float) generationInfo.maxBaseLimit), 0, 1000, "%.2f");
                        cell.setScale(Scales.pow(5));
                        cell.addPropertyListener(this);
                        return new TableElementCell("Amount (p. V.)", cell);
                    }
                    case 1:
                    {
                        TableCellFloatRange cell = new TableCellFloatRange("scaledLimit", new FloatRange((float) generationInfo.minScaledLimit, (float) generationInfo.maxScaledLimit), 0, 1000, "%.2f");
                        cell.setScale(Scales.pow(5));
                        cell.addPropertyListener(this);
                        return new TableElementCell("Amount (scaled)", cell);
                    }
                }
                break;
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
                case "weight":
                    generationInfo.generationWeight = TableElements.toDouble((Float) cell.getPropertyValue());
                    break;
                case "baseLimit":
                {
                    FloatRange baseLimit = (FloatRange) cell.getPropertyValue();
                    generationInfo.minBaseLimit = baseLimit.getMin();
                    generationInfo.maxBaseLimit = baseLimit.getMax();
                    break;
                }
                case "scaledLimit":
                {
                    FloatRange baseLimit = (FloatRange) cell.getPropertyValue();
                    generationInfo.minScaledLimit = baseLimit.getMin();
                    generationInfo.maxScaledLimit = baseLimit.getMax();
                    break;
                }
                case "front":
                    generationInfo.front = (EnumFacing) cell.getPropertyValue();
                    break;
            }
        }
    }
}
