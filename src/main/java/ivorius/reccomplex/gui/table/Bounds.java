/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by lukas on 02.06.14.
 */

@SideOnly(Side.CLIENT)
public class Bounds
{
    private int minX, maxX;
    private int minY, maxY;

    public Bounds(int minX, int maxX, int minY, int maxY)
    {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    public static Bounds fromAxes(int x, int width, int y, int height)
    {
        return new Bounds(x, x + width, y, y + height);
    }

    public static Bounds fromSize(int x, int y, int width, int height)
    {
        return new Bounds(x, x + width, y, y + height);
    }

    public static Bounds fromButton(GuiButton button)
    {
        return fromAxes(button.x, button.width, button.y, button.height);
    }

    public static Bounds fromTextField(GuiTextField textField)
    {
        return fromAxes(textField.x, textField.width, textField.y, textField.height);
    }

    public static void set(GuiButton button, Bounds bounds)
    {
        button.x = bounds.getMinX();
        button.y = bounds.getMinY();
        button.width = bounds.getWidth();
        button.height = bounds.getHeight();
    }

    public static void set(GuiTextField textField, Bounds bounds)
    {
        textField.x = bounds.getMinX();
        textField.y = bounds.getMinY();
        textField.width = bounds.getWidth();
        textField.height = bounds.getHeight();
        textField.setCursorPosition(textField.getCursorPosition()); // Update text field scroll
    }

    public int getMinX()
    {
        return minX;
    }

    public int getMaxX()
    {
        return maxX;
    }

    public int getMinY()
    {
        return minY;
    }

    public int getMaxY()
    {
        return maxY;
    }

    public int getWidth()
    {
        return maxX - minX;
    }

    public int getHeight()
    {
        return maxY - minY;
    }

    public int getCenterX()
    {
        return (minX + maxX) / 2;
    }

    public int getCenterY()
    {
        return (minY + maxY) / 2;
    }

    public boolean contains(int x, int y)
    {
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }
}
