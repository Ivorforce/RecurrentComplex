/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.client.rendering;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockCoord;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;

/**
 * Created by lukas on 09.02.15.
 */
@SideOnly(Side.CLIENT)
public class AreaRenderer
{
    public static void renderArea(BlockArea area, boolean lined, float sizeP)
    {
        BlockCoord lower = area.getLowerCorner();
        BlockCoord higher = area.getHigherCorner();

        BlockCoord biggerMax = higher.add(1, 1, 1);

        drawCuboid(lower, biggerMax, lined, sizeP);
    }

    @SideOnly(Side.CLIENT)
    private static void drawCuboid(BlockCoord min, BlockCoord max, boolean lined, float sizeP)
    {
        float width2 = ((float) max.x - (float) min.x) * 0.5f;
        float height2 = ((float) max.y - (float) min.y) * 0.5f;
        float length2 = ((float) max.z - (float) min.z) * 0.5f;

        double centerX = min.x + width2;
        double centerY = min.y + height2;
        double centerZ = min.z + length2;

        GL11.glPushMatrix();
        GL11.glTranslated(centerX, centerY, centerZ);
        if (lined)
            drawLineCuboid(Tessellator.instance, width2 + sizeP, height2 + sizeP, length2 + sizeP, 1);
        else
            drawCuboid(Tessellator.instance, width2 + sizeP, height2 + sizeP, length2 + sizeP, 1);
        GL11.glPopMatrix();
    }

    public static void drawCuboid(Tessellator tessellator, float sizeX, float sizeY, float sizeZ, float in)
    {
        tessellator.startDrawingQuads();

        tessellator.addVertexWithUV(-sizeX * in, -sizeY * in, -sizeZ, 0, 0);
        tessellator.addVertexWithUV(-sizeX * in, sizeY * in, -sizeZ, 0, 1);
        tessellator.addVertexWithUV(sizeX * in, sizeY * in, -sizeZ, 1, 1);
        tessellator.addVertexWithUV(sizeX * in, -sizeY * in, -sizeZ, 1, 0);

        tessellator.addVertexWithUV(-sizeX * in, -sizeY * in, sizeZ, 0, 0);
        tessellator.addVertexWithUV(sizeX * in, -sizeY * in, sizeZ, 1, 0);
        tessellator.addVertexWithUV(sizeX * in, sizeY * in, sizeZ, 1, 1);
        tessellator.addVertexWithUV(-sizeX * in, sizeY * in, sizeZ, 0, 1);

        tessellator.addVertexWithUV(-sizeX, -sizeY * in, -sizeZ * in, 0, 0);
        tessellator.addVertexWithUV(-sizeX, -sizeY * in, sizeZ * in, 0, 1);
        tessellator.addVertexWithUV(-sizeX, sizeY * in, sizeZ * in, 1, 1);
        tessellator.addVertexWithUV(-sizeX, sizeY * in, -sizeZ * in, 1, 0);

        tessellator.addVertexWithUV(sizeX, -sizeY * in, -sizeZ * in, 0, 0);
        tessellator.addVertexWithUV(sizeX, sizeY * in, -sizeZ * in, 0, 1);
        tessellator.addVertexWithUV(sizeX, sizeY * in, sizeZ * in, 1, 1);
        tessellator.addVertexWithUV(sizeX, -sizeY * in, sizeZ * in, 1, 0);

        tessellator.addVertexWithUV(-sizeX * in, sizeY, -sizeZ * in, 0, 0);
        tessellator.addVertexWithUV(-sizeX * in, sizeY, sizeZ * in, 0, 1);
        tessellator.addVertexWithUV(sizeX * in, sizeY, sizeZ * in, 1, 1);
        tessellator.addVertexWithUV(sizeX * in, sizeY, -sizeZ * in, 1, 0);

        tessellator.addVertexWithUV(-sizeX * in, -sizeY, -sizeZ * in, 0, 0);
        tessellator.addVertexWithUV(sizeX * in, -sizeY, -sizeZ * in, 1, 0);
        tessellator.addVertexWithUV(sizeX * in, -sizeY, sizeZ * in, 1, 1);
        tessellator.addVertexWithUV(-sizeX * in, -sizeY, sizeZ * in, 0, 1);

        tessellator.draw();
    }

    public static void drawLineCuboid(Tessellator tessellator, float sizeX, float sizeY, float sizeZ, float in)
    {
        tessellator.startDrawing(GL11.GL_LINE_STRIP);

        tessellator.addVertex(-sizeX * in, -sizeY * in, -sizeZ);
        tessellator.addVertex(-sizeX * in, sizeY * in, -sizeZ);
        tessellator.addVertex(sizeX * in, sizeY * in, -sizeZ);
        tessellator.addVertex(sizeX * in, -sizeY * in, -sizeZ);
        tessellator.addVertex(-sizeX * in, -sizeY * in, -sizeZ);

        tessellator.addVertex(-sizeX * in, -sizeY * in, sizeZ);
        tessellator.addVertex(-sizeX * in, sizeY * in, sizeZ);
        tessellator.addVertex(sizeX * in, sizeY * in, sizeZ);
        tessellator.addVertex(sizeX * in, -sizeY * in, sizeZ);
        tessellator.addVertex(-sizeX * in, -sizeY * in, sizeZ);

        tessellator.draw();

        tessellator.startDrawing(GL11.GL_LINES);

        tessellator.addVertex(-sizeX * in, sizeY * in, -sizeZ);
        tessellator.addVertex(-sizeX * in, sizeY * in, sizeZ);

        tessellator.addVertex(sizeX * in, sizeY * in, -sizeZ);
        tessellator.addVertex(sizeX * in, sizeY * in, sizeZ);

        tessellator.addVertex(sizeX * in, -sizeY * in, -sizeZ);
        tessellator.addVertex(sizeX * in, -sizeY * in, sizeZ);

        tessellator.draw();
    }
}
