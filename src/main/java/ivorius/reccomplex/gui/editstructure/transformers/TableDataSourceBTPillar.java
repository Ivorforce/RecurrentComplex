/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.transformers;

import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.transformers.TransformerPillar;
import ivorius.reccomplex.utils.BlockStates;
import net.minecraft.block.Block;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBTPillar extends TableDataSourceSegmented implements TableCellPropertyListener
{
    private TransformerPillar transformer;

    public TableDataSourceBTPillar(TransformerPillar transformer)
    {
        this.transformer = transformer;

        addManagedSection(0, TableDataSourceExpression.constructDefault("Sources", transformer.sourceMatcher));
    }

    public TransformerPillar getTransformer()
    {
        return transformer;
    }

    public void setTransformer(TransformerPillar transformer)
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
            if (index == 0)
            {
                TableCellString cell = TableDataSourceBTNatural.elementForBlock("destID", transformer.destState.getBlock());
                cell.addPropertyListener(this);
                return new TableElementCell("Dest Block", cell);
            }
            else if (index == 1)
            {
                TableCellInteger cell = new TableCellInteger("destMeta", BlockStates.getMetadata(transformer.destState), 0, 16);
                cell.addPropertyListener(this);
                return new TableElementCell("Dest Metadata", cell);
            }
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    @Override
    public void valueChanged(TableCellPropertyDefault cell)
    {
        if ("destID".equals(cell.getID()))
        {
            transformer.destState = BlockStates.fromMetadata((Block) Block.blockRegistry.getObject(cell.getPropertyValue()), BlockStates.getMetadata(transformer.destState));
            TableDataSourceBTNatural.setStateForBlockTextfield(((TableCellString) cell));
        }
        else if ("destMeta".equals(cell.getID()))
        {
            transformer.destState = transformer.destState.with((int) cell.getPropertyValue());
        }
    }
}
