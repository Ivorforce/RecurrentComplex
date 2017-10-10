/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.files.RCFileSaver;
import ivorius.reccomplex.gui.editstructure.preset.TableDataSourcePresettedList;
import ivorius.reccomplex.gui.editstructure.preset.TableDataSourcePresettedObject;
import ivorius.reccomplex.gui.table.TableCells;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.utils.presets.PresettedList;
import ivorius.reccomplex.world.gen.feature.structure.generic.WeightedDimensionMatcher;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 04.06.14.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceDimensionGenList extends TableDataSourceSegmented
{
    public TableDataSourceDimensionGenList(PresettedList<WeightedDimensionMatcher> list, TableDelegate delegate, TableNavigator navigator)
    {
        addManagedSegment(0, new TableDataSourcePresettedObject<>(list, RCFileSaver.DIMENSION_PRESET, delegate, navigator)
                .withApplyPresetAction(() -> addPresetSegments(list, delegate, navigator)));

        addPresetSegments(list, delegate, navigator);
    }

    public void addPresetSegments(final PresettedList<WeightedDimensionMatcher> list, final TableDelegate delegate, final TableNavigator navigator)
    {
        addManagedSegment(1, new TableDataSourcePresettedList<WeightedDimensionMatcher>(list, delegate, navigator)
        {
            @Override
            public String getDisplayString(WeightedDimensionMatcher generationInfo)
            {
                return String.format("%s%s: %.1f", generationInfo.getDisplayString(), TextFormatting.RESET, generationInfo.getActiveGenerationWeight());
            }

            @Override
            public WeightedDimensionMatcher newEntry(String actionID)
            {
                return new WeightedDimensionMatcher("", null);
            }

            @Nonnull
            @Override
            public TableCell entryCell(boolean enabled, WeightedDimensionMatcher weightedDimensionMatcher)
            {
                return TableCells.edit(enabled, navigator, delegate, () -> new TableDataSourceDimensionGen(weightedDimensionMatcher, tableDelegate));
            }
        });
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Dimensions";
    }
}
