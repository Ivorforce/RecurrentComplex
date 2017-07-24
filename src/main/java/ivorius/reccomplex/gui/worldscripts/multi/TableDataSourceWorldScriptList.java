/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.multi;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.cell.TableCellButton;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceList;
import ivorius.reccomplex.utils.RCStrings;
import ivorius.reccomplex.world.gen.script.WorldScript;
import ivorius.reccomplex.world.gen.script.WorldScriptRegistry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceWorldScriptList extends TableDataSourceList<WorldScript, List<WorldScript>>
{
    protected BlockPos realWorldPos;

    public TableDataSourceWorldScriptList(List<WorldScript> list, BlockPos realWorldPos, TableDelegate tableDelegate, TableNavigator navigator)
    {
        super(list, tableDelegate, navigator);
        this.realWorldPos = realWorldPos;
        setUsesPresetActionForAdding(true);
        duplicateTitle = TextFormatting.GREEN + "D";
    }

    @Override
    public String getDisplayString(WorldScript script)
    {
        return RCStrings.abbreviateFormatted(script.getDisplayString(), 24);
    }

    @Override
    public WorldScript newEntry(String actionID)
    {
        return tryInstantiate(actionID, WorldScriptRegistry.INSTANCE.objectClass(actionID), "Failed instantiating world script: %s");
    }

    @Nonnull
    @Override
    public TableCell entryCell(boolean enabled, WorldScript worldScript)
    {
        return TableCells.edit(enabled, navigator, tableDelegate, () -> worldScript.tableDataSource(realWorldPos, navigator, tableDelegate));
    }

    @Override
    public WorldScript copyEntry(WorldScript worldScript)
    {
        return WorldScriptRegistry.INSTANCE.copy(worldScript);
    }

    @Override
    public List<TableCellButton> getAddActions()
    {
        return TableCells.addManyWithBase(WorldScriptRegistry.INSTANCE.allIDs(), "reccomplex.worldscript.", canEditList());
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Scripts";
    }
}
