/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import ivorius.ivtoolkit.blocks.BlockPositions;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.ivtoolkit.maze.classic.MazeRoom;
import ivorius.ivtoolkit.rendering.grid.GridQuadCache;
import ivorius.reccomplex.client.rendering.OperationRenderer;
import ivorius.reccomplex.gui.GuiHider;
import ivorius.reccomplex.gui.table.TableCells;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceList;
import ivorius.reccomplex.world.gen.feature.structure.generic.Selection;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceSelection extends TableDataSourceList<Selection.Area, Selection>
{
    private int[] dimensions;
    protected boolean showIdentifier;

    protected Function<MazeRoom, BlockPos> realWorldMapper;

    public TableDataSourceSelection(Selection list, int[] dimensions, TableDelegate tableDelegate, TableNavigator navigator, boolean showIdentifier)
    {
        super(list, tableDelegate, navigator);
        this.dimensions = dimensions;
        this.showIdentifier = showIdentifier;
        duplicateTitle = TextFormatting.GREEN + "D";
    }

    public TableDataSourceSelection visualizing(Function<MazeRoom, BlockPos> realWorldMapper)
    {
        this.realWorldMapper = realWorldMapper;
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
        return TableCells.edit(enabled, navigator, tableDelegate, () -> new TableDataSourceSelectionArea(area, dimensions, showIdentifier));
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
        return realWorldMapper != null;
    }

    @Override
    public GuiHider.Visualizer visualizer()
    {
        return new Visualizer();
    }

    public class Visualizer implements GuiHider.Visualizer
    {
        protected GridQuadCache<?> quadCache;
        protected BlockPos lowerCoord;

        public Visualizer()
        {
            final Object handle = new Object();

            int[] one = new int[dimensions.length];
            Arrays.fill(one, 1);

            Selection realWorldSelection = new Selection(3);
            for (Selection.Area area : list)
            {
                BlockPos min = realWorldMapper.apply(new MazeRoom(area.getMinCoord()));
                // Add one and subtract later since we want it to fill the whole area
                BlockPos max = realWorldMapper.apply(new MazeRoom(IvVecMathHelper.add(area.getMaxCoord(), one)));
                max = max.subtract(new Vec3i(1, 1, 1));

                realWorldSelection.add(new Selection.Area(area.isAdditive(),
                        BlockPositions.toIntArray(min), BlockPositions.toIntArray(max),
                        area.getIdentifier()));
            }
            lowerCoord = BlockPositions.fromIntArray(realWorldSelection.boundsLower());
            Set<BlockPos> coords = realWorldSelection.compile(true).keySet().stream()
                    .map(r -> BlockPositions.fromIntArray(r.getCoordinates()))
                    .map(p -> p.subtract(lowerCoord))
                    .collect(Collectors.toSet());

            quadCache = GridQuadCache.createQuadCache(realWorldSelection.boundsSize(), new float[]{1, 1, 1}, input -> {
                BlockPos coord = input.getLeft();
                EnumFacing direction = input.getRight();

                return coords.contains(coord) && !coords.contains(coord.offset(direction))
                        ? handle
                        : null;
            });
        }

        @Override
        public void draw(Entity renderEntity, float partialTicks)
        {
            GlStateManager.color(0.8f, 0.75f, 0.5f);
            OperationRenderer.renderGridQuadCache(quadCache, AxisAlignedTransform2D.ORIGINAL, lowerCoord, renderEntity.ticksExisted, partialTicks);
        }
    }
}
