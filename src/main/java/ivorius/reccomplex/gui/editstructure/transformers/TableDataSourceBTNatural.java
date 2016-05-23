/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.transformers;

import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.transformers.TransformerNatural;
import ivorius.reccomplex.utils.IvTranslations;
import ivorius.reccomplex.utils.scale.Scales;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBTNatural extends TableDataSourceSegmented implements TableCellPropertyListener
{
    private TransformerNatural transformer;

    public TableDataSourceBTNatural(TransformerNatural transformer)
    {
        this.transformer = transformer;

        addManagedSection(0, TableDataSourceExpression.constructDefault("Sources", transformer.sourceMatcher));
    }

    public static TableCellString elementForBlock(String id, Block block)
    {
        TableCellString element = new TableCellString(id, Block.blockRegistry.getNameForObject(block).toString());
        element.setShowsValidityState(true);
        setStateForBlockTextfield(element);
        return element;
    }

    public static void setStateForBlockTextfield(TableCellString elementString)
    {
        elementString.setValidityState(stateForBlock(elementString.getPropertyValue()));
    }

    public static GuiValidityStateIndicator.State stateForBlock(String blockID)
    {
        return Block.blockRegistry.containsKey(new ResourceLocation(blockID)) ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.INVALID;
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
        return 2;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return segment == 1 ? 2 : super.sizeOfSegment(segment);
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 1)
        {
            switch (index)
            {
                case 0:
                {
                    TableCellFloatNullable cell = new TableCellFloatNullable("naturalExpansionDistance", TableElements.toFloat(transformer.naturalExpansionDistance), 1.0f, 0, 40, "D", "C");
                    cell.setScale(Scales.pow(5));
                    cell.addPropertyListener(this);
                    cell.setTooltip(IvTranslations.formatLines("reccomplex.transformer.natural.naturalExpansionDistance.tooltip"));
                    return new TableElementCell(IvTranslations.get("reccomplex.transformer.natural.naturalExpansionDistance"), cell);
                }
                case 1:
                {
                    TableCellFloatNullable cell = new TableCellFloatNullable("naturalExpansionRandomization", TableElements.toFloat(transformer.naturalExpansionRandomization), 1.0f, 0, 40, "D", "C");
                    cell.setScale(Scales.pow(5));
                    cell.addPropertyListener(this);
                    cell.setTooltip(IvTranslations.formatLines("reccomplex.transformer.natural.naturalExpansionRandomization.tooltip"));
                    return new TableElementCell(IvTranslations.get("reccomplex.transformer.natural.naturalExpansionRandomization"), cell);
                }
            }
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
                case "naturalExpansionDistance":
                    transformer.naturalExpansionDistance = TableElements.toDouble((Float) cell.getPropertyValue());
                    break;
                case "naturalExpansionRandomization":
                    transformer.naturalExpansionRandomization = TableElements.toDouble((Float) cell.getPropertyValue());
                    break;
            }
        }
    }
}
