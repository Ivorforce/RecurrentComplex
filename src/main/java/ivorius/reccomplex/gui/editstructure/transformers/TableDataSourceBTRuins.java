/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.transformers;

import ivorius.ivtoolkit.gui.FloatRange;
import ivorius.reccomplex.gui.TableDirections;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.transformers.TransformerRuins;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.utils.scale.Scales;
import net.minecraft.util.EnumFacing;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBTRuins extends TableDataSourceSegmented
{
    private TransformerRuins transformer;

    public TableDataSourceBTRuins(TransformerRuins transformer, TableNavigator navigator, TableDelegate delegate)
    {
        this.transformer = transformer;

        addManagedSection(0, new TableDataSourceTransformer(transformer, delegate, navigator));
    }

    public TransformerRuins getTransformer()
    {
        return transformer;
    }

    public void setTransformer(TransformerRuins transformer)
    {
        this.transformer = transformer;
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
            case 1:
                return 5;
            case 2:
                return 3;
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
                switch (index)
                {
                    case 0:
                        return new TableElementCell(new TableCellTitle("decayTitle", IvTranslations.get("reccomplex.transformer.ruins.decay.title")));
                    case 1:
                    {
                        TableCellFloatRange cell = new TableCellFloatRange("decay", new FloatRange(transformer.minDecay, transformer.maxDecay), 0.0f, 1.0f, "%.4f");
                        cell.setScale(Scales.pow(5));
                        cell.setTooltip(IvTranslations.formatLines("reccomplex.transformer.ruins.decay.base.tooltip"));
                        cell.addPropertyConsumer(val -> {
                            transformer.minDecay = val.getMin();
                            transformer.maxDecay = val.getMax();
                        });
                        return new TableElementCell(IvTranslations.get("reccomplex.transformer.ruins.decay.base"), cell);
                    }
                    case 2:
                    {
                        TableCellFloat cell = new TableCellFloat("decayChaos", transformer.decayChaos, 0.0f, 1.0f);
                        cell.setScale(Scales.pow(3));
                        cell.setTooltip(IvTranslations.formatLines("reccomplex.transformer.ruins.decay.chaos.tooltip"));
                        cell.addPropertyConsumer(val -> transformer.decayChaos = val);
                        return new TableElementCell(IvTranslations.get("reccomplex.transformer.ruins.decay.chaos"), cell);
                    }
                    case 3:
                    {
                        TableCellFloat cell = new TableCellFloat("decayValueDensity", transformer.decayValueDensity, 0.0f, 1.0f);
                        cell.setScale(Scales.pow(3));
                        cell.setTooltip(IvTranslations.formatLines("reccomplex.transformer.ruins.decay.density.tooltip"));
                        cell.addPropertyConsumer(val -> transformer.decayValueDensity = val);
                        return new TableElementCell(IvTranslations.get("reccomplex.transformer.ruins.decay.density"), cell);
                    }
                    case 4:
                    {
                        TableCellEnum<EnumFacing> cell = new TableCellEnum<>("decaySide", transformer.decayDirection, TableDirections.getDirectionOptions(EnumFacing.VALUES));
                        cell.setTooltip(IvTranslations.formatLines("reccomplex.transformer.ruins.decay.direction.tooltip"));
                        cell.addPropertyConsumer(val -> transformer.decayDirection = val);
                        return new TableElementCell(IvTranslations.get("reccomplex.transformer.ruins.decay.direction"), cell);
                    }
                }
                break;
            case 2:
                switch (index)
                {
                    case 0:
                        return new TableElementCell(new TableCellTitle("otherTitle", IvTranslations.get("reccomplex.transformer.ruins.other.title")));
                    case 1:
                    {
                        TableCellFloat cell = new TableCellFloat("erosion", transformer.blockErosion, 0.0f, 1.0f);
                        cell.setScale(Scales.pow(3));
                        cell.setTooltip(IvTranslations.formatLines("reccomplex.transformer.ruins.erosion.tooltip"));
                        cell.addPropertyConsumer(val -> transformer.blockErosion = val);
                        return new TableElementCell(IvTranslations.get("reccomplex.transformer.ruins.erosion"), cell);
                    }
                    case 2:
                    {
                        TableCellFloat cell = new TableCellFloat("vines", transformer.vineGrowth, 0.0f, 1.0f);
                        cell.setScale(Scales.pow(3));
                        cell.setTooltip(IvTranslations.formatLines("reccomplex.transformer.ruins.vines.tooltip"));
                        cell.addPropertyConsumer(val -> transformer.vineGrowth = val);
                        return new TableElementCell(IvTranslations.get("reccomplex.transformer.ruins.vines"), cell);
                    }
                }
                break;
        }

        return super.elementForIndexInSegment(table, index, segment);
    }
}
