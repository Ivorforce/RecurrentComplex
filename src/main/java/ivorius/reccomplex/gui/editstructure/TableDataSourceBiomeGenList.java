/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.gui.editstructure.preset.TableDataSourcePresettedList;
import ivorius.reccomplex.gui.editstructure.preset.TableDataSourcePresettedObject;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.BiomeGenerationInfo;
import ivorius.reccomplex.utils.presets.PresettedList;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceBiomeGenList extends TableDataSourceSegmented
{
    public TableDataSourceBiomeGenList(PresettedList<BiomeGenerationInfo> list, TableDelegate tableDelegate, TableNavigator navigator)
    {
        addManagedSection(0, new TableDataSourcePresettedObject<ArrayList<BiomeGenerationInfo>>(list, tableDelegate, navigator)
        {
            @Override
            public String getBasePresetKey()
            {
                return "reccomplex.biomePreset.";
            }
        });

        addManagedSection(1, new TableDataSourcePresettedList<BiomeGenerationInfo>(list, tableDelegate, navigator)
        {
            @Override
            public String getDisplayString(BiomeGenerationInfo biomeGenerationInfo)
            {
                return String.format("%s (%.2f)", StringUtils.abbreviate(biomeGenerationInfo.getDisplayString(), 16), biomeGenerationInfo.getActiveGenerationWeight());
            }

            @Override
            public BiomeGenerationInfo newEntry(String actionID)
            {
                return new BiomeGenerationInfo("", null);
            }

            @Override
            public TableDataSource editEntryDataSource(BiomeGenerationInfo entry)
            {
                return new TableDataSourceBiomeGen(entry, tableDelegate);
            }
        });
    }
}
