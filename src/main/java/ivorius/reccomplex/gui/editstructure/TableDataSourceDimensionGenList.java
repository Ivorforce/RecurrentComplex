/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.files.RCFileSaver;
import ivorius.reccomplex.gui.editstructure.preset.TableDataSourcePresettedList;
import ivorius.reccomplex.gui.editstructure.preset.TableDataSourcePresettedObject;
import ivorius.reccomplex.gui.table.TableCells;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.world.gen.feature.structure.generic.DimensionGenerationInfo;
import ivorius.reccomplex.utils.presets.PresettedList;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceDimensionGenList extends TableDataSourceSegmented
{
    public TableDataSourceDimensionGenList(PresettedList<DimensionGenerationInfo> list, TableDelegate delegate, TableNavigator navigator)
    {
        addManagedSegment(0, new TableDataSourcePresettedObject<>(list, RCFileSaver.DIMENSION_PRESET, delegate, navigator)
            .withApplyPresetAction(() -> addPresetSegments(list, delegate, navigator)));

        addPresetSegments(list, delegate, navigator);
    }

    public void addPresetSegments(final PresettedList<DimensionGenerationInfo> list, final TableDelegate delegate, final TableNavigator navigator)
    {
        addManagedSegment(1, new TableDataSourcePresettedList<DimensionGenerationInfo>(list, delegate, navigator)
        {
            @Override
            public String getDisplayString(DimensionGenerationInfo generationInfo)
            {
                return String.format("%s%s: %.1f", StringUtils.abbreviate(generationInfo.getDisplayString(), 20), TextFormatting.RESET, generationInfo.getActiveGenerationWeight());
            }

            @Override
            public DimensionGenerationInfo newEntry(String actionID)
            {
                return new DimensionGenerationInfo("", null);
            }

            @Nonnull
            @Override
            public TableCell entryCell(boolean enabled, DimensionGenerationInfo dimensionGenerationInfo)
            {
                return TableCells.edit(enabled, navigator, delegate, () -> new TableDataSourceDimensionGen(dimensionGenerationInfo, tableDelegate));
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
