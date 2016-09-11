/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.transformers;

import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.transformers.TransformerNatural;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.utils.scale.Scales;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBTNatural extends TableDataSourceSegmented
{
    private TransformerNatural transformer;

    public TableDataSourceBTNatural(TransformerNatural transformer, TableNavigator navigator, TableDelegate delegate)
    {
        this.transformer = transformer;

        addManagedSection(0, new TableDataSourceTransformer(transformer, delegate, navigator));
        addManagedSection(1, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.gui.sources"), transformer.sourceMatcher, null));
    }

    public static TableCellString elementForBlock(String id, String block)
    {
        TableCellString element = new TableCellString(id, block);
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
        return Block.REGISTRY.containsKey(new ResourceLocation(blockID)) ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.INVALID;
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
        return 3;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return segment == 2 ? 2 : super.sizeOfSegment(segment);
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 2)
        {
            switch (index)
            {
                case 0:
                {
                    TableCellFloat cell = new TableCellFloat("naturalExpansionDistance", TableElements.toFloat(transformer.naturalExpansionDistance), 0, 40);
                    cell.setScale(Scales.pow(5));
                    cell.addPropertyConsumer(val -> transformer.naturalExpansionDistance = TableElements.toDouble(val));
                    return new TableElementCell(IvTranslations.get("reccomplex.transformer.natural.naturalExpansionDistance"), cell)
                            .withTitleTooltip(IvTranslations.formatLines("reccomplex.transformer.natural.naturalExpansionDistance.tooltip"));
                }
                case 1:
                {
                    TableCellFloat cell = new TableCellFloat("naturalExpansionRandomization", TableElements.toFloat(transformer.naturalExpansionRandomization), 0, 40);
                    cell.setScale(Scales.pow(5));
                    cell.addPropertyConsumer(val -> transformer.naturalExpansionRandomization = TableElements.toDouble(val));
                    return new TableElementCell(IvTranslations.get("reccomplex.transformer.natural.naturalExpansionRandomization"), cell)
                            .withTitleTooltip(IvTranslations.formatLines("reccomplex.transformer.natural.naturalExpansionRandomization.tooltip"));
                }
            }
        }

        return super.elementForIndexInSegment(table, index, segment);
    }
}
