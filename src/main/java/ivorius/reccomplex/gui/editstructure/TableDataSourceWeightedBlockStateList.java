/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.gui.editstructure.preset.TableDataSourcePresettedList;
import ivorius.reccomplex.gui.editstructure.preset.TableDataSourcePresettedObject;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDataSourceSegmented;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.structures.generic.WeightedBlockState;
import ivorius.reccomplex.utils.BlockStates;
import ivorius.reccomplex.utils.presets.PresettedList;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceWeightedBlockStateList extends TableDataSourceSegmented
{
    public TableDataSourceWeightedBlockStateList(PresettedList<WeightedBlockState> list, TableDelegate delegate, TableNavigator navigator)
    {
        addManagedSection(0, new TableDataSourcePresettedObject<ArrayList<WeightedBlockState>>(list, delegate, navigator)
        {
            @Override
            public String getBasePresetKey()
            {
                return "reccomplex.weightedBlockStatePreset.";
            }
        });

        addManagedSection(1, new TableDataSourcePresettedList<WeightedBlockState>(list, delegate, navigator)
        {
            @Override
            public String getDisplayString(WeightedBlockState entry)
            {
                return String.format("%s$%d (%.2f)", StringUtils.abbreviate(Block.REGISTRY.getNameForObject(entry.state.getBlock()).toString(), 16), BlockStates.toMetadata(entry.state), entry.getWeight());
            }

            @Override
            public WeightedBlockState newEntry(String actionID)
            {
                return new WeightedBlockState(null, Blocks.STONE.getDefaultState(), "");
            }

            @Override
            public TableDataSource editEntryDataSource(WeightedBlockState entry)
            {
                return new TableDataSourceWeightedBlockState(entry, navigator, tableDelegate);
            }
        });
    }

}
