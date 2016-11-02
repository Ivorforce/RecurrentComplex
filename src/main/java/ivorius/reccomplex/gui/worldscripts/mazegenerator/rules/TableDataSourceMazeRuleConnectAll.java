/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator.rules;

import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.cell.TableCellBoolean;
import ivorius.reccomplex.gui.table.cell.TableCellTitle;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourcePreloaded;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.worldscripts.mazegenerator.TableDataSourceMazePathList;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.*;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.rules.saved.MazeRuleConnectAll;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by lukas on 21.03.16.
 */
public class TableDataSourceMazeRuleConnectAll extends TableDataSourceSegmented
{
    private final MazeRuleConnectAll rule;
    private List<SavedMazePathConnection> expected;

    private TableDelegate tableDelegate;

    public TableDataSourceMazeRuleConnectAll(MazeRuleConnectAll rule, TableDelegate tableDelegate, TableNavigator navigator, List<SavedMazePathConnection> expected, List<IntegerRange> bounds)
    {
        this.rule = rule;
        this.expected = expected;
        this.tableDelegate = tableDelegate;

        addManagedSegment(1, new TableDataSourcePreloaded(new TitledCell(new TableCellTitle("", "Paths"))));
        addManagedSegment(3, new TableDataSourceMazePathList(rule.exits, tableDelegate, navigator, bounds));
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Connect All";
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
    public TableCell cellForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            TableCellBoolean preventCell = new TableCellBoolean("prevent", rule.preventConnection,
                    TextFormatting.GOLD + IvTranslations.get("reccomplex.mazerule.connect.prevent"),
                    TextFormatting.GREEN + IvTranslations.get("reccomplex.mazerule.connect.prevent"));
            preventCell.addPropertyConsumer(val -> rule.preventConnection = val);
            return new TitledCell(preventCell);
        }
        else if (segment == 2)
        {
            TableCellBoolean cell = new TableCellBoolean("additive", rule.additive,
                    TextFormatting.GREEN + IvTranslations.get("reccomplex.mazerule.connectall.additive"),
                    TextFormatting.GOLD + IvTranslations.get("reccomplex.mazerule.connectall.subtractive"));
            cell.addPropertyConsumer(val -> {
                rule.additive = val;
                tableDelegate.reloadData();
            });
            return new TitledCell(cell);
        }
        else if (segment == 4)
        {
            return new TitledCell(new TableCellTitle("", IvTranslations.get("reccomplex.mazerule.connectall.preview")));
        }
        else if (segment == 5)
        {
            ConnectorFactory factory = new ConnectorFactory();
            Set<Connector> blockedConnections = Collections.singleton(factory.get(ConnectorStrategy.DEFAULT_WALL));
            List<SavedMazePath> exitPaths = MazeRuleConnectAll.getPaths(rule.exits, expected, blockedConnections, factory).collect(Collectors.toList());

            return new TitledCell(new TableCellTitle("", exitPaths.get(index).toString()));
        }

        return super.cellForIndexInSegment(table, index, segment);
    }
}
