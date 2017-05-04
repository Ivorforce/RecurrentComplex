/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.ivtoolkit.blocks.BlockStates;
import ivorius.reccomplex.files.RCFileSaver;
import ivorius.reccomplex.gui.editstructure.preset.TableDataSourcePresettedList;
import ivorius.reccomplex.gui.editstructure.preset.TableDataSourcePresettedObject;
import ivorius.reccomplex.gui.table.TableCells;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.utils.RCStrings;
import ivorius.reccomplex.world.gen.feature.structure.generic.WeightedBlockState;
import ivorius.reccomplex.utils.presets.PresettedList;
import net.minecraft.init.Blocks;
import net.minecraft.util.text.TextFormatting;

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
        TableDataSourcePresettedList<WeightedBlockState> listSource = new TableDataSourcePresettedList<WeightedBlockState>(list, delegate, navigator)
        {
            @Override
            public String getDisplayString(WeightedBlockState entry)
            {
                if (entry.state == null)
                    return String.format("None (%.2f)", entry.getWeight());
                return String.format("%s$%d (%.2f)", RCStrings.abbreviateFormatted(entry.state.getBlock().getLocalizedName(), 16), BlockStates.toMetadata(entry.state), entry.getWeight());
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
                return TableCells.edit(enabled, navigator, delegate, () -> new TableDataSourceWeightedBlockState(weightedBlockState, navigator, tableDelegate));
            }

            @Override
            public WeightedBlockState copyEntry(WeightedBlockState weightedBlockState)
            {
                return weightedBlockState.copy();
            }
        };
        listSource.setDuplicateTitle(TextFormatting.GREEN + "D");
        addManagedSegment(1, listSource);
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Blocks";
    }
}
