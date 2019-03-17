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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 21.03.16.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceMazeRuleConnect extends TableDataSourceSegmented
{
    private final MazeRuleConnect rule;

    public TableDataSourceMazeRuleConnect(MazeRuleConnect rule, TableDelegate tableDelegate, TableNavigator navigator, Selection bounds)
    {
        this.rule = rule;

        TableCellTitle startTitle = new TableCellTitle("", IvTranslations.get("reccomplex.mazerule.connect.start"));
        addSegment(1, new TableDataSourcePreloaded(new TitledCell(startTitle).withTooltip(IvTranslations.formatLines("reccomplex.mazerule.connect.start.tooltip"))));
        addSegment(2, new TableDataSourceMazePathList(rule.start, tableDelegate, navigator, bounds));

        TableCellTitle endTitle = new TableCellTitle("", IvTranslations.get("reccomplex.mazerule.connect.end"));
        addSegment(3, new TableDataSourcePreloaded(new TitledCell(endTitle).withTooltip(IvTranslations.formatLines("reccomplex.mazerule.connect.end.tooltip"))));
        addSegment(4, new TableDataSourceMazePathList(rule.end, tableDelegate, navigator, bounds));
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
                    TextFormatting.GREEN + IvTranslations.get("reccomplex.mazerule.connect.ensure"));
            preventCell.addListener(val -> rule.preventConnection = val);
            return new TitledCell(preventCell);
        }

        return super.cellForIndexInSegment(table, index, segment);
    }
}
