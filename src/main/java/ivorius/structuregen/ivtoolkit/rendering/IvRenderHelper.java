/*
 * Copyright (c) 2014, Lukas Tenbrink.
 * http://lukas.axxim.net
 *
 * You are free to:
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes, unless you have a permit by the creator.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package ivorius.structuregen.ivtoolkit.rendering;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class IvRenderHelper
{
    public static void drawRectFullScreen(Minecraft mc)
    {
        drawRectFullScreen(mc.displayWidth, mc.displayHeight);
    }

    public static void drawRectFullScreen(int screenWidth, int screenHeight)
    {
        Tessellator tessellator = Tessellator.instance;

        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(0.0, 0.0, 0.0, 0.0, 1.0);
        tessellator.addVertexWithUV(0.0, screenHeight, 0.0, 0.0, 0.0);
        tessellator.addVertexWithUV(screenWidth, screenHeight, 0.0, 1.0, 0.0);
        tessellator.addVertexWithUV(screenWidth, 0.0, 0.0, 1.0, 1.0);
        tessellator.draw();
    }

    public static void renderCubeInvBlock(RenderBlocks rb, Block block)
    {
        block.setBlockBoundsForItemRender();
        Tessellator tessellator = Tessellator.instance;

        double x = 0.0;
        double y = 0.0;
        double z = 0.0;

        rb.setRenderBoundsFromBlock(block);
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, -1.0F, 0.0F);
        rb.renderFaceYNeg(block, x, y, z, rb.getBlockIconFromSide(block, 0));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        rb.renderFaceYPos(block, x, y, z, rb.getBlockIconFromSide(block, 1));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, -1.0F);
        rb.renderFaceZNeg(block, x, y, z, rb.getBlockIconFromSide(block, 2));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, 1.0F);
        rb.renderFaceZPos(block, x, y, z, rb.getBlockIconFromSide(block, 3));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(-1.0F, 0.0F, 0.0F);
        rb.renderFaceXNeg(block, x, y, z, rb.getBlockIconFromSide(block, 4));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(1.0F, 0.0F, 0.0F);
        rb.renderFaceXPos(block, x, y, z, rb.getBlockIconFromSide(block, 5));
        tessellator.draw();
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
    }

    public static void renderDefaultBlock(RenderBlocks rb, Block block, double x, double y, double z)
    {
        block.setBlockBoundsForItemRender();
        rb.setRenderBoundsFromBlock(block);
        rb.renderFaceYNeg(block, x, y, z, rb.getBlockIconFromSide(block, 0));
        rb.renderFaceYPos(block, x, y, z, rb.getBlockIconFromSide(block, 1));
        rb.renderFaceZNeg(block, x, y, z, rb.getBlockIconFromSide(block, 2));
        rb.renderFaceZPos(block, x, y, z, rb.getBlockIconFromSide(block, 3));
        rb.renderFaceXNeg(block, x, y, z, rb.getBlockIconFromSide(block, 4));
        rb.renderFaceXPos(block, x, y, z, rb.getBlockIconFromSide(block, 5));
    }

    public static void renderLights(float ticks, int color, float alpha, int number)
    {
        float width = 2.5f;

        Tessellator tessellator = Tessellator.instance;

        float usedTicks = ticks / 200.0F;

        Random random = new Random(432L);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDepthMask(false);
        GL11.glPushMatrix();

        for (int var7 = 0; (float) var7 < number; ++var7)
        {
            float xLogFunc = (((float) var7 / number * 28493.0f + ticks) / 10.0f) % 20.0f;
            if (xLogFunc > 10.0f)
            {
                xLogFunc = 20.0f - xLogFunc;
            }

            float yLogFunc = 1.0f / (1.0f + (float) Math.pow(2.71828f, -0.8f * xLogFunc) * ((1.0f / 0.01f) - 1.0f));

            float lightAlpha = yLogFunc;

            if (lightAlpha > 0.01f)
            {
                GL11.glRotatef(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(random.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(random.nextFloat() * 360.0F + usedTicks * 90.0F, 0.0F, 0.0F, 1.0F);
                tessellator.startDrawing(6);
                float var8 = random.nextFloat() * 20.0F + 5.0F;
                float var9 = random.nextFloat() * 2.0F + 1.0F;
                tessellator.setColorRGBA_I(color, (int) (255.0F * alpha * lightAlpha));
                tessellator.addVertex(0.0D, 0.0D, 0.0D);
                tessellator.setColorRGBA_I(color, 0);
                tessellator.addVertex(-width * (double) var9, var8, (-0.5F * var9));
                tessellator.addVertex(width * (double) var9, var8, (-0.5F * var9));
                tessellator.addVertex(0.0D, var8, (1.0F * var9));
                tessellator.addVertex(-width * (double) var9, var8, (-0.5F * var9));
                tessellator.draw();
            }
        }

        GL11.glPopMatrix();
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
    }

    public static void renderParticle(Tessellator par1Tessellator, float time, float scale)
    {
        float f1 = ActiveRenderInfo.rotationX;
        float f2 = ActiveRenderInfo.rotationZ;
        float f3 = ActiveRenderInfo.rotationYZ;
        float f4 = ActiveRenderInfo.rotationXY;
        float f5 = ActiveRenderInfo.rotationXZ;
//        double interpPosX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)time;
//        double interpPosY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)time;
//        double interpPosZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)time;

        float f6 = 0.0f;
        float f7 = 1.0f;
        float f8 = 0.0f;
        float f9 = 1.0f;
        float f10 = scale;

        float f11 = 0.0f;
        float f12 = 0.0f;
        float f13 = 0.0f;
        par1Tessellator.startDrawingQuads();
        par1Tessellator.addVertexWithUV((double) (f11 - f1 * f10 - f3 * f10), (double) (f12 - f5 * f10), (double) (f13 - f2 * f10 - f4 * f10), (double) f7, (double) f9);
        par1Tessellator.addVertexWithUV((double) (f11 - f1 * f10 + f3 * f10), (double) (f12 + f5 * f10), (double) (f13 - f2 * f10 + f4 * f10), (double) f7, (double) f8);
        par1Tessellator.addVertexWithUV((double) (f11 + f1 * f10 + f3 * f10), (double) (f12 + f5 * f10), (double) (f13 + f2 * f10 + f4 * f10), (double) f6, (double) f8);
        par1Tessellator.addVertexWithUV((double) (f11 + f1 * f10 - f3 * f10), (double) (f12 - f5 * f10), (double) (f13 + f2 * f10 - f4 * f10), (double) f6, (double) f9);
        par1Tessellator.draw();
    }

    public static void drawNormalCube(Tessellator tessellator, float size, float in, boolean lined)
    {
        if (lined)
        {
            tessellator.startDrawing(GL11.GL_LINE_STRIP);
        }
        else
        {
            tessellator.startDrawingQuads();
        }

        tessellator.addVertex(-size * in, -size * in, -size);
        tessellator.addVertex(size * in, -size * in, -size);
        tessellator.addVertex(size * in, size * in, -size);
        tessellator.addVertex(-size * in, size * in, -size);
        if (lined)
        {
            tessellator.draw();
        }

        if (lined)
        {
            tessellator.startDrawing(GL11.GL_LINE_STRIP);
        }
        tessellator.addVertex(-size * in, -size * in, size);
        tessellator.addVertex(-size * in, size * in, size);
        tessellator.addVertex(size * in, size * in, size);
        tessellator.addVertex(size * in, -size * in, size);
        if (lined)
        {
            tessellator.draw();
        }

        if (lined)
        {
            tessellator.startDrawing(GL11.GL_LINE_STRIP);
        }
        tessellator.addVertex(-size, -size * in, -size * in);
        tessellator.addVertex(-size, size * in, -size * in);
        tessellator.addVertex(-size, size * in, size * in);
        tessellator.addVertex(-size, -size * in, size * in);
        if (lined)
        {
            tessellator.draw();
        }

        if (lined)
        {
            tessellator.startDrawing(GL11.GL_LINE_STRIP);
        }
        tessellator.addVertex(size, -size * in, -size * in);
        tessellator.addVertex(size, -size * in, size * in);
        tessellator.addVertex(size, size * in, size * in);
        tessellator.addVertex(size, size * in, -size * in);
        if (lined)
        {
            tessellator.draw();
        }

        if (lined)
        {
            tessellator.startDrawing(GL11.GL_LINE_STRIP);
        }
        tessellator.addVertex(-size * in, size, -size * in);
        tessellator.addVertex(size * in, size, -size * in);
        tessellator.addVertex(size * in, size, size * in);
        tessellator.addVertex(-size * in, size, size * in);
        if (lined)
        {
            tessellator.draw();
        }

        if (lined)
        {
            tessellator.startDrawing(GL11.GL_LINE_STRIP);
        }
        tessellator.addVertex(-size * in, -size, -size * in);
        tessellator.addVertex(-size * in, -size, size * in);
        tessellator.addVertex(size * in, -size, size * in);
        tessellator.addVertex(size * in, -size, -size * in);

        tessellator.draw();
    }

    public static void drawCuboid(Tessellator tessellator, float sizeX, float sizeY, float sizeZ, float in, boolean lined)
    {
        if (lined)
        {
            tessellator.startDrawing(GL11.GL_LINE_STRIP);
        }
        else
        {
            tessellator.startDrawingQuads();
        }

        tessellator.addVertex(-sizeX * in, -sizeY * in, -sizeZ);
        tessellator.addVertex(-sizeX * in, sizeY * in, -sizeZ);
        tessellator.addVertex(sizeX * in, sizeY * in, -sizeZ);
        tessellator.addVertex(sizeX * in, -sizeY * in, -sizeZ);
        if (lined)
        {
            tessellator.draw();
        }

        if (lined)
        {
            tessellator.startDrawing(GL11.GL_LINE_STRIP);
        }
        tessellator.addVertex(-sizeX * in, -sizeY * in, sizeZ);
        tessellator.addVertex(sizeX * in, -sizeY * in, sizeZ);
        tessellator.addVertex(sizeX * in, sizeY * in, sizeZ);
        tessellator.addVertex(-sizeX * in, sizeY * in, sizeZ);
        if (lined)
        {
            tessellator.draw();
        }

        if (lined)
        {
            tessellator.startDrawing(GL11.GL_LINE_STRIP);
        }
        tessellator.addVertex(-sizeX, -sizeY * in, -sizeZ * in);
        tessellator.addVertex(-sizeX, -sizeY * in, sizeZ * in);
        tessellator.addVertex(-sizeX, sizeY * in, sizeZ * in);
        tessellator.addVertex(-sizeX, sizeY * in, -sizeZ * in);
        if (lined)
        {
            tessellator.draw();
        }

        if (lined)
        {
            tessellator.startDrawing(GL11.GL_LINE_STRIP);
        }
        tessellator.addVertex(sizeX, -sizeY * in, -sizeZ * in);
        tessellator.addVertex(sizeX, sizeY * in, -sizeZ * in);
        tessellator.addVertex(sizeX, sizeY * in, sizeZ * in);
        tessellator.addVertex(sizeX, -sizeY * in, sizeZ * in);
        if (lined)
        {
            tessellator.draw();
        }

        if (lined)
        {
            tessellator.startDrawing(GL11.GL_LINE_STRIP);
        }
        tessellator.addVertex(-sizeX * in, sizeY, -sizeZ * in);
        tessellator.addVertex(-sizeX * in, sizeY, sizeZ * in);
        tessellator.addVertex(sizeX * in, sizeY, sizeZ * in);
        tessellator.addVertex(sizeX * in, sizeY, -sizeZ * in);
        if (lined)
        {
            tessellator.draw();
        }

        if (lined)
        {
            tessellator.startDrawing(GL11.GL_LINE_STRIP);
        }
        tessellator.addVertex(-sizeX * in, -sizeY, -sizeZ * in);
        tessellator.addVertex(sizeX * in, -sizeY, -sizeZ * in);
        tessellator.addVertex(sizeX * in, -sizeY, sizeZ * in);
        tessellator.addVertex(-sizeX * in, -sizeY, sizeZ * in);

        tessellator.draw();
    }

    public static void renderCuboid(Tessellator tessellator, float sizeX, float sizeY, float sizeZ, float in)
    {
        tessellator.addVertex(-sizeX * in, -sizeY * in, -sizeZ);
        tessellator.addVertex(-sizeX * in, sizeY * in, -sizeZ);
        tessellator.addVertex(sizeX * in, sizeY * in, -sizeZ);
        tessellator.addVertex(sizeX * in, -sizeY * in, -sizeZ);

        tessellator.addVertex(-sizeX * in, -sizeY * in, sizeZ);
        tessellator.addVertex(sizeX * in, -sizeY * in, sizeZ);
        tessellator.addVertex(sizeX * in, sizeY * in, sizeZ);
        tessellator.addVertex(-sizeX * in, sizeY * in, sizeZ);

        tessellator.addVertex(-sizeX, -sizeY * in, -sizeZ * in);
        tessellator.addVertex(-sizeX, -sizeY * in, sizeZ * in);
        tessellator.addVertex(-sizeX, sizeY * in, sizeZ * in);
        tessellator.addVertex(-sizeX, sizeY * in, -sizeZ * in);

        tessellator.addVertex(sizeX, -sizeY * in, -sizeZ * in);
        tessellator.addVertex(sizeX, sizeY * in, -sizeZ * in);
        tessellator.addVertex(sizeX, sizeY * in, sizeZ * in);
        tessellator.addVertex(sizeX, -sizeY * in, sizeZ * in);

        tessellator.addVertex(-sizeX * in, sizeY, -sizeZ * in);
        tessellator.addVertex(-sizeX * in, sizeY, sizeZ * in);
        tessellator.addVertex(sizeX * in, sizeY, sizeZ * in);
        tessellator.addVertex(sizeX * in, sizeY, -sizeZ * in);

        tessellator.addVertex(-sizeX * in, -sizeY, -sizeZ * in);
        tessellator.addVertex(sizeX * in, -sizeY, -sizeZ * in);
        tessellator.addVertex(sizeX * in, -sizeY, sizeZ * in);
        tessellator.addVertex(-sizeX * in, -sizeY, sizeZ * in);
    }

    public static void drawModelCuboid(Tessellator tessellator, float x, float y, float z, float sizeX, float sizeY, float sizeZ)
    {
        float tM = 1.0f / 16.0f;

        float transX = (x + sizeX * 0.5f) * tM;
        float transY = (y + sizeY * 0.5f) * tM - 0.5f;
        float transZ = (z + sizeZ * 0.5f) * tM;

        tessellator.addTranslation(transX, transY, transZ);
        renderCuboid(tessellator, sizeX * tM * 0.5f, sizeY * tM * 0.5f, sizeZ * tM * 0.5f, 1.0f);
        tessellator.addTranslation(-transX, -transY, -transZ);
    }
}
