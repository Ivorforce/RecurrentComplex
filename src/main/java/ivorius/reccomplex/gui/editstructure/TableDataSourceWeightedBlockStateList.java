/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.files.RCFileSaver;
import ivorius.reccomplex.gui.editstructure.preset.TableDataSourcePresettedList;
import ivorius.reccomplex.gui.editstructure.preset.TableDataSourcePresettedObject;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.world.gen.feature.structure.generic.WeightedBlockState;
import ivorius.reccomplex.utils.presets.PresettedList;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceWeightedBlockStateList extends TableDataSourceSegmented
{
    public TableDataSourceWeightedBlockStateList(PresettedList<WeightedBlockState> list, TableDelegate delegate, TableNavigator navigator)
    {
        addManagedSegment(0, new TableDataSourcePresettedObject<>(list, RCFileSaver.BLOCK_PRESET, delegate, navigator)
                .withApplyPresetAction(() -> addPresetSegments(list, delegate, navigator)));

        addPresetSegments(list, delegate, navigator);
    }

    public void addPresetSegments(final PresettedList<WeightedBlockState> list, final TableDelegate delegate, final TableNavigator navigator)
    {
        addManagedSegment(1, new TableDataSourcePresettedList<WeightedBlockState>(list, delegate, navigator)
        {
            @Override
            public String getDisplayString(WeightedBlockState entry)
            {
                return String.format("%s$%d (%.2f)", StringUtils.abbreviate(Block.REGISTRY.getNameForObject(entry.state.getBlock()).toString(), 16), ivorius.ivtoolkit.blocks.BlockStates.toMetadata(entry.state), entry.getWeight());
            }

            @Override
            public WeightedBlockState newEntry(String actionID)
            {
                return new WeightedBlockState(null, Blocks.STONE.getDefaultState(), null);
            }

            @Nonnull
            @Override
            public TableCell entryCell(boolean enabled, WeightedBlockState weightedBlockState)
            {
                return editCell(enabled, navigator, delegate, () -> new TableDataSourceWeightedBlockState(weightedBlockState, navigator, tableDelegate));
            }
        });
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Blocks";
    }
}
