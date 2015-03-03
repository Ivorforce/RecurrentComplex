/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.blocktransformers;

import ivorius.reccomplex.gui.editstructure.TableDataSourceDimensionGen;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.blocktransformers.BlockTransformerPillar;
import net.minecraft.block.Block;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBTPillar extends TableDataSourceSegmented implements TableElementPropertyListener
{
    private BlockTransformerPillar blockTransformer;

    private TableElementTitle parsed;

    public TableDataSourceBTPillar(BlockTransformerPillar blockTransformer)
    {
        this.blockTransformer = blockTransformer;
    }

    public BlockTransformerPillar getBlockTransformer()
    {
        return blockTransformer;
    }

    public void setBlockTransformer(BlockTransformerPillar blockTransformer)
    {
        this.blockTransformer = blockTransformer;
    }

    @Override
    public int numberOfSegments()
    {
        return 2;
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
                TableElementString element = new TableElementString("source", "Sources", blockTransformer.sourceMatcher.getExpression());
                element.addPropertyListener(this);
                return element;
            }
            else if (index == 1)
                return parsed = new TableElementTitle("parsed", "", StringUtils.abbreviate(TableDataSourceDimensionGen.parsedString(blockTransformer.sourceMatcher), 60));
        }
        else if (segment == 1)
        {
            if (index == 0)
            {
                TableElementString element = TableDataSourceBTNatural.elementForBlock("destID", "Dest Block", blockTransformer.destBlock);
                element.addPropertyListener(this);
                return element;
            }
            else if (index == 1)
            {
                TableElementInteger element = new TableElementInteger("destMeta", "Dest Metadata", blockTransformer.destMetadata, 0, 16);
                element.addPropertyListener(this);
                return element;
            }
        }

        return null;
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        if ("source".equals(element.getID()))
        {
            blockTransformer.sourceMatcher.setExpression((String) element.getPropertyValue());
            if (parsed != null)
                parsed.setDisplayString(StringUtils.abbreviate(TableDataSourceDimensionGen.parsedString(blockTransformer.sourceMatcher), 60));
        }
        else if ("destID".equals(element.getID()))
        {
            blockTransformer.destBlock = (Block) Block.blockRegistry.getObject(element.getPropertyValue());
            TableDataSourceBTNatural.setStateForBlockTextfield(((TableElementString) element));
        }
        else if ("destMeta".equals(element.getID()))
        {
            blockTransformer.destMetadata = (int) element.getPropertyValue();
        }
    }
}
