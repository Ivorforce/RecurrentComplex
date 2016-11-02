/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
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
    public boolean showIdentifier;

    public TableDataSourceSelection(Selection list, int[] dimensions, TableDelegate tableDelegate, TableNavigator navigator, boolean showIdentifier)
    {
        super(list, tableDelegate, navigator);
        this.dimensions = dimensions;
        this.showIdentifier = showIdentifier;
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
        return list.size() > 0 ? list.get(list.size() - 1).copy()
                : new Selection.Area(true, new int[dimensions.length], new int[dimensions.length], showIdentifier ? "" : null);
    }

    @Override
    public TableDataSource editEntryDataSource(Selection.Area entry)
    {
        return new TableDataSourceSelectionArea(entry, dimensions, showIdentifier);
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Area List";
    }
}
