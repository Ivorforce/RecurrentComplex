/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.client.rendering.MazeVisualizationContext;
import ivorius.reccomplex.client.rendering.SelectionQuadCache;
import ivorius.reccomplex.gui.GuiHider;
import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.gui.table.cell.*;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.structure.generic.Selection;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 08.10.14.
 */
public class TableDataSourceSelectionArea extends TableDataSourceSegmented
{
    public static final String[] COORD_NAMES = {"x", "y", "z"};

    private Selection.Area area;

    private int[] dimensions;
    private boolean showIdentifier;

    protected MazeVisualizationContext visualizationContext;

    public TableDataSourceSelectionArea(Selection.Area area, int[] dimensions, boolean showIdentifier)
    {
        this.area = area;
        this.dimensions = dimensions;
        this.showIdentifier = showIdentifier;
    }

    public TableDataSourceSelectionArea visualizing(MazeVisualizationContext visualizationContext)
    {
        this.visualizationContext = visualizationContext;
        return this;
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Area";
    }

    @Override
    public int numberOfSegments()
    {
        return showIdentifier ? 3 : 2;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 0:
                return 1;
            case 1:
                return COORD_NAMES.length;
            case 2:
                return 1;
            default:
                return super.sizeOfSegment(segment);
        }
    }

    @Override
    public TableCell cellForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            TableCellBoolean cell = new TableCellBoolean("additive", area.isAdditive(),
                    TextFormatting.GREEN + IvTranslations.get("reccomplex.selection.area.additive"),
                    TextFormatting.GOLD + IvTranslations.get("reccomplex.selection.area.subtractive"));
            cell.addListener(area::setAdditive);
            return new TitledCell(cell);
        }
        else if (segment == 1)
        {
            String title = IvTranslations.get("reccomplex.selection.area.range." + COORD_NAMES[index]);
            IntegerRange intRange = new IntegerRange(area.getMinCoord()[index], area.getMaxCoord()[index]);
            TableCellIntegerRange cell = new TableCellIntegerRange("area" + index, intRange, 0, dimensions[index] - 1);
            cell.addListener(val -> area.setCoord(index, val.getMin(), val.getMax()));
            return new TitledCell(title, cell).withTitleTooltip(IvTranslations.getLines("reccomplex.selection.area.range." + COORD_NAMES[index] + ".tooltip"));
        }
        else if (segment == 2)
        {
            TableCellString cell = new TableCellString("", area.getIdentifier() != null ? area.getIdentifier() : "");
            cell.addListener(area::setIdentifier);
            return new TitledCell(IvTranslations.get("reccomplex.selection.area.identifier"), cell);
        }

        return super.cellForIndexInSegment(table, index, segment);
    }

    @Override
    public boolean canVisualize()
    {
        return visualizationContext != null;
    }

    @Override
    public GuiHider.Visualizer visualizer()
    {
        Selection selection = new Selection(dimensions.length);
        selection.add(area);
        return new SelectionQuadCache.Visualizer(selection, visualizationContext);
    }
}
