/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 02.06.14.
 */
public class TableElementTitle extends TableElementDefault
{
    protected String displayString;

    @Nonnull
    protected Positioning positioning = Positioning.CENTER;

    public TableElementTitle(String id, String title, String displayString)
    {
        super(id, title);
        this.displayString = displayString;
    }

    public String getDisplayString()
    {
        return displayString;
    }

    public void setDisplayString(String displayString)
    {
        this.displayString = displayString;
    }

    @Nonnull
    public Positioning getPositioning()
    {
        return positioning;
    }

    public void setPositioning(@Nonnull Positioning positioning)
    {
        this.positioning = positioning;
    }

    @Override
    public void draw(GuiTable screen, int mouseX, int mouseY, float partialTicks)
    {
        super.draw(screen, mouseX, mouseY, partialTicks);

        Bounds bounds = bounds();
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        screen.drawCenteredString(fontRenderer, displayString, bounds.getCenterX(), positioning.getY(fontRenderer, bounds.getMinY(), bounds.getMaxY()), 0xffffffff);
    }

    public enum Positioning
    {
        BOTTOM, CENTER, TOP;

        public int getY(FontRenderer renderer, int min, int max)
        {
            switch (this)
            {
                case TOP:
                    return min + 3;
                case CENTER:
                    return (min + max - renderer.FONT_HEIGHT) / 2;
                case BOTTOM:
                    return max - 2 - renderer.FONT_HEIGHT;
                default:
                    throw new InternalError();
            }
        }
    }
}
