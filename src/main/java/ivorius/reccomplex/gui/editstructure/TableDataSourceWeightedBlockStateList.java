/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.structures.generic.WeightedBlockState;
import ivorius.reccomplex.utils.BlockStates;
import ivorius.reccomplex.utils.PresettedList;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceWeightedBlockStateList extends TableDataSourcePresettedList<WeightedBlockState>
{
    public TableDataSourceWeightedBlockStateList(PresettedList<WeightedBlockState> list, TableDelegate tableDelegate, TableNavigator navigator)
    {
        super(list, tableDelegate, navigator);
    }

    @Override
    protected String getBasePresetKey()
    {
        return "reccomplex.weightedBlockStatePreset.";
    }

    @Override
    public String getDisplayString(WeightedBlockState entry)
    {
        return String.format("%s$%d (%.2f)", StringUtils.abbreviate(Block.blockRegistry.getNameForObject(entry.state.getBlock()), 16), BlockStates.getMetadata(entry.state), entry.getWeight());
    }

    @Override
    public WeightedBlockState newEntry(String actionID)
    {
        return new WeightedBlockState(null, BlockStates.defaultState(Blocks.stone), "");
    }

    @Override
    public TableDataSource editEntryDataSource(WeightedBlockState entry)
    {
        return new TableDataSourceWeightedBlockState(entry, tableDelegate);
    }
}
