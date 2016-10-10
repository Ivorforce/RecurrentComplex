/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.files.RCFileSaver;
import ivorius.reccomplex.gui.editstructure.preset.TableDataSourcePresettedList;
import ivorius.reccomplex.gui.editstructure.preset.TableDataSourcePresettedObject;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.world.gen.feature.structure.generic.DimensionGenerationInfo;
import ivorius.reccomplex.utils.presets.PresettedList;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.StringUtils;

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

            @Override
            public TableDataSource editEntryDataSource(DimensionGenerationInfo entry)
            {
                return new TableDataSourceDimensionGen(entry, tableDelegate);
            }
        });
    }
}
