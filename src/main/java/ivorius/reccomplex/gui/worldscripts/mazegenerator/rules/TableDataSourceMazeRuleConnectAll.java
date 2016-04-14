/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator.rules;

import com.google.common.collect.Lists;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.worldscripts.mazegenerator.TableDataSourceMazePathList;
import ivorius.reccomplex.gui.worldscripts.mazegenerator.reachability.TableDataSourceMazeReachability;
import ivorius.reccomplex.structures.generic.maze.*;
import ivorius.reccomplex.structures.generic.maze.rules.saved.MazeRuleConnect;
import ivorius.reccomplex.structures.generic.maze.rules.saved.MazeRuleConnectAll;
import ivorius.reccomplex.utils.IvTranslations;
import net.minecraft.util.EnumChatFormatting;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by lukas on 21.03.16.
 */
public class TableDataSourceMazeRuleConnectAll extends TableDataSourceSegmented implements TableCellPropertyListener
{
    private final MazeRuleConnectAll rule;
    private List<SavedMazePathConnection> expected;

    private TableDelegate tableDelegate;

    public TableDataSourceMazeRuleConnectAll(MazeRuleConnectAll rule, TableDelegate tableDelegate, TableNavigator navigator, List<SavedMazePathConnection> expected, int[] boundsLower, int[] boundsHigher)
    {
        this.rule = rule;
        this.expected = expected;
        this.tableDelegate = tableDelegate;

        addManagedSection(1, new TableDataSourcePreloaded(new TableElementCell(new TableCellTitle("", "Paths"))));
        addManagedSection(3, new TableDataSourceMazePathList(rule.exits, tableDelegate, navigator, boundsLower, boundsHigher));
    }

    @Override
    public int numberOfSegments()
    {
        return rule.additive ? 4 : 6;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 0:
            case 2:
                return 1;
            case 4:
                return 1;
            case 5:
                return expected.size() - rule.exits.size();
            default:
                return super.sizeOfSegment(segment);
        }
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
        else if (segment == 2)
        {
            TableCellBoolean cell = new TableCellBoolean("additive", rule.additive, EnumChatFormatting.GREEN + "Additive", EnumChatFormatting.GOLD + "Subtractive");
            cell.addPropertyListener(this);
            return new TableElementCell(cell);
        }
        else if (segment == 4)
        {
            return new TableElementCell(new TableCellTitle("", "Preview"));
        }
        else if (segment == 5)
        {
            ConnectorFactory factory = new ConnectorFactory();
            Set<Connector> blockedConnections = Collections.singleton(factory.get("Wall"));
            List<SavedMazePath> exitPaths = MazeRuleConnectAll.getPaths(rule.exits, expected, blockedConnections, factory).collect(Collectors.toList());

            return new TableElementCell(new TableCellTitle("", exitPaths.get(index).toString()));
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    @Override
    public void valueChanged(TableCellPropertyDefault cell)
    {
        if ("additive".equals(cell.getID()))
        {
            rule.additive = (Boolean) cell.getPropertyValue();
            tableDelegate.reloadData();
        }
    }
}
