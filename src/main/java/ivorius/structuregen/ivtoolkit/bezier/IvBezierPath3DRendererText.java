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

package ivorius.structuregen.ivtoolkit.bezier;

import ivorius.structuregen.ivtoolkit.math.IvMathHelper;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

/**
 * Created by lukas on 20.04.14.
 */
public class IvBezierPath3DRendererText
{
    private FontRenderer fontRenderer;
    private String text;
    private boolean spreadToFill;
    private double shift;
    private boolean inwards;
    private double capBottom;
    private double capTop;

    public IvBezierPath3DRendererText()
    {
        capTop = 1.0;
    }

    public FontRenderer getFontRenderer()
    {
        return fontRenderer;
    }

    public void setFontRenderer(FontRenderer fontRenderer)
    {
        this.fontRenderer = fontRenderer;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public boolean isSpreadToFill()
    {
        return spreadToFill;
    }

    public void setSpreadToFill(boolean spreadToFill)
    {
        this.spreadToFill = spreadToFill;
    }

    public double getShift()
    {
        return shift;
    }

    public void setShift(double shift)
    {
        this.shift = shift;
    }

    public boolean isInwards()
    {
        return inwards;
    }

    public void setInwards(boolean inwards)
    {
        this.inwards = inwards;
    }

    public double getCapBottom()
    {
        return capBottom;
    }

    public void setCapBottom(double capBottom)
    {
        this.capBottom = capBottom;
    }

    public double getCapTop()
    {
        return capTop;
    }

    public void setCapTop(double capTop)
    {
        this.capTop = capTop;
    }

    public void render(IvBezierPath3D path)
    {
        if (path.isDirty())
        {
            path.buildDistances();
        }

        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        String plainText = "";
        ArrayList<String> modifiers = new ArrayList<String>();
        modifiers.add("");

        for (int i = 0; i < text.length(); i++)
        {
            char character = text.charAt(i);

            if (character == '\u00A7' && i + 1 < text.length())
            {
                modifiers.set(modifiers.size() - 1, modifiers.get(modifiers.size() - 1) + text.substring(i, i + 2));
                i++;
            }
            else
            {
                plainText = plainText + character;
                modifiers.add(modifiers.get(modifiers.size() - 1));
            }
        }

        for (int i = 0; i < plainText.length(); i++)
        {
            int charIndex = inwards ? i : plainText.length() - i - 1;
            char character = plainText.charAt(charIndex);

            if (character != ' ')
            {
                double totalProgress = (spreadToFill ? ((double) i / (double) text.length()) : (i * 0.5)) + shift;
                double finalProgress = ((totalProgress % 1.0) + 1.0) % 1.0;

                if (finalProgress >= capBottom && finalProgress <= capTop)
                {
                    IvBezierPoint3DCachedStep cachedStep = path.getCachedStep(finalProgress);
                    double[] position = cachedStep.getPosition();
                    double[] rotation = path.getNaturalRotation(cachedStep, 0.01);

                    double red = IvMathHelper.mix(cachedStep.getLeftPoint().getRed(), cachedStep.getRightPoint().getRed(), cachedStep.getInnerProgress());
                    double green = IvMathHelper.mix(cachedStep.getLeftPoint().getGreen(), cachedStep.getRightPoint().getGreen(), cachedStep.getInnerProgress());
                    double blue = IvMathHelper.mix(cachedStep.getLeftPoint().getBlue(), cachedStep.getRightPoint().getBlue(), cachedStep.getInnerProgress());
                    double alpha = IvMathHelper.mix(cachedStep.getLeftPoint().getAlpha(), cachedStep.getRightPoint().getAlpha(), cachedStep.getInnerProgress());

                    double textSize = IvMathHelper.mix(cachedStep.getLeftPoint().getFontSize(), cachedStep.getRightPoint().getFontSize(), cachedStep.getInnerProgress());

                    GL11.glPushMatrix();
                    GL11.glTranslated(position[0], position[1], position[2]);
                    GL11.glScaled(-textSize / 12.0, -textSize / 12.0, -textSize / 12.0);
                    GL11.glRotatef((float) rotation[0] + (inwards ? 0.0f : 180.0f), 0.0f, 1.0f, 0.0f);
                    GL11.glRotatef((float) rotation[1], 1.0f, 0.0f, 0.0f);
                    fontRenderer.drawString(modifiers.get(charIndex) + character, 0, 0, ((int) (red * 255.0) << 16) + ((int) (green * 255.0) << 8) + ((int) (blue * 255.0)));
                    GL11.glPopMatrix();
                }
            }
        }

        GL11.glDisable(GL11.GL_BLEND);
    }
}
