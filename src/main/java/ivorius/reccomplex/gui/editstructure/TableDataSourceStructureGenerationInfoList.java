/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.generic.gentypes.StructureGenerationInfo;
import ivorius.reccomplex.utils.IvTranslations;
import net.minecraft.util.StatCollector;

import java.util.ArrayList;
import java.util.Collection;
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
        setEarlierTitle("Up");
        setLaterTitle("Down");
    }

    @Override
    public TableElementButton.Action[] getAddActions()
    {
        Collection<String> allTypes = StructureRegistry.getStructureGenerationInfoRegistry().allIDs();
        List<TableElementButton.Action> actions = new ArrayList<>(allTypes.size());
        for (String type : allTypes)
        {
            String baseKey = "reccomplex.generationInfo." + type;
            actions.add(new TableElementButton.Action(type,
                    StatCollector.translateToLocal(baseKey),
                    IvTranslations.formatLines(baseKey + ".tooltip")
            ));
        }
        return actions.toArray(new TableElementButton.Action[actions.size()]);
    }

    @Override
    public String getDisplayString(StructureGenerationInfo structureGenerationInfo)
    {
        return structureGenerationInfo.displayString();
    }

    @Override
    public StructureGenerationInfo newEntry(String actionID)
    {
        Class<? extends StructureGenerationInfo> clazz = StructureRegistry.getStructureGenerationInfoRegistry().typeForID(actionID);

        return instantiateStructureGenerationInfo(clazz);
    }

    @Override
    public TableDataSource editEntryDataSource(StructureGenerationInfo entry)
    {
        return entry.tableDataSource(navigator, tableDelegate);
    }

    public StructureGenerationInfo instantiateStructureGenerationInfo(Class<? extends StructureGenerationInfo> clazz)
    {
        StructureGenerationInfo generationInfo = null;

        try
        {
            generationInfo = clazz.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            RecurrentComplex.logger.error(e);
        }

        return generationInfo;
    }
}
