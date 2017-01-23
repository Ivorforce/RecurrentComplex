/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.cell;

import ivorius.reccomplex.gui.table.Bounds;
import ivorius.reccomplex.gui.table.GuiTable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by lukas on 02.06.14.
 */
public abstract class TableCellDefault implements TableCell
{
    protected String id;

    private boolean hidden = false;

    private Bounds bounds = new Bounds(0, 0, 0, 0);

    protected List<String> tooltip;

    public TableCellDefault(String id)
    {
        this.id = id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    @Nullable
    @Override
    public String getID()
    {
        return id;
    }

    public List<String> getTooltip()
    {
        return tooltip;
    }

    public void setTooltip(List<String> tooltip)
    {
        this.tooltip = tooltip;
    }

    public TableCellDefault withTooltip(List<String> tooltip)
    {
        setTooltip(tooltip);
        return this;
    }

    @Override
    public void initGui(GuiTable screen)
    {

    }

    @Override
    public void setBounds(Bounds bounds)
    {
        this.bounds = bounds;
    }

    @Override
    public Bounds bounds()
    {
        return bounds;
    }

    @Override
    public void setHidden(boolean hidden)
    {
        this.hidden = hidden;
    }

    @Override
    public boolean isHidden()
    {
        return hidden;
    }

    public FontRenderer getFontRenderer()
    {
        return Minecraft.getMinecraft().fontRenderer;
    }

    @Override
    public void draw(GuiTable screen, int mouseX, int mouseY, float partialTicks)
    {
    }

    @Override
    public void drawFloating(GuiTable screen, int mouseX, int mouseY, float partialTicks)
    {
        List<String> tooltip = getTooltip();
        if (tooltip != null)
            screen.drawTooltipRect(tooltip, bounds(), mouseX, mouseY, getFontRenderer());
    }

    @Override
    public void update(GuiTable screen)
    {

    }

    @Override
    public boolean keyTyped(char keyChar, int keyCode)
    {
        return false;
    }

    @Override
    public void mouseClicked(int button, int x, int y)
    {

    }

    @Override
    public void buttonClicked(int buttonID)
    {

    }
}
