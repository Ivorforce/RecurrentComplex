/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

import net.minecraft.client.Minecraft;

/**
 * Created by lukas on 02.06.14.
 */
public class TableElementTitle extends TableElementDefault
{
    private String displayString;

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

    @Override
    public void draw(GuiTable screen, int mouseX, int mouseY, float partialTicks)
    {
        super.draw(screen, mouseX, mouseY, partialTicks);

        screen.drawCenteredString(Minecraft.getMinecraft().fontRenderer, displayString, bounds().getCenterX(), bounds().getCenterY(), 0xffffffff);
    }
}
