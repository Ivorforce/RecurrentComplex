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
public class TableDataSourceBTRuins extends TableDataSourceSegmented implements TableElementPropertyListener
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
                        return new TableElementTitle("decayTitle", "", IvTranslations.get("reccomplex.transformer.ruins.decay.title"));
                    case 1:
                    {
                        TableElementFloatRange element = new TableElementFloatRange("decay", IvTranslations.get("reccomplex.transformer.ruins.decay.base"), new FloatRange(transformer.minDecay, transformer.maxDecay), 0.0f, 1.0f, 2);
                        element.setTooltip(IvTranslations.formatLines("reccomplex.transformer.ruins.decay.base.tooltip"));
                        element.addPropertyListener(this);
                        return element;
                    }
                    case 2:
                    {
                        TableElementFloat element = new TableElementFloat("decayChaos", IvTranslations.get("reccomplex.transformer.ruins.decay.chaos"), transformer.decayChaos, 0.0f, 1.0f);
                        element.setTooltip(IvTranslations.formatLines("reccomplex.transformer.ruins.decay.chaos.tooltip"));
                        element.addPropertyListener(this);
                        return element;
                    }
                    case 3:
                    {
                        TableElementFloat element = new TableElementFloat("decayValueDensity", IvTranslations.get("reccomplex.transformer.ruins.decay.density"), transformer.decayValueDensity, 0.0f, 1.0f);
                        element.setTooltip(IvTranslations.formatLines("reccomplex.transformer.ruins.decay.density.tooltip"));
                        element.addPropertyListener(this);
                        return element;
                    }
                    case 4:
                    {
                        TableElementEnum element = new TableElementEnum<>("decaySide", IvTranslations.get("reccomplex.transformer.ruins.decay.direction"), transformer.decayDirection, TableDirections.getDirectionOptions(ForgeDirection.VALID_DIRECTIONS));
                        element.setTooltip(IvTranslations.formatLines("reccomplex.transformer.ruins.decay.direction.tooltip"));
                        element.addPropertyListener(this);
                        return element;
                    }
                }
                break;
            case 1:
                switch (index)
                {
                    case 0:
                        return new TableElementTitle("otherTitle", "", IvTranslations.get("reccomplex.transformer.ruins.other.title"));
                    case 1:
                    {
                        TableElementFloat element = new TableElementFloat("erosion", IvTranslations.get("reccomplex.transformer.ruins.erosion"), transformer.blockErosion, 0.0f, 1.0f);
                        element.setTooltip(IvTranslations.formatLines("reccomplex.transformer.ruins.erosion.tooltip"));
                        element.addPropertyListener(this);
                        return element;
                    }
                    case 2:
                    {
                        TableElementFloat element = new TableElementFloat("vines", IvTranslations.get("reccomplex.transformer.ruins.vines"), transformer.vineGrowth, 0.0f, 1.0f);
                        element.setTooltip(IvTranslations.formatLines("reccomplex.transformer.ruins.vines.tooltip"));
                        element.addPropertyListener(this);
                        return element;
                    }
                }
                break;
        }

        return null;
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        switch (element.getID())
        {
            case "decaySide":
                transformer.decayDirection = (ForgeDirection) element.getPropertyValue();
                break;
            case "decay":
                FloatRange range = (FloatRange) element.getPropertyValue();
                transformer.minDecay = range.getMin();
                transformer.maxDecay = range.getMax();
                break;
            case "decayChaos":
                transformer.decayChaos = (float) element.getPropertyValue();
                break;
            case "decayValueDensity":
                transformer.decayValueDensity = (float) element.getPropertyValue();
                break;
            case "erosion":
                transformer.blockErosion = (float) element.getPropertyValue();
                break;
            case "vines":
                transformer.vineGrowth = (float) element.getPropertyValue();
                break;
        }
    }
}
