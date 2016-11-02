/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.editstructure.preset.TableDataSourcePresettedList;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCellButton;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceList;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.gentypes.GenerationInfo;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceStructureGenerationInfoList extends TableDataSourceList<GenerationInfo, List<GenerationInfo>>
{
    public TableDataSourceStructureGenerationInfoList(List<GenerationInfo> list, TableDelegate tableDelegate, TableNavigator navigator)
    {
        super(list, tableDelegate, navigator);
        setUsesPresetActionForAdding(true);
    }

    @Override
    public List<TableCellButton> getAddActions()
    {
        return TableDataSourcePresettedList.addActions(StructureRegistry.GENERATION_INFOS.allIDs(), "reccomplex.generationInfo.", canEditList());
    }

    @Override
    public String getDisplayString(GenerationInfo generationInfo)
    {
        return generationInfo.displayString();
    }

    @Override
    public GenerationInfo newEntry(String actionID)
    {
        return tryInstantiate(actionID, StructureRegistry.GENERATION_INFOS.typeForID(actionID), "Failed instantiating generation info: %s");
    }

    @Override
    public TableDataSource editEntryDataSource(GenerationInfo entry)
    {
        return entry.tableDataSource(navigator, tableDelegate);
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Generation Infos";
    }
}
