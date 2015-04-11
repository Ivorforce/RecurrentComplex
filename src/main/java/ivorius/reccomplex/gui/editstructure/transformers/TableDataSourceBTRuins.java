/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.transformers;

import ivorius.ivtoolkit.gui.FloatRange;
import ivorius.reccomplex.gui.TableDirections;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.transformers.TransformerRuins;
import ivorius.reccomplex.utils.IvTranslations;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBTRuins extends TableDataSourceSegmented implements TableCellPropertyListener
{
    private TransformerRuins transformer;

    public TableDataSourceBTRuins(TransformerRuins transformer)
    {
        this.transformer = transformer;
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
        return 2;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return segment == 0 ? 5 : 3;
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        switch (segment)
        {
            case 0:
                switch (index)
                {
                    case 0:
                        return new TableElementCell(new TableCellTitle("decayTitle", IvTranslations.get("reccomplex.transformer.ruins.decay.title")));
                    case 1:
                    {
                        TableCellFloatRange cell = new TableCellFloatRange("decay", new FloatRange(transformer.minDecay, transformer.maxDecay), 0.0f, 1.0f, 2);
                        cell.setTooltip(IvTranslations.formatLines("reccomplex.transformer.ruins.decay.base.tooltip"));
                        cell.addPropertyListener(this);
                        return new TableElementCell(IvTranslations.get("reccomplex.transformer.ruins.decay.base"), cell);
                    }
                    case 2:
                    {
                        TableCellFloat cell = new TableCellFloat("decayChaos", transformer.decayChaos, 0.0f, 1.0f);
                        cell.setTooltip(IvTranslations.formatLines("reccomplex.transformer.ruins.decay.chaos.tooltip"));
                        cell.addPropertyListener(this);
                        return new TableElementCell(IvTranslations.get("reccomplex.transformer.ruins.decay.chaos"), cell);
                    }
                    case 3:
                    {
                        TableCellFloat cell = new TableCellFloat("decayValueDensity", transformer.decayValueDensity, 0.0f, 1.0f);
                        cell.setTooltip(IvTranslations.formatLines("reccomplex.transformer.ruins.decay.density.tooltip"));
                        cell.addPropertyListener(this);
                        return new TableElementCell(IvTranslations.get("reccomplex.transformer.ruins.decay.density"), cell);
                    }
                    case 4:
                    {
                        TableCellEnum cell = new TableCellEnum<>("decaySide", transformer.decayDirection, TableDirections.getDirectionOptions(ForgeDirection.VALID_DIRECTIONS));
                        cell.setTooltip(IvTranslations.formatLines("reccomplex.transformer.ruins.decay.direction.tooltip"));
                        cell.addPropertyListener(this);
                        return new TableElementCell(IvTranslations.get("reccomplex.transformer.ruins.decay.direction"), cell);
                    }
                }
                break;
            case 1:
                switch (index)
                {
                    case 0:
                        return new TableElementCell(new TableCellTitle("otherTitle", IvTranslations.get("reccomplex.transformer.ruins.other.title")));
                    case 1:
                    {
                        TableCellFloat element = new TableCellFloat("erosion", transformer.blockErosion, 0.0f, 1.0f);
                        element.setTooltip(IvTranslations.formatLines("reccomplex.transformer.ruins.erosion.tooltip"));
                        element.addPropertyListener(this);
                        return new TableElementCell(IvTranslations.get("reccomplex.transformer.ruins.erosion"), element);
                    }
                    case 2:
                    {
                        TableCellFloat element = new TableCellFloat("vines", transformer.vineGrowth, 0.0f, 1.0f);
                        element.setTooltip(IvTranslations.formatLines("reccomplex.transformer.ruins.vines.tooltip"));
                        element.addPropertyListener(this);
                        return new TableElementCell(IvTranslations.get("reccomplex.transformer.ruins.vines"), element);
                    }
                }
                break;
        }

        return null;
    }

    @Override
    public void valueChanged(TableCellPropertyDefault cell)
    {
        if (cell.getID() != null)
        {
            switch (cell.getID())
            {
                case "decaySide":
                    transformer.decayDirection = (ForgeDirection) cell.getPropertyValue();
                    break;
                case "decay":
                    FloatRange range = (FloatRange) cell.getPropertyValue();
                    transformer.minDecay = range.getMin();
                    transformer.maxDecay = range.getMax();
                    break;
                case "decayChaos":
                    transformer.decayChaos = (float) cell.getPropertyValue();
                    break;
                case "decayValueDensity":
                    transformer.decayValueDensity = (float) cell.getPropertyValue();
                    break;
                case "erosion":
                    transformer.blockErosion = (float) cell.getPropertyValue();
                    break;
                case "vines":
                    transformer.vineGrowth = (float) cell.getPropertyValue();
                    break;
            }
        }
    }
}
