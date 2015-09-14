/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.multi;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.scripts.world.WorldScript;
import ivorius.reccomplex.scripts.world.WorldScriptRegistry;
import ivorius.reccomplex.utils.IvTranslations;
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
        Class<? extends WorldScript> clazz = WorldScriptRegistry.INSTANCE.getScript(actionID);

        return instantiateScript(clazz);
    }

    @Override
    public TableDataSource editEntryDataSource(WorldScript entry)
    {
        return entry.tableDataSource(navigator, tableDelegate);
    }

    @Override
    public TableCellButton.Action[] getAddActions()
    {
        Collection<String> allTypes = WorldScriptRegistry.INSTANCE.keySet();
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

    public WorldScript instantiateScript(Class<? extends WorldScript> clazz)
    {
        WorldScript script = null;

        try
        {
            script = clazz.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            RecurrentComplex.logger.error(e);
        }

        return script;
    }
}
