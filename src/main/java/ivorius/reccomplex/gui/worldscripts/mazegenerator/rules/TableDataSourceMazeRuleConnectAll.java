/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator.rules;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.worldscripts.mazegenerator.TableDataSourceMazePathList;
import ivorius.reccomplex.structures.generic.maze.rules.saved.MazeRuleConnect;
import ivorius.reccomplex.structures.generic.maze.rules.saved.MazeRuleConnectAll;
import ivorius.reccomplex.utils.IvTranslations;
import net.minecraft.util.EnumChatFormatting;

/**
 * Created by lukas on 21.03.16.
 */
public class TableDataSourceMazeRuleConnectAll extends TableDataSourceSegmented implements TableCellPropertyListener
{
    private final MazeRuleConnectAll rule;

    public TableDataSourceMazeRuleConnectAll(MazeRuleConnectAll rule, TableDelegate tableDelegate, TableNavigator navigator, int[] boundsLower, int[] boundsHigher)
    {
        this.rule = rule;
        addManagedSection(0, new TableDataSourcePreloaded(new TableElementCell(new TableCellTitle("", "Paths"))));
        addManagedSection(2, new TableDataSourceMazePathList(rule.exits, tableDelegate, navigator, boundsLower, boundsHigher));
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return segment == 1 ? 1 : super.sizeOfSegment(segment);
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 1)
        {
            TableCellBoolean cell = new TableCellBoolean("additive", rule.additive, EnumChatFormatting.GREEN + "Additive", EnumChatFormatting.GOLD + "Subtractive");
            cell.addPropertyListener(this);
            return new TableElementCell(cell);
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    @Override
    public void valueChanged(TableCellPropertyDefault cell)
    {
        if ("additive".equals(cell.getID()))
            rule.additive = (Boolean) cell.getPropertyValue();
    }
}
