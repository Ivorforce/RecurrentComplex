/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.ivtoolkit.bezier;

import ivorius.ivtoolkit.math.IvMathHelper;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;

/**
 * Created by lukas on 20.04.14.
 */
public class IvBezierPath3DRendererTexture
{
    private double lineWidth;
    private double stepSize;
    private double textureShift;

    public IvBezierPath3DRendererTexture()
    {
        lineWidth = 1.0;
        stepSize = 0.1;
    }

    public double getLineWidth()
    {
        return lineWidth;
    }

    public void setLineWidth(double lineWidth)
    {
        this.lineWidth = lineWidth;
    }

    public double getStepSize()
    {
        return stepSize;
    }

    public void setStepSize(double stepSize)
    {
        this.stepSize = stepSize;
    }

    public double getTextureShift()
    {
        return textureShift;
    }

    public void setTextureShift(double textureShift)
    {
        this.textureShift = textureShift;
    }

    public void render(IvBezierPath3D path)
    {
        if (path.isDirty())
        {
            path.buildDistances();
        }

        Tessellator tessellator = Tessellator.instance;

        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        for (double progress = 0.0; progress < (1.0 + stepSize); progress += stepSize)
        {
            boolean isVeryFirst = progress == 0.0;
            boolean isVeryLast = progress >= 1.0;

            double totalProgress = Math.min(progress, 1.0);
            IvBezierPoint3DCachedStep cachedStep = path.getCachedStep(totalProgress);
            double[] position = cachedStep.getPosition();
            double[] pVector = path.getPVector(cachedStep, stepSize);

            double red = IvMathHelper.mix(cachedStep.getLeftPoint().getRed(), cachedStep.getRightPoint().getRed(), cachedStep.getInnerProgress());
            double green = IvMathHelper.mix(cachedStep.getLeftPoint().getGreen(), cachedStep.getRightPoint().getGreen(), cachedStep.getInnerProgress());
            double blue = IvMathHelper.mix(cachedStep.getLeftPoint().getBlue(), cachedStep.getRightPoint().getBlue(), cachedStep.getInnerProgress());
            double alpha = IvMathHelper.mix(cachedStep.getLeftPoint().getAlpha(), cachedStep.getRightPoint().getAlpha(), cachedStep.getInnerProgress());

            double textureX = totalProgress + textureShift;
            if (!isVeryFirst)
            {
                tessellator.setColorRGBA_F((float) red, (float) green, (float) blue, (float) alpha);
                tessellator.addVertexWithUV(position[0] - pVector[0] * lineWidth, position[1] - pVector[1] * lineWidth, position[2] - pVector[2] * lineWidth, textureX, 0);
                tessellator.addVertexWithUV(position[0] + pVector[0] * lineWidth, position[1] + pVector[1] * lineWidth, position[2] + pVector[2] * lineWidth, textureX, 1);
                tessellator.draw();
            }

            if (!isVeryLast)
            {
                tessellator.startDrawingQuads();
                tessellator.setColorRGBA_F((float) red, (float) green, (float) blue, (float) alpha);
                tessellator.addVertexWithUV(position[0] + pVector[0] * lineWidth, position[1] + pVector[1] * lineWidth, position[2] + pVector[2] * lineWidth, textureX, 1);
                tessellator.addVertexWithUV(position[0] - pVector[0] * lineWidth, position[1] - pVector[1] * lineWidth, position[2] - pVector[2] * lineWidth, textureX, 0);
            }
        }

        GL11.glDisable(GL11.GL_BLEND);
    }
}
