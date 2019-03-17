/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.reccomplex.gui.table.TableCells;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceList;
import ivorius.reccomplex.world.gen.feature.structure.generic.Selection;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.SavedMazePath;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceMazePathList extends TableDataSourceList<SavedMazePath, List<SavedMazePath>>
{
    private Selection bounds;

    public TableDataSourceMazePathList(List<SavedMazePath> list, TableDelegate tableDelegate, TableNavigator navigator, Selection bounds)
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
        return new SavedMazePath(2, new MazeRoom(new int[bounds.dimensions]), false);
    }

    @Nonnull
    @Override
    public TableCell entryCell(boolean enabled, SavedMazePath savedMazePath)
    {
        return TableCells.edit(enabled, navigator, tableDelegate, () -> new TableDataSourceMazePath(savedMazePath, bounds, tableDelegate, navigator));
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Paths";
    }
}
