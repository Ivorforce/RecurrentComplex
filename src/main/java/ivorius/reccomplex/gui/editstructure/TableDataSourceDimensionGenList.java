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
import ivorius.reccomplex.structures.generic.DimensionGenerationInfo;
import ivorius.reccomplex.utils.presets.PresettedList;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceDimensionGenList extends TableDataSourceSegmented
{
    public TableDataSourceDimensionGenList(PresettedList<DimensionGenerationInfo> list, TableDelegate delegate, TableNavigator navigator)
    {
        addManagedSection(0, new TableDataSourcePresettedObject<ArrayList<DimensionGenerationInfo>>(list, delegate, navigator)
        {
            @Override
            public String getBasePresetKey()
            {
                return "reccomplex.dimensionPreset.";
            }
        });

        addManagedSection(1, new TableDataSourcePresettedList<DimensionGenerationInfo>(list, delegate, navigator)
        {
            @Override
            public String getDisplayString(DimensionGenerationInfo generationInfo)
            {
                return String.format("%s (%.2f)", StringUtils.abbreviate(generationInfo.getDisplayString(), 16), generationInfo.getActiveGenerationWeight());
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
