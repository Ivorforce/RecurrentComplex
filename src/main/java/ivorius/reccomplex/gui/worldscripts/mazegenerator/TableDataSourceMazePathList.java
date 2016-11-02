/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import net.minecraft.util.text.TextFormatting;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceList;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.SavedMazePath;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceMazePathList extends TableDataSourceList<SavedMazePath, List<SavedMazePath>>
{
    private List<IntegerRange> bounds;

    public TableDataSourceMazePathList(List<SavedMazePath> list, TableDelegate tableDelegate, TableNavigator navigator, List<IntegerRange> bounds)
    {
        super(list, tableDelegate, navigator);
        this.bounds = bounds;
    }

    @Override
    public String getDisplayString(SavedMazePath mazePath)
    {
        return String.format("%s %s%s%s", Arrays.toString(mazePath.sourceRoom.getCoordinates()),
                TextFormatting.BLUE, TableDataSourceMazePath.directionFromPath(mazePath).toString(), TextFormatting.RESET);
    }

    @Override
    public SavedMazePath newEntry(String actionID)
    {
        return new SavedMazePath(2, new MazeRoom(new int[bounds.size()]), false);
    }

    @Override
    public TableDataSource editEntryDataSource(SavedMazePath entry)
    {
        return new TableDataSourceMazePath(entry, bounds, tableDelegate);
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Paths";
    }
}
