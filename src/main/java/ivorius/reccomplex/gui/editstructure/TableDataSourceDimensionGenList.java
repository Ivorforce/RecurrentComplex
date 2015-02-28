/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.DimensionGenerationInfo;
import ivorius.reccomplex.structures.generic.gentypes.DimensionMatcherPresets;
import ivorius.reccomplex.structures.generic.gentypes.PresettedList;
import ivorius.reccomplex.utils.IvTranslations;
import net.minecraft.util.StatCollector;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceDimensionGenList extends TableDataSourcePresettedList<DimensionGenerationInfo> implements TableElementActionListener
{
    public TableDataSourceDimensionGenList(PresettedList<DimensionGenerationInfo> list, TableDelegate tableDelegate, TableNavigator navigator)
    {
        super(list, tableDelegate, navigator);
    }

    @Override
    protected String getBasePresetKey()
    {
        return "reccomplex.dimensionPreset.";
    }

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
}
