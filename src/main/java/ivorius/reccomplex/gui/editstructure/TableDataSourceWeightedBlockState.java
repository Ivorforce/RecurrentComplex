/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.RCGuiTables;
import ivorius.reccomplex.gui.TableDataSourceBlockState;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.WeightedBlockState;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceWeightedBlockState extends TableDataSourceSegmented implements TableCellPropertyListener
{
    private WeightedBlockState weightedBlockState;

    private TableDelegate tableDelegate;
    private TableCellTitle parsed;

    private TableCellString idCell;
    private TableCellInteger metaCell;

    public TableDataSourceWeightedBlockState(WeightedBlockState weightedBlockState, TableDelegate tableDelegate)
    {
        this.weightedBlockState = weightedBlockState;
        this.tableDelegate = tableDelegate;

        addManagedSection(1, new TableDataSourceBlockState(weightedBlockState.state, state -> weightedBlockState.state = state, "Block", "Metadata"));
    }

    public static GuiValidityStateIndicator.State stateForNBTCompoundJson(String json)
    {
        if (json.length() == 0)
            return GuiValidityStateIndicator.State.VALID;

        NBTTagCompound nbtbase;

        try
        {
            nbtbase = JsonToNBT.getTagFromJson(json);
            if (nbtbase != null)
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
        switch (segment)
        {
            case 0:
            case 2:
                return 2;
            default:
                return super.sizeOfSegment(segment);
        }
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            return RCGuiTables.defaultWeightElement(this, weightedBlockState.weight);
        }
        else if (segment == 2)
        {
            TableCellString cell = new TableCellString("tileEntityInfo", weightedBlockState.tileEntityInfo);
            cell.addPropertyListener(this);
            cell.setShowsValidityState(true);
            cell.setValidityState(stateForNBTCompoundJson(weightedBlockState.tileEntityInfo));
            return new TableElementCell("Tile Entity NBT", cell);
        }

        return null;
    }

    @Override
    public void valueChanged(TableCellPropertyDefault cell)
    {
        if ("weight".equals(cell.getID()))
        {
            weightedBlockState.weight = TableElements.toDouble((Float) cell.getPropertyValue());
        }
        else if ("tileEntityInfo".equals(cell.getID()))
        {
            weightedBlockState.tileEntityInfo = (String) cell.getPropertyValue();
            ((TableCellString) cell).setValidityState(stateForNBTCompoundJson(weightedBlockState.tileEntityInfo));
        }
    }
}
