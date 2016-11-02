/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.transformers;

import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.cell.TableCellFloat;
import ivorius.reccomplex.gui.table.cell.TableCellString;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.structure.generic.transformers.TransformerNatural;
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

        addManagedSegment(0, new TableDataSourceTransformer(transformer, delegate, navigator));
        addManagedSegment(1, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.gui.sources"), transformer.sourceMatcher, null));
    }

    public static TableCellString cellForBlock(String id, String block)
    {
        TableCellString cell = new TableCellString(id, block);
        cell.setShowsValidityState(true);
        setStateForBlockTextfield(cell);
        return cell;
    }

    public static void setStateForBlockTextfield(TableCellString cell)
    {
        cell.setValidityState(stateForBlock(cell.getPropertyValue()));
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
    public TableCell cellForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 2)
        {
            switch (index)
            {
                case 0:
                {
                    TableCellFloat cell = new TableCellFloat("naturalExpansionDistance", TableCells.toFloat(transformer.naturalExpansionDistance), 0, 40);
                    cell.setScale(Scales.pow(5));
                    cell.addPropertyConsumer(val -> transformer.naturalExpansionDistance = TableCells.toDouble(val));
                    return new TitledCell(IvTranslations.get("reccomplex.transformer.natural.naturalExpansionDistance"), cell)
                            .withTitleTooltip(IvTranslations.formatLines("reccomplex.transformer.natural.naturalExpansionDistance.tooltip"));
                }
                case 1:
                {
                    TableCellFloat cell = new TableCellFloat("naturalExpansionRandomization", TableCells.toFloat(transformer.naturalExpansionRandomization), 0, 40);
                    cell.setScale(Scales.pow(5));
                    cell.addPropertyConsumer(val -> transformer.naturalExpansionRandomization = TableCells.toDouble(val));
                    return new TitledCell(IvTranslations.get("reccomplex.transformer.natural.naturalExpansionRandomization"), cell)
                            .withTitleTooltip(IvTranslations.formatLines("reccomplex.transformer.natural.naturalExpansionRandomization.tooltip"));
                }
            }
        }

        return super.cellForIndexInSegment(table, index, segment);
    }
}
