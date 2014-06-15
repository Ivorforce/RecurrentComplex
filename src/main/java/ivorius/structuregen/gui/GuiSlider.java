/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.gui;

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
    protected int getHoverState(boolean mouseHovering)
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
            this.drawTexturedModalRect(this.xPosition + (int) (drawVal * (float) (this.width - 8)), this.yPosition, 0, 66, 4, 20);
            this.drawTexturedModalRect(this.xPosition + (int) (drawVal * (float) (this.width - 8)) + 4, this.yPosition, 196, 66, 4, 20);
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