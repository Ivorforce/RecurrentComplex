/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.ivtoolkit.tools.IvTranslations;
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
public class TableDataSourceWeightedBlockState extends TableDataSourceSegmented
{
    private WeightedBlockState weightedBlockState;

    public TableDataSourceWeightedBlockState(WeightedBlockState weightedBlockState, TableNavigator navigator, TableDelegate delegate)
    {
        this.weightedBlockState = weightedBlockState;

        addManagedSegment(1, new TableDataSourceBlockState(weightedBlockState.state, state -> weightedBlockState.state = state, navigator, delegate, "Block", "Metadata"));
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
                return 1;
            default:
                return super.sizeOfSegment(segment);
        }
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            return RCGuiTables.defaultWeightElement(val -> weightedBlockState.weight = TableElements.toDouble(val), weightedBlockState.weight);
        }
        else if (segment == 2)
        {
            TableCellString cell = new TableCellString("tileEntityInfo", weightedBlockState.tileEntityInfo);
            cell.addPropertyConsumer(val -> {
                weightedBlockState.tileEntityInfo = val;
                cell.setValidityState(stateForNBTCompoundJson(weightedBlockState.tileEntityInfo));
            });
            cell.setShowsValidityState(true);
            cell.setValidityState(stateForNBTCompoundJson(weightedBlockState.tileEntityInfo));
            return new TableElementCell(IvTranslations.get("reccomplex.tileentity.nbt"), cell);
        }

        return super.elementForIndexInSegment(table, index, segment);
    }
}
