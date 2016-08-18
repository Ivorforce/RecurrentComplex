/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.Selection;
import com.mojang.realmsclient.gui.ChatFormatting;

import java.util.Arrays;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceSelection extends TableDataSourceList<Selection.Area, Selection>
{
    private int[] dimensions;

    public TableDataSourceSelection(Selection list, int[] dimensions, TableDelegate tableDelegate, TableNavigator navigator)
    {
        super(list, tableDelegate, navigator);
        this.dimensions = dimensions;
    }

    @Override
    public String getDisplayString(Selection.Area area)
    {
        ChatFormatting color = area.isAdditive() ? ChatFormatting.GREEN : ChatFormatting.RED;
        return String.format("%s%s%s - %s%s", color, Arrays.toString(area.getMinCoord()), ChatFormatting.RESET, color, Arrays.toString(area.getMaxCoord()));
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
