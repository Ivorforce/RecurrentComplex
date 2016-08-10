/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.multi;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.scripts.world.WorldScript;
import ivorius.reccomplex.scripts.world.WorldScriptRegistry;
import ivorius.reccomplex.utils.IvClasses;
import ivorius.ivtoolkit.tools.IvTranslations;
import net.minecraft.util.StatCollector;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceWorldScriptList extends TableDataSourceList<WorldScript, List<WorldScript>>
{
    public TableDataSourceWorldScriptList(List<WorldScript> list, TableDelegate tableDelegate, TableNavigator navigator)
    {
        super(list, tableDelegate, navigator);
        setUsesPresetActionForAdding(true);
    }

    @Override
    public String getDisplayString(WorldScript script)
    {
        return StringUtils.abbreviate(script.getDisplayString(), 24);
    }

    @Override
    public WorldScript newEntry(String actionID)
    {
        return IvClasses.instantiate(WorldScriptRegistry.INSTANCE.objectClass(actionID));
    }

    @Override
    public TableDataSource editEntryDataSource(WorldScript entry)
    {
        return entry.tableDataSource(navigator, tableDelegate);
    }

    @Override
    public TableCellButton.Action[] getAddActions()
    {
        Collection<String> allTypes = WorldScriptRegistry.INSTANCE.allIDs();
        List<TableCellButton.Action> actions = new ArrayList<>(allTypes.size());
        for (String type : allTypes)
        {
            String baseKey = "reccomplex.worldscript." + type;
            actions.add(new TableCellButton.Action(type,
                    StatCollector.translateToLocal(baseKey),
                    IvTranslations.formatLines(baseKey + ".tooltip")
            ));
        }
        return actions.toArray(new TableCellButton.Action[actions.size()]);
    }

}
