/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.editstructure.transformers.TableDataSourceBTNatural;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.utils.BlockStates;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;

/**
 * Created by lukas on 06.05.16.
 */
public class TableDataSourceBlockState extends TableDataSourceSegmented implements TableCellPropertyListener
{
    private IBlockState state;
    private Consumer<IBlockState> consumer;

    private String blockTitle;
    private String metadataTitle;

    private TableCellString idCell;
    private TableCellInteger metaCell;

    public TableDataSourceBlockState(IBlockState state, Consumer<IBlockState> consumer, String blockTitle, String metadataTitle)
    {
        this.state = state;
        this.consumer = consumer;
        this.blockTitle = blockTitle;
        this.metadataTitle = metadataTitle;
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
        if (index == 0)
        {
            TableCellString cell = idCell = TableDataSourceBTNatural.elementForBlock("block", state.getBlock());
            cell.addPropertyListener(this);
            return new TableElementCell(blockTitle, cell);
        }
        else if (index == 1)
        {
            TableCellInteger cell = metaCell = new TableCellInteger("metadata", BlockStates.toMetadata(state), 0, 15);
            cell.addPropertyListener(this);
            return new TableElementCell(metadataTitle, cell);
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    @Override
    public void valueChanged(TableCellPropertyDefault cell)
    {
        consumer.accept(state = computeBlockState());
    }

    public IBlockState computeBlockState()
    {
        Block block = RecurrentComplex.mcRegistry.blockFromID(new ResourceLocation(idCell.getPropertyValue()));
        return block != null ? block.getStateFromMeta(metaCell.getPropertyValue()) : null;
    }
}
