/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator.reachability;

import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.client.rendering.MazeVisualizationContext;
import ivorius.reccomplex.gui.GuiHider;
import ivorius.reccomplex.gui.table.TableCells;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.cell.TableCellTitle;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceList;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceMulti;
import ivorius.reccomplex.gui.table.datasource.TableDataSourcePreloaded;
import ivorius.reccomplex.gui.worldscripts.mazegenerator.TableDataSourceMazePath;
import ivorius.reccomplex.world.gen.feature.structure.generic.Selection;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.SavedMazePath;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by lukas on 16.03.16.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceMazePathPairList extends TableDataSourceList<ImmutablePair<SavedMazePath, SavedMazePath>, List<ImmutablePair<SavedMazePath, SavedMazePath>>>
{
    private Selection bounds;

    protected MazeVisualizationContext visualizationContext;

    public TableDataSourceMazePathPairList(List<ImmutablePair<SavedMazePath, SavedMazePath>> list, TableDelegate tableDelegate, TableNavigator navigator, Selection bounds)
    {
        super(list, tableDelegate, navigator);
        this.bounds = bounds;
    }

    public TableDataSourceMazePathPairList visualizing(MazeVisualizationContext context)
    {
        this.visualizationContext = context;
        return this;
    }

    public TableDelegate getTableDelegate()
    {
        return tableDelegate;
    }

    public void setTableDelegate(TableDelegate tableDelegate)
    {
        this.tableDelegate = tableDelegate;
    }

    @Override
    public String getDisplayString(ImmutablePair<SavedMazePath, SavedMazePath> pair)
    {
        return pair.toString();
    }

    @Override
    public ImmutablePair<SavedMazePath, SavedMazePath> newEntry(String actionID)
    {
        return ImmutablePair.of(new SavedMazePath(0, new MazeRoom(0, 0, 0), true), new SavedMazePath(0, new MazeRoom(0, 0, 0), false));
    }

    @Nonnull
    @Override
    public TableCell entryCell(boolean enabled, ImmutablePair<SavedMazePath, SavedMazePath> pair)
    {
        return TableCells.edit(enabled, navigator, tableDelegate, () -> new TableDataSourceMulti(
                new TableDataSourcePreloaded(new TitledCell(new TableCellTitle("", IvTranslations.get("reccomplex.gui.source")))),
                new TableDataSourceMazePath(pair.getLeft(), bounds, tableDelegate, navigator),
                new TableDataSourcePreloaded(new TitledCell(new TableCellTitle("", IvTranslations.get("reccomplex.gui.destination")))),
                new TableDataSourceMazePath(pair.getRight(), bounds, tableDelegate, navigator)
        )
        {
            @Override
            public boolean canVisualize()
            {
                return visualizationContext != null;
            }

            @Override
            public GuiHider.Visualizer visualizer()
            {
                return TableDataSourceMazePath.visualizePaths(visualizationContext, Arrays.asList(pair.left, pair.right));
            }
        });
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Connections";
    }

    @Override
    public boolean canVisualize()
    {
        return visualizationContext != null;
    }

    @Override
    public GuiHider.Visualizer visualizer()
    {
        return TableDataSourceMazePath.visualizePaths(visualizationContext, list.stream()
                .flatMap(p -> Stream.of(p.left, p.right))
                .collect(Collectors.toSet())
        );
    }
}
