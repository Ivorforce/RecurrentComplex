/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator.rules;

import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.worldscripts.mazegenerator.TableDataSourceMazePathList;
import ivorius.reccomplex.structures.generic.maze.rules.saved.MazeRuleConnect;
import ivorius.ivtoolkit.tools.IvTranslations;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

/**
 * Created by lukas on 21.03.16.
 */
public class TableDataSourceMazeRuleConnect extends TableDataSourceSegmented
{

    private final MazeRuleConnect rule;

    public TableDataSourceMazeRuleConnect(MazeRuleConnect rule, TableDelegate tableDelegate, TableNavigator navigator, List<IntegerRange> bounds)
    {
        this.rule = rule;

        TableCellTitle startTitle = new TableCellTitle("", IvTranslations.get("reccomplex.mazerule.connect.start"));
        addManagedSection(1, new TableDataSourcePreloaded(new TableElementCell(startTitle).withTitleTooltip(IvTranslations.formatLines("reccomplex.mazerule.connect.start.tooltip"))));
        addManagedSection(2, new TableDataSourceMazePathList(rule.start, tableDelegate, navigator, bounds));

        TableCellTitle endTitle = new TableCellTitle("", IvTranslations.get("reccomplex.mazerule.connect.end"));
        addManagedSection(3, new TableDataSourcePreloaded(new TableElementCell(endTitle).withTitleTooltip(IvTranslations.formatLines("reccomplex.mazerule.connect.end.tooltip"))));
        addManagedSection(4, new TableDataSourceMazePathList(rule.end, tableDelegate, navigator, bounds));
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
            TableCellBoolean preventCell = new TableCellBoolean("prevent", rule.preventConnection,
                    TextFormatting.GOLD + IvTranslations.get("reccomplex.mazerule.connect.prevent"),
                    TextFormatting.GREEN + IvTranslations.get("reccomplex.mazerule.connect.prevent"));
            preventCell.addPropertyConsumer(val -> rule.preventConnection = val);
            return new TableElementCell(preventCell);
        }

        return super.elementForIndexInSegment(table, index, segment);
    }
}
