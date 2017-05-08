/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator.reachability;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.*;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.SavedMazePath;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.SavedMazeReachability;
import net.minecraft.util.text.TextFormatting;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by lukas on 16.03.16.
 */
public class TableDataSourceMazeReachabilityGrouping extends TableDataSourceSegmented
{
    private SavedMazeReachability reachability;

    public TableDataSourceMazeReachabilityGrouping(SavedMazeReachability reachability, Set<SavedMazePath> expected, TableDelegate tableDelegate, TableNavigator tableNavigator)
    {
        this.reachability = reachability;
        addManagedSegment(1, new TableDataSourceMazeReachabilityGroups(reachability, expected, tableDelegate, tableNavigator));
    }

    public SavedMazeReachability getReachability()
    {
        return reachability;
    }

    public void setReachability(SavedMazeReachability reachability)
    {
        this.reachability = reachability;
    }

    @Override
    public int numberOfSegments()
    {
        return 2;
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
            TableCellBoolean cell = new TableCellBoolean(null, reachability.groupByDefault, "Group", "Don't Group");
            cell.addPropertyConsumer(b -> reachability.groupByDefault = b);
            return new TitledCell(IvTranslations.get("reccomplex.reachability.groups.default.behavior"), cell)
                    .withTitleTooltip(IvTranslations.getLines("reccomplex.reachability.groups.default.behavior.tooltip"));
        }

        return super.cellForIndexInSegment(table, index, segment);
    }
}
