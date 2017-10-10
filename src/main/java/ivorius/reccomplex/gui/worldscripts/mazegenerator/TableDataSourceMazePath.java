/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import com.google.common.collect.Iterables;
import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.client.rendering.MazeVisualizationContext;
import ivorius.reccomplex.client.rendering.SelectionQuadCache;
import ivorius.reccomplex.gui.GuiHider;
import ivorius.reccomplex.gui.TableDirections;
import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.cell.TableCellButton;
import ivorius.reccomplex.gui.table.cell.TableCellEnum;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.structure.generic.Selection;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.ConnectorStrategy;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.SavedMazePath;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.SavedMazePathConnection;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Created by lukas on 22.06.14.
 */
public class TableDataSourceMazePath extends TableDataSourceSegmented
{
    public static final String[] COORD_NAMES = {"x", "y", "z"};

    private SavedMazePath mazePath;
    private Selection bounds;
    private TableDelegate tableDelegate;

    private TableCellButton invertableButton;
    private final TableDataSourceMazeRoom source;

    protected MazeVisualizationContext visualizationContext;

    public TableDataSourceMazePath(SavedMazePath mazePath, Selection bounds, TableDelegate tableDelegate, TableNavigator navigator)
    {
        this.mazePath = mazePath;
        this.bounds = bounds;
        this.tableDelegate = tableDelegate;


        addManagedSegment(0, source =
                new TableDataSourceMazeRoom(mazePath.sourceRoom, mazeRoom -> {
                    this.mazePath.sourceRoom = mazeRoom;
                    if (invertableButton != null) invertableButton.setEnabled(isInvertable());
                }, bounds.bounds(),
                        Arrays.stream(COORD_NAMES).map(s -> IvTranslations.get("reccomplex.generationInfo.mazeComponent.position." + s)).collect(Collectors.toList()),
                        Arrays.stream(COORD_NAMES).map(s -> IvTranslations.getLines("reccomplex.generationInfo.mazeComponent.position." + s + ".tooltip")).collect(Collectors.toList()))
        );
    }

    public static boolean contains(int[] array, Selection bounds)
    {
        return bounds.compile(true).containsKey(new MazeRoom(array));
    }

    public static EnumFacing directionFromPath(SavedMazePath path)
    {
        switch (path.pathDimension)
        {
            case 0:
                return path.pathGoesUp ? EnumFacing.EAST : EnumFacing.WEST;
            case 1:
                return path.pathGoesUp ? EnumFacing.UP : EnumFacing.DOWN;
            case 2:
                return path.pathGoesUp ? EnumFacing.SOUTH : EnumFacing.NORTH;
        }

        return null;
    }

    public static SavedMazePathConnection pathFromDirection(EnumFacing side, int[] room)
    {
        int pathDim = side.getFrontOffsetX() != 0 ? 0 : side.getFrontOffsetY() != 0 ? 1 : side.getFrontOffsetZ() != 0 ? 2 : -1;
        int offset = side.getFrontOffsetX() + side.getFrontOffsetY() + side.getFrontOffsetZ();

        return new SavedMazePathConnection(pathDim, new MazeRoom(room[0], room[1], room[2]), offset > 0, ConnectorStrategy.DEFAULT_PATH, Collections.emptyList());
    }

    public static void addToSelection(Selection selection, SavedMazePath path)
    {
        selection.add(Selection.Area.from(true, path.sourceRoom.getCoordinates(), path.sourceRoom.getCoordinates(), "s"));
        selection.add(Selection.Area.from(true, path.getDestRoom().getCoordinates(), path.getDestRoom().getCoordinates(), "d"));
    }

    @NotNull
    public static GuiHider.Visualizer visualizePaths(MazeVisualizationContext visualizationContext, Collection<SavedMazePath> mazePaths)
    {
        if (mazePaths.size() <= 0)
            return new SelectionQuadCache.Visualizer(new Selection(0), visualizationContext);
        
        Selection selection = new Selection(Iterables.getFirst(mazePaths, null).sourceRoom.getDimensions());
        mazePaths.forEach(p -> addToSelection(selection, p));
        return new SelectionQuadCache.Visualizer(selection, visualizationContext);

    }

    public TableDataSourceMazePath visualizing(MazeVisualizationContext context)
    {
        this.visualizationContext = context;
        return this;
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Path";
    }

    @Override
    public int numberOfSegments()
    {
        return 3;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 1:
                return 1;
            case 2:
                return 1;
            default:
                return super.sizeOfSegment(segment);
        }
    }

    @Override
    public TableCell cellForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 1)
        {
            TableCellEnum.Option<EnumFacing>[] optionList = TableDirections.getDirectionOptions(EnumFacing.VALUES);

            TableCellEnum<EnumFacing> cell = new TableCellEnum<>("side", directionFromPath(mazePath), optionList);
            cell.addListener(val ->
            {
                SavedMazePathConnection path = pathFromDirection(val, mazePath.sourceRoom.getCoordinates());
                mazePath.pathDimension = path.path.pathDimension;
                mazePath.pathGoesUp = path.path.pathGoesUp;
                tableDelegate.reloadData();
            });
            return new TitledCell(IvTranslations.get("reccomplex.generationInfo.mazeComponent.path.side"), cell);
        }
        else if (segment == 2)
        {
            invertableButton = new TableCellButton("actions", "inverse", IvTranslations.get("reccomplex.generationInfo.mazeComponent.path.invert"), isInvertable());
            invertableButton.addAction(() ->
            {
                mazePath.set(mazePath.inverse());
                source.room = mazePath.sourceRoom;
                tableDelegate.reloadData();
            });
            return new TitledCell(invertableButton);
        }

        return super.cellForIndexInSegment(table, index, segment);
    }

    protected boolean isInvertable()
    {
        return contains(mazePath.inverse().getSourceRoom().getCoordinates(), bounds);
    }

    @Override
    public boolean canVisualize()
    {
        return visualizationContext != null;
    }

    @Override
    public GuiHider.Visualizer visualizer()
    {
        return visualizePaths(visualizationContext, Collections.singletonList(mazePath));
    }
}
