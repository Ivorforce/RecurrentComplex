/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator.rules;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.maze.rules.MazeRule;
import ivorius.reccomplex.structures.generic.maze.rules.MazeRuleRegistry;
import ivorius.reccomplex.utils.IvClasses;
import ivorius.reccomplex.utils.IvTranslations;
import net.minecraft.util.StatCollector;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by lukas on 21.03.16.
 */
public class TableDataSourceMazeRuleList extends TableDataSourceList<MazeRule<?>, List<MazeRule<?>>>
{
    private int[] boundsLower;
    private int[] boundsHigher;

    public TableDataSourceMazeRuleList(List<MazeRule<?>> list, TableDelegate tableDelegate, TableNavigator navigator, int[] boundsLower, int[] boundsHigher)
    {
        super(list, tableDelegate, navigator);
        this.boundsLower = boundsLower;
        this.boundsHigher = boundsHigher;
        setUsesPresetActionForAdding(true);
    }

    @Override
    public String getDisplayString(MazeRule mazeRule)
    {
        return StringUtils.abbreviate(mazeRule.displayString(), 24);
    }

    @Override
    public MazeRule newEntry(String actionID)
    {
        return IvClasses.instantiate(MazeRuleRegistry.INSTANCE.objectClass(actionID));
    }

    @Override
    public TableDataSource editEntryDataSource(MazeRule<?> mazeRule)
    {
        return  mazeRule.tableDataSource(navigator, tableDelegate, boundsLower, boundsHigher);
    }

    @Override
    public TableCellButton.Action[] getAddActions()
    {
        Collection<String> allTypes = MazeRuleRegistry.INSTANCE.allIDs();
        List<TableCellButton.Action> actions = new ArrayList<>(allTypes.size());
        for (String type : allTypes)
        {
            String baseKey = "reccomplex.mazerule." + type;
            actions.add(new TableCellButton.Action(type,
                    StatCollector.translateToLocal(baseKey),
                    IvTranslations.formatLines(baseKey + ".tooltip")
            ));
        }
        return actions.toArray(new TableCellButton.Action[actions.size()]);
    }
}
