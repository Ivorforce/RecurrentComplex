/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.BiomeGenerationInfo;
import ivorius.reccomplex.utils.IvTranslations;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBiomeGen extends TableDataSourceSegmented implements TableElementPropertyListener
{
    private BiomeGenerationInfo generationInfo;

    private TableDelegate tableDelegate;
    private TableElementTitle parsed;

    public TableDataSourceBiomeGen(BiomeGenerationInfo generationInfo, TableDelegate tableDelegate)
    {
        this.generationInfo = generationInfo;
        this.tableDelegate = tableDelegate;
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
                TableElementString element = new TableElementString("biomeID", "Biomes", generationInfo.getBiomeMatcher().getExpression());
                element.setTooltip(IvTranslations.formatLines("reccomplex.expression.biome.tooltip"));
                element.addPropertyListener(this);
                return element;
            }
            else if (index == 1)
            {
                parsed = new TableElementTitle("parsed", "", StringUtils.abbreviate(TableDataSourceDimensionGen.parsedString(generationInfo.getBiomeMatcher()), 60));
                parsed.setPositioning(TableElementTitle.Positioning.TOP);
                return parsed;
            }
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
        if ("biomeID".equals(element.getID()))
        {
            generationInfo.getBiomeMatcher().setExpression((String) element.getPropertyValue());
            if (parsed != null)
                parsed.setDisplayString(StringUtils.abbreviate(TableDataSourceDimensionGen.parsedString(generationInfo.getBiomeMatcher()), 60));
        }
        else if ("weight".equals(element.getID()))
        {
            generationInfo.setGenerationWeight(TableElements.toDouble((Float) element.getPropertyValue()));
        }
    }
}
