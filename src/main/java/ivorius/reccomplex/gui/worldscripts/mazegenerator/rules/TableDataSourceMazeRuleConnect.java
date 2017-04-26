/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator.rules;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.cell.TableCellBoolean;
import ivorius.reccomplex.gui.table.cell.TableCellTitle;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourcePreloaded;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.worldscripts.mazegenerator.TableDataSourceMazePathList;
import ivorius.reccomplex.world.gen.feature.structure.generic.Selection;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.rules.saved.MazeRuleConnect;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 21.03.16.
 */
public class TableDataSourceMazeRuleConnect extends TableDataSourceSegmented
{
    private final MazeRuleConnect rule;

    public TableDataSourceMazeRuleConnect(MazeRuleConnect rule, TableDelegate tableDelegate, TableNavigator navigator, Selection bounds)
    {
        this.rule = rule;

        TableCellTitle startTitle = new TableCellTitle("", IvTranslations.get("reccomplex.mazerule.connect.start"));
        addManagedSegment(1, new TableDataSourcePreloaded(new TitledCell(startTitle).withTitleTooltip(IvTranslations.formatLines("reccomplex.mazerule.connect.start.tooltip"))));
        addManagedSegment(2, new TableDataSourceMazePathList(rule.start, tableDelegate, navigator, bounds));

        TableCellTitle endTitle = new TableCellTitle("", IvTranslations.get("reccomplex.mazerule.connect.end"));
        addManagedSegment(3, new TableDataSourcePreloaded(new TitledCell(endTitle).withTitleTooltip(IvTranslations.formatLines("reccomplex.mazerule.connect.end.tooltip"))));
        addManagedSegment(4, new TableDataSourceMazePathList(rule.end, tableDelegate, navigator, bounds));
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Connect";
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return segment == 0 ? 1 : super.sizeOfSegment(segment);
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

        return super.cellForIndexInSegment(table, index, segment);
    }
}
