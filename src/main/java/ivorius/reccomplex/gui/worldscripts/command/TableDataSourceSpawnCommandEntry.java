/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.command;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.RCGuiTables;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCellButton;
import ivorius.reccomplex.gui.table.cell.TableCellPresetAction;
import ivorius.reccomplex.gui.table.cell.TableCellString;
import ivorius.reccomplex.gui.table.cell.TableElementCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.script.WorldScriptCommand;
import net.minecraft.init.Blocks;

import java.util.Arrays;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceSpawnCommandEntry extends TableDataSourceSegmented
{
    private WorldScriptCommand.Entry entry;

    private TableDelegate tableDelegate;

    public TableDataSourceSpawnCommandEntry(WorldScriptCommand.Entry entry, TableDelegate tableDelegate)
    {
        this.entry = entry;
        this.tableDelegate = tableDelegate;
    }

    @Override
    public int numberOfSegments()
    {
        return 1;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return 3;
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (index == 0)
        {
            TableCellPresetAction cell = new TableCellPresetAction("default", Arrays.asList(
                    new TableCellButton("", "spawner", Blocks.MOB_SPAWNER.getLocalizedName()),
                    new TableCellButton("", "entity", IvTranslations.get("reccomplex.spawncommand.preset.entity"))
            ));
            cell.addAction(action -> {
                if ("spawner".equals(action))
                    entry.command = "/setblock ~ ~ ~ mob_spawner 0 replace {SpawnData:{id:Zombie}}";
                else if ("entity".equals(action))
                    entry.command = "/summon Zombie ~ ~ ~";

                tableDelegate.reloadData();
            });
            return new TableElementCell(IvTranslations.get("reccomplex.preset"), cell);
        }
        else if (index == 1)
        {
            TableCellString cell = new TableCellString("command", entry.command);
            cell.setMaxStringLength(32767); // Same as GuiCommandBlock.
            cell.addPropertyConsumer(val -> entry.command = val);
            return new TableElementCell(IvTranslations.get("reccomplex.gui.command"), cell);
        }
        else if (index == 2)
        {
            return RCGuiTables.defaultWeightElement(val -> entry.weight = TableElements.toDouble(val), entry.weight);
        }

        return null;
    }
}
