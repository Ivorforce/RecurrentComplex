/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.client.rendering;

import ivorius.ivtoolkit.blocks.BlockPositions;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.rendering.grid.GridQuadCache;
import ivorius.reccomplex.gui.GuiHider;
import ivorius.reccomplex.world.gen.feature.structure.generic.Selection;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.Set;
import java.util.stream.Collectors;

public class SelectionQuadCache
{
    public static class Visualizer implements GuiHider.Visualizer
    {
        protected GridQuadCache<?> quadCache;
        protected BlockPos lowerCoord;

        public Visualizer(Selection selection, MazeVisualizationContext context)
        {
            final Object handle = new Object();

            Selection realWorldSelection = context.mapSelection(selection);
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
