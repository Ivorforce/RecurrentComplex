/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.DimensionGenerationInfo;
import ivorius.reccomplex.utils.ExpressionCache;
import ivorius.reccomplex.utils.IvTranslations;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceDimensionGen extends TableDataSourceSegmented implements TableElementPropertyListener
{
    private DimensionGenerationInfo generationInfo;

    private TableDelegate tableDelegate;
    private TableElementTitle parsed;

    public TableDataSourceDimensionGen(DimensionGenerationInfo generationInfo, TableDelegate tableDelegate)
    {
        this.generationInfo = generationInfo;
        this.tableDelegate = tableDelegate;
    }

    public static String parsedString(ExpressionCache expressionCache)
    {
        if (expressionCache.isExpressionValid())
            return expressionCache.getDisplayString();
        else
        {
            ParseException parseException = expressionCache.getParseException();
            return String.format("%s%s%s: at %d", EnumChatFormatting.RED, parseException.getMessage(), EnumChatFormatting.RESET,
                    parseException.getErrorOffset());
        }
    }

    @Override
    public int numberOfSegments()
    {
        return 2;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return segment == 0 ? 2 : 1;
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            if (index == 0)
            {
                TableElementString element = new TableElementString("dimID", "Dimensions", generationInfo.getDimensionMatcher().getExpression());
                element.setTooltip(IvTranslations.formatLines("reccomplex.expression.dimension.tooltip"));
                element.addPropertyListener(this);
                return element;
            }
            else if (index == 1)
                return parsed = new TableElementTitle("parsed", "", StringUtils.abbreviate(parsedString(generationInfo.getDimensionMatcher()), 70));
        }
        else if (segment == 1)
        {
            TableElementFloatNullable element = new TableElementFloatNullable("weight", "Weight", TableElements.toFloat(generationInfo.getGenerationWeight()), 1.0f, 0, 10, "D", "C");
            element.addPropertyListener(this);
            return element;
        }

        return null;
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        if ("dimID".equals(element.getID()))
        {
            generationInfo.getDimensionMatcher().setExpression((String) element.getPropertyValue());
            if (parsed != null)
                parsed.setDisplayString(StringUtils.abbreviate(parsedString(generationInfo.getDimensionMatcher()), 70));
        }
        else if ("weight".equals(element.getID()))
        {
            generationInfo.setGenerationWeight(TableElements.toDouble((Float) element.getPropertyValue()));
        }
    }
}
