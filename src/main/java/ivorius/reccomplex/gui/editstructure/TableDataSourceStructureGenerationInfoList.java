/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.editstructure.preset.TableDataSourcePresettedList;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.generic.gentypes.StructureGenerationInfo;

import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceStructureGenerationInfoList extends TableDataSourceList<StructureGenerationInfo, List<StructureGenerationInfo>>
{
    public TableDataSourceStructureGenerationInfoList(List<StructureGenerationInfo> list, TableDelegate tableDelegate, TableNavigator navigator)
    {
        super(list, tableDelegate, navigator);
        setUsesPresetActionForAdding(true);
        setEarlierTitle(IvTranslations.get("gui.up"));
        setLaterTitle(IvTranslations.get("gui.down"));
    }

    @Override
    public TableCellButton[] getAddActions()
    {
        return TableDataSourcePresettedList.addActions(StructureRegistry.GENERATION_INFOS.allIDs(), "reccomplex.generationInfo.", canEditList());
    }

    @Override
    public String getDisplayString(StructureGenerationInfo structureGenerationInfo)
    {
        return structureGenerationInfo.displayString();
    }

    @Override
    public StructureGenerationInfo newEntry(String actionID)
    {
        return tryInstantiate(actionID, StructureRegistry.GENERATION_INFOS.typeForID(actionID), "Failed instantiating generation info: %s");
    }

    @Override
    public TableDataSource editEntryDataSource(StructureGenerationInfo entry)
    {
        return entry.tableDataSource(navigator, tableDelegate);
    }
}
