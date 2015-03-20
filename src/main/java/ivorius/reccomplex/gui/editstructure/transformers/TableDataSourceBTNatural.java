/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.transformers;

import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.editstructure.TableDataSourceDimensionGen;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.transformers.TransformerNatural;
import ivorius.reccomplex.utils.IvTranslations;
import net.minecraft.block.Block;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBTNatural extends TableDataSourceSegmented implements TableElementPropertyListener
{
    private TransformerNatural transformer;

    private TableElementTitle parsed;

    public TableDataSourceBTNatural(TransformerNatural transformer)
    {
        this.transformer = transformer;
    }

    public TransformerNatural getTransformer()
    {
        return transformer;
    }

    public void setTransformer(TransformerNatural transformer)
    {
        this.transformer = transformer;
    }

    @Override
    public int numberOfSegments()
    {
        return 1;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return 2;
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            if (index == 0)
            {
                TableElementString element = new TableElementString("source", "Sources", transformer.sourceMatcher.getExpression());
                element.setTooltip(IvTranslations.formatLines("reccomplex.expression.block.tooltip"));
                element.addPropertyListener(this);
                return element;
            }
            else if (index == 1)
                return parsed = new TableElementTitle("parsed", "", StringUtils.abbreviate(TableDataSourceDimensionGen.parsedString(transformer.sourceMatcher), 60));
        }

        return null;
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        if ("source".equals(element.getID()))
        {
            transformer.sourceMatcher.setExpression((String) element.getPropertyValue());
            if (parsed != null)
                parsed.setDisplayString(StringUtils.abbreviate(TableDataSourceDimensionGen.parsedString(transformer.sourceMatcher), 60));
        }
    }

    public static TableElementString elementForBlock(String id, String title, Block block)
    {
        TableElementString element = new TableElementString(id, title, Block.blockRegistry.getNameForObject(block));
        element.setShowsValidityState(true);
        setStateForBlockTextfield(element);
        return element;
    }

    public static void setStateForBlockTextfield(TableElementString elementString)
    {
        elementString.setValidityState(stateForBlock(elementString.getPropertyValue()));
    }

    public static GuiValidityStateIndicator.State stateForBlock(String blockID)
    {
        return Block.blockRegistry.containsKey(blockID) ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.INVALID;
    }
}
