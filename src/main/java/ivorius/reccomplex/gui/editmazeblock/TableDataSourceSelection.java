/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editmazeblock;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.Selection;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceSelection extends TableDataSourceList<Selection.Area, Selection> implements TableElementButton.Listener
{
    private int[] dimensions;

    public TableDataSourceSelection(Selection list, int[] dimensions, TableDelegate tableDelegate, TableNavigator navigator)
    {
        super(list, tableDelegate, navigator);
        this.dimensions = dimensions;
        setAddTitle("Add Area");
    }

    @Override
    public String getDisplayString(Selection.Area area)
    {
        EnumChatFormatting color = area.isAdditive() ? EnumChatFormatting.GREEN : EnumChatFormatting.RED;
        return String.format(color + "%s - %s", Arrays.toString(area.getMinCoord()), Arrays.toString(area.getMaxCoord()));
    }

    @Override
    public Selection.Area newEntry(String actionID)
    {
        return new Selection.Area(true, new int[dimensions.length], new int[dimensions.length]);
    }

    @Override
    public TableDataSource editEntryDataSource(Selection.Area entry)
    {
        return new TableDataSourceSelectionArea(entry, dimensions);
    }

}
