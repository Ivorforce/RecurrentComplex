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

package ivorius.ivtoolkit.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 28.05.14.
 */
public class GuiSlider extends GuiButton
{
    private float value;
    public boolean mousePressedInside;
    private List<GuiControlListener<GuiSlider>> listeners = new ArrayList<>();
    private float minValue = 0.0f;
    private float maxValue = 1.0f;

    public GuiSlider(int id, int x, int y, int width, int height, String displayString)
    {
        super(id, x, y, width, height, displayString);
    }

    @Override
    public int getHoverState(boolean mouseHovering)
    {
        return 0;
    }

    @Override
    protected void mouseDragged(Minecraft mc, int x, int y) // Is actually some kind of draw method
    {
        if (this.visible)
        {
            if (this.mousePressedInside)
            {
                this.value = (float) (x - (this.xPosition + 4)) / (float) (this.width - 8);
                this.value = (this.value) * (maxValue - minValue) + minValue;

                if (this.value < minValue)
                {
                    this.value = minValue;
                }

                if (this.value > maxValue)
                {
                    this.value = maxValue;
                }

                notifyOfChanges();
            }

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            float drawVal = (this.value - this.minValue) / (this.maxValue - this.minValue);
            this.drawTexturedModalRect(this.xPosition + (int) (drawVal * (float) (this.width - 8)), this.yPosition, 0, 66, 4, height);
            this.drawTexturedModalRect(this.xPosition + (int) (drawVal * (float) (this.width - 8)) + 4, this.yPosition, 196, 66, 4, height);
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int x, int y)
    {
        if (super.mousePressed(mc, x, y))
        {
            this.value = (float) (x - (this.xPosition + 4)) / (float) (this.width - 8);
            this.value = (this.value) * (maxValue - minValue) + minValue;

            if (this.value < minValue)
            {
                this.value = minValue;
            }

            if (this.value > maxValue)
            {
                this.value = maxValue;
            }

            this.mousePressedInside = true;
            notifyOfChanges();

            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public void mouseReleased(int x, int y)
    {
        this.mousePressedInside = false;
    }

    private void notifyOfChanges()
    {
        for (GuiControlListener<GuiSlider> listener : listeners)
        {
            listener.valueChanged(this);
        }
    }

    public float getValue()
    {
        return value;
    }

    public void setValue(float value)
    {
        this.value = value;
    }

    public float getMinValue()
    {
        return minValue;
    }

    public void setMinValue(float minValue)
    {
        this.minValue = minValue;
    }

    public float getMaxValue()
    {
        return maxValue;
    }

    public void setMaxValue(float maxValue)
    {
        this.maxValue = maxValue;
    }

    public void addListener(GuiControlListener<GuiSlider> listener)
    {
        listeners.add(listener);
    }

    public void removeListener(GuiControlListener<GuiSlider> listener)
    {
        listeners.remove(listener);
    }

    public List<GuiControlListener<GuiSlider>> listeners()
    {
        return Collections.unmodifiableList(listeners);
    }
}