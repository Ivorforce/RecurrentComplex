/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.editstructure.blocktransformers.TableDataSourceBTNatural;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.WeightedBlockState;
import net.minecraft.block.Block;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceWeightedBlockState extends TableDataSourceSegmented implements TableElementPropertyListener
{
    private WeightedBlockState weightedBlockState;

    private TableDelegate tableDelegate;
    private TableElementTitle parsed;

    public TableDataSourceWeightedBlockState(WeightedBlockState weightedBlockState, TableDelegate tableDelegate)
    {
        this.weightedBlockState = weightedBlockState;
        this.tableDelegate = tableDelegate;
    }

    public static GuiValidityStateIndicator.State stateForNBTCompoundJson(String json)
    {
        if (json.length() == 0)
            return GuiValidityStateIndicator.State.VALID;

        NBTBase nbtbase;

        try
        {
            nbtbase = JsonToNBT.func_150315_a(json);
            if (nbtbase instanceof NBTTagCompound)
                return GuiValidityStateIndicator.State.VALID;
        }
        catch (NBTException ignored)
        {

        }

        return GuiValidityStateIndicator.State.INVALID;
    }

    @Override
    public int numberOfSegments()
    {
        return 3;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return segment == 1 ? 2 : 1;
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            TableElementFloatNullable element = new TableElementFloatNullable("weight", "Weight", TableElements.toFloat(weightedBlockState.weight), 1.0f, 0, 10, "D", "C");
            element.addPropertyListener(this);
            return element;
        }
        else if (segment == 1)
        {
            if (index == 0)
            {
                TableElementString element = TableDataSourceBTNatural.elementForBlock("block", "Block", weightedBlockState.block);
                element.addPropertyListener(this);
                return element;
            }
            else if (index == 1)
            {
                TableElementInteger element = new TableElementInteger("metadata", "Metadata", weightedBlockState.metadata, 0, 15);
                element.addPropertyListener(this);
                return element;
            }
        }
        else if (segment == 2)
        {
            TableElementString element = new TableElementString("tileEntityInfo", "Tile Entity NBT", weightedBlockState.tileEntityInfo);
            element.addPropertyListener(this);
            element.setShowsValidityState(true);
            element.setValidityState(stateForNBTCompoundJson(weightedBlockState.tileEntityInfo));
            return element;
        }

        return null;
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        if ("weight".equals(element.getID()))
        {
            weightedBlockState.weight = TableElements.toDouble((Float) element.getPropertyValue());
        }
        else if ("block".equals(element.getID()))
        {
            weightedBlockState.block = (Block) Block.blockRegistry.getObject(element.getPropertyValue());
            TableDataSourceBTNatural.setStateForBlockTextfield(((TableElementString) element));
        }
        else if ("metadata".equals(element.getID()))
        {
            weightedBlockState.metadata = (int) element.getPropertyValue();
        }
        else if ("tileEntityInfo".equals(element.getID()))
        {
            weightedBlockState.tileEntityInfo = (String) element.getPropertyValue();
            ((TableElementString) element).setValidityState(stateForNBTCompoundJson(weightedBlockState.tileEntityInfo));
        }
    }
}
