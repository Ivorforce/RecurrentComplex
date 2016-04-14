/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator.rules;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.worldscripts.mazegenerator.TableDataSourceMazePathList;
import ivorius.reccomplex.structures.generic.maze.rules.saved.MazeRuleConnect;
import ivorius.reccomplex.utils.IvTranslations;
import net.minecraft.util.EnumChatFormatting;

/**
 * Created by lukas on 21.03.16.
 */
public class TableDataSourceMazeRuleConnect extends TableDataSourceSegmented
{

    private final MazeRuleConnect rule;

    public TableDataSourceMazeRuleConnect(MazeRuleConnect rule, TableDelegate tableDelegate, TableNavigator navigator, int[] boundsLower, int[] boundsHigher)
    {
        this.rule = rule;

        TableCellTitle startTitle = new TableCellTitle("", "Start");
        startTitle.setTooltip(IvTranslations.formatLines("reccomplex.mazerule.connect.start.tooltip"));
        addManagedSection(1, new TableDataSourcePreloaded(new TableElementCell(startTitle)));
        addManagedSection(2, new TableDataSourceMazePathList(rule.start, tableDelegate, navigator, boundsLower, boundsHigher));

        TableCellTitle endTitle = new TableCellTitle("", "End");
        endTitle.setTooltip(IvTranslations.formatLines("reccomplex.mazerule.connect.end.tooltip"));
        addManagedSection(3, new TableDataSourcePreloaded(new TableElementCell(endTitle)));
        addManagedSection(4, new TableDataSourceMazePathList(rule.end, tableDelegate, navigator, boundsLower, boundsHigher));
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return segment == 0 ? 1 : super.sizeOfSegment(segment);
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            TableCellBoolean preventCell = new TableCellBoolean("prevent", rule.preventConnection, EnumChatFormatting.GOLD + "Prevent Connection", EnumChatFormatting.GREEN + "Ensure Connection");
            preventCell.addPropertyListener(cell -> rule.preventConnection = (boolean) cell.getPropertyValue());
            return new TableElementCell(preventCell);
        }

        return super.elementForIndexInSegment(table, index, segment);
    }
}
