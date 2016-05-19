/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.client.rendering;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.reccomplex.utils.Icons;
import net.minecraft.util.BlockPos;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.rendering.grid.AreaRenderer;
import ivorius.ivtoolkit.rendering.grid.CubeMesh;
import ivorius.ivtoolkit.rendering.grid.GridQuadCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.EnumFacing;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

/**
 * Created by lukas on 21.03.15.
 */
public class OperationRenderer
{
    public static void applyTransformVisual(AxisAlignedTransform2D transform2D, float[] size)
    {
        if (transform2D.getRotation() % 2 == 1)
            GlStateManager.translate(size[2] * 0.5f, 0f, size[0] * 0.5f);
        else
            GlStateManager.translate(size[0] * 0.5f, 0f, size[2] * 0.5f);

        GlStateManager.rotate(-90.0f * transform2D.getRotation(), 0, 1, 0);

        if (transform2D.isMirrorX())
            GlStateManager.scale(-1, 1, 1);

        GlStateManager.translate(-size[0] * 0.5f, 0f, -size[2] * 0.5f);
    }

    public static void renderGridQuadCache(GridQuadCache<?> cached, AxisAlignedTransform2D transform, BlockPos lowerCoord, int ticks, float partialTicks)
    {
        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.5f);
        GlStateManager.disableCull();

        ResourceLocation curTex = Icons.frame(SelectionRenderer.LATTICE_TEXTURE, (ticks + partialTicks) * 0.75f);
        Minecraft.getMinecraft().renderEngine.bindTexture(curTex);

        GlStateManager.pushMatrix();
        GlStateManager.translate(lowerCoord.getX(), lowerCoord.getY(), lowerCoord.getZ());
        applyTransformVisual(transform, cached.getSize());

        Tessellator tessellator = Tessellator.getInstance();
        tessellator.getWorldRenderer().startDrawingQuads();
        for (GridQuadCache.CachedQuadLevel<?> cachedQuadLevel : cached)
        {
            EnumFacing direction = cachedQuadLevel.direction;
            float zLevel = cachedQuadLevel.zLevel + 0.01f * (direction.getFrontOffsetX() + direction.getFrontOffsetY() + direction.getFrontOffsetZ());

            FloatBuffer quads = cachedQuadLevel.quads;
            while (quads.position() < quads.limit() - 3)
            {
                float minX = quads.get(),
                        minY = quads.get(),
                        maxX = quads.get(),
                        maxY = quads.get();
                float[] minAxes = GridQuadCache.getNormalAxes(direction, zLevel, minX, minY);
                float[] maxAxes = GridQuadCache.getNormalAxes(direction, zLevel, maxX, maxY);

                CubeMesh.renderSide(direction, minAxes[0], minAxes[1], minAxes[2], maxAxes[0], maxAxes[1], maxAxes[2],
                        Icons.from(minX, minY, maxX, maxY));
            }
            quads.position(0);
        }
        tessellator.draw();
        GlStateManager.popMatrix();

        GlStateManager.enableCull();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.002f);
        GlStateManager.disableBlend();
    }

    public static void maybeRenderBoundingBox(BlockPos lowerCoord, int[] size, int ticks, float partialTicks)
    {
        if (size[0] > 0 && size[1] > 0 && size[2] > 0)
            renderBoundingBox(BlockArea.areaFromSize(lowerCoord, size), ticks, partialTicks);
    }

    public static void renderBoundingBox(BlockArea area, int ticks, float partialTicks)
    {
        GL11.glLineWidth(3.0f);
        GlStateManager.color(0.8f, 0.8f, 1.0f);
        AreaRenderer.renderAreaLined(area, 0.0232f);

        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0001f);

        ResourceLocation curTex = Icons.frame(SelectionRenderer.TEXTURE, (ticks + partialTicks) * 0.75f);
        Minecraft.getMinecraft().renderEngine.bindTexture(curTex);

        GlStateManager.color(0.6f, 0.6f, 0.8f, 0.3f);
        AreaRenderer.renderArea(area, false, true, 0.0132f);

        GlStateManager.color(0.8f, 0.8f, 1.0f, 0.5f);
        AreaRenderer.renderArea(area, false, false, 0.0132f);

        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.002f);
        GlStateManager.disableBlend();
    }
}
