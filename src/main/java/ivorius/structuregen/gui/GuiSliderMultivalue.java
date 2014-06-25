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
public class GuiSliderMultivalue extends GuiButton
{
    private float[] values;
    public int mousePressedInsideIndex = -1;
    private List<GuiControlListener<GuiSliderMultivalue>> listeners = new ArrayList<>();
    private float minValue = 0.0f;
    private float maxValue = 1.0f;

    public GuiSliderMultivalue(int id, int x, int y, int width, int height, int values, String displayString)
    {
        super(id, x, y, width, height, displayString);
        this.values = new float[values];
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
            if (this.mousePressedInsideIndex >= 0)
            {
                values[mousePressedInsideIndex] = (float) (x - (this.xPosition + 4)) / (float) (this.width - 8);
                values[mousePressedInsideIndex] = (values[mousePressedInsideIndex]) * (maxValue - minValue) + minValue;

                if (values[mousePressedInsideIndex] < minValue)
                {
                    values[mousePressedInsideIndex] = minValue;
                }

                if (values[mousePressedInsideIndex] > maxValue)
                {
                    values[mousePressedInsideIndex] = maxValue;
                }

                notifyOfChanges();
            }

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            for (float value : values)
            {
                float drawVal = (value - this.minValue) / (this.maxValue - this.minValue);
                this.drawTexturedModalRect(this.xPosition + (int) (drawVal * (float) (this.width - 8)), this.yPosition, 0, 66, 4, 20);
                this.drawTexturedModalRect(this.xPosition + (int) (drawVal * (float) (this.width - 8)) + 4, this.yPosition, 196, 66, 4, 20);
            }
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int x, int y)
    {
        if (super.mousePressed(mc, x, y))
        {
            float value = (float) (x - (this.xPosition + 4)) / (float) (this.width - 8);
            value = value * (maxValue - minValue) + minValue;

            float nearestDist = -1;
            for (int i = 0; i < values.length; i++)
            {
                float dist = Math.abs(values[i] - value);

                if (dist < nearestDist || nearestDist < 0)
                {
                    mousePressedInsideIndex = i;
                    nearestDist = dist;
                }
            }

            values[mousePressedInsideIndex] = value;

            if (values[mousePressedInsideIndex] < minValue)
            {
                values[mousePressedInsideIndex] = minValue;
            }

            if (values[mousePressedInsideIndex] > maxValue)
            {
                values[mousePressedInsideIndex] = maxValue;
            }

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
        this.mousePressedInsideIndex = -1;
    }

    private void notifyOfChanges()
    {
        for (GuiControlListener<GuiSliderMultivalue> listener : listeners)
        {
            listener.valueChanged(this);
        }
    }

    public float getValue(int index)
    {
        return this.values[index];
    }

    public void setValue(int index, float value)
    {
        this.values[index] = value;
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

    public void addListener(GuiControlListener<GuiSliderMultivalue> listener)
    {
        listeners.add(listener);
    }

    public void removeListener(GuiControlListener<GuiSliderMultivalue> listener)
    {
        listeners.remove(listener);
    }

    public List<GuiControlListener<GuiSliderMultivalue>> listeners()
    {
        return Collections.unmodifiableList(listeners);
    }
}