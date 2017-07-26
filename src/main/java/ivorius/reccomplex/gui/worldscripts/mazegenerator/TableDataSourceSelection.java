/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import ivorius.reccomplex.client.rendering.MazeVisualizationContext;
import ivorius.reccomplex.client.rendering.SelectionQuadCache;
import ivorius.reccomplex.gui.GuiHider;
import ivorius.reccomplex.gui.table.TableCells;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceList;
import ivorius.reccomplex.world.gen.feature.structure.generic.Selection;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceSelection extends TableDataSourceList<Selection.Area, Selection>
{
    private int[] dimensions;
    protected boolean showIdentifier;

    protected MazeVisualizationContext visualizationContext;

    public TableDataSourceSelection(Selection list, int[] dimensions, TableDelegate tableDelegate, TableNavigator navigator, boolean showIdentifier)
    {
        super(list, tableDelegate, navigator);
        this.dimensions = dimensions;
        this.showIdentifier = showIdentifier;
        duplicateTitle = TextFormatting.GREEN + "D";
    }

    public TableDataSourceSelection visualizing(MazeVisualizationContext visualizationContext)
    {
        this.visualizationContext = visualizationContext;
        return this;
    }

    @Override
    public String getDisplayString(Selection.Area area)
    {
        TextFormatting color = area.isAdditive() ? TextFormatting.GREEN : TextFormatting.RED;
        return String.format("%s%s%s - %s%s", color, Arrays.toString(area.getMinCoord()), TextFormatting.RESET, color, Arrays.toString(area.getMaxCoord()));
    }

    @Override
    public Selection.Area newEntry(String actionID)
    {
        return new Selection.Area(true, new int[dimensions.length], new int[dimensions.length], showIdentifier ? "" : null);
    }

    @Override
    public Selection.Area copyEntry(Selection.Area area)
    {
        return area.copy();
    }

    @Nonnull
    @Override
    public TableCell entryCell(boolean enabled, Selection.Area area)
    {
        return TableCells.edit(enabled, navigator, tableDelegate, () -> new TableDataSourceSelectionArea(area, dimensions, showIdentifier)
                .visualizing(visualizationContext));
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Area List";
    }

    @Override
    public boolean canVisualize()
    {
        return visualizationContext != null;
    }

    @Override
    public GuiHider.Visualizer visualizer()
    {
        return new SelectionQuadCache.Visualizer(list, visualizationContext);
    }
}
