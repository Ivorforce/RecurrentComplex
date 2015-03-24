/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.transformers;

import ivorius.reccomplex.gui.editstructure.TableDataSourceDimensionGen;
import ivorius.reccomplex.gui.editstructure.TableDataSourceWeightedBlockStateList;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.transformers.TransformerReplaceAll;
import ivorius.reccomplex.utils.IvTranslations;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBTReplaceAll extends TableDataSourceSegmented implements TableElementPropertyListener, TableElementActionListener
{
    private TransformerReplaceAll transformer;

    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    private TableElementTitle parsed;

    public TableDataSourceBTReplaceAll(TransformerReplaceAll transformer, TableNavigator navigator, TableDelegate tableDelegate)
    {
        this.transformer = transformer;
        this.navigator = navigator;
        this.tableDelegate = tableDelegate;
    }

    public TransformerReplaceAll getTransformer()
    {
        return transformer;
    }

    public void setTransformer(TransformerReplaceAll transformer)
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
        return segment == 0 ? 2 : 1;
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
            {
                parsed = new TableElementTitle("parsed", "", StringUtils.abbreviate(TableDataSourceDimensionGen.parsedString(transformer.sourceMatcher), 60));
                parsed.setPositioning(TableElementTitle.Positioning.TOP);
                return parsed;
            }
        }
        else if (segment == 1)
        {
            TableElementButton element = new TableElementButton("dest", "Destinations", new TableElementButton.Action("edit", "Edit"));
            element.addListener(this);
            return element;
        }

        return null;
    }

    public static String byteArrayToHexString(byte[] bytes)
    {
        StringBuilder builder = new StringBuilder();

        for (byte aByte : bytes)
        {
            builder.append(String.format("%01X", aByte));
        }

        return builder.toString();
    }

    public static byte[] hexStringToByteArray(String string)
    {
        List<Byte> bytes = new ArrayList<>();

        for (int i = 0; i < string.length(); i++)
        {
            char aChar = string.charAt(i);
            byte aByte = (byte) Character.digit(aChar, 16);

            if (aByte >= 0)
            {
                bytes.add(aByte);
            }
        }

        byte[] byteArray = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++)
        {
            byteArray[i] = bytes.get(i);
        }

        return byteArray;
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

    @Override
    public void actionPerformed(TableElement element, String action)
    {
        if ("dest".equals(element.getID()))
        {
            GuiTable table = new GuiTable(tableDelegate, new TableDataSourceWeightedBlockStateList(transformer.destination, tableDelegate, navigator));
            navigator.pushTable(table);
        }
    }
}
