/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.cell;

import ivorius.reccomplex.gui.table.Bounds;
import ivorius.reccomplex.gui.table.GuiTable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by lukas on 02.06.14.
 */
public class TitledCell extends TableCellDefault
{
    public static final int TITLE_WIDTH = 100;

    @Nullable
    protected String title;

    @Nonnull
    protected TableCell cell;

    protected List<String> titleTooltip;

    public TitledCell(@Nullable String id, @Nullable String title, @Nonnull TableCell cell)
    {
        super(id);
        this.title = title;
        this.cell = cell;
    }

    public TitledCell(@Nullable String title, @Nonnull TableCell cell)
    {
        this(null, title, cell);
    }

    public TitledCell(@Nonnull TableCell cell)
    {
        this(null, null, cell);
    }

    @Nullable
    public String getTitle()
    {
        return title;
    }

    public void setTitle(@Nullable String title)
    {
        this.title = title;
    }

    @Nonnull
    public TableCell getCell()
    {
        return cell;
    }

    public void setCell(@Nonnull TableCell cell)
    {
        this.cell = cell;
    }

    public List<String> getTitleTooltip()
    {
        return titleTooltip;
    }

    public void setTitleTooltip(List<String> titleTooltip)
    {
        this.titleTooltip = titleTooltip;
    }

    public TitledCell withTitleTooltip(List<String> tooltip)
    {
        setTitleTooltip(tooltip);
        return this;
    }

    @Override
    public void initGui(GuiTable screen)
    {
        super.initGui(screen);
        cell.initGui(screen);
    }

    @Override
    public void setBounds(Bounds bounds)
    {
        super.setBounds(bounds);
        cell.setBounds(Bounds.fromSize(bounds.getMinX() + TITLE_WIDTH, bounds.getMinY(), bounds.getWidth() - TITLE_WIDTH, bounds.getHeight()));
    }

    @Override
    public void setHidden(boolean hidden)
    {
        super.setHidden(hidden);
        cell.setHidden(hidden);
    }

    @Override
    public void draw(GuiTable screen, int mouseX, int mouseY, float partialTicks)
    {
        super.draw(screen, mouseX, mouseY, partialTicks);
        Bounds bounds = bounds();
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

        String title = getTitle();
        if (title != null)
        {
            int stringWidth = fontRenderer.getStringWidth(title);
            screen.drawString(fontRenderer, title, bounds.getMinX() + TITLE_WIDTH - stringWidth - 10, bounds.getCenterY() - 4, 0xffffffff);
        }

        cell.draw(screen, mouseX, mouseY, partialTicks);
    }

    @Override
    public void drawFloating(GuiTable screen, int mouseX, int mouseY, float partialTicks)
    {
        super.drawFloating(screen, mouseX, mouseY, partialTicks);
        cell.drawFloating(screen, mouseX, mouseY, partialTicks);

        Bounds bounds = bounds();
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

        String title = getTitle();
        if (title != null)
        {
            int stringWidth = Math.max(fontRenderer.getStringWidth(title), 100);

            List<String> tooltip = getTitleTooltip();
            if (tooltip != null)
                screen.drawTooltipRect(tooltip, Bounds.fromSize(bounds.getMinX() + TITLE_WIDTH- stringWidth - 10, bounds.getCenterY() - 6, stringWidth, 12), mouseX, mouseY, fontRenderer);
        }
    }

    @Override
    public void update(GuiTable screen)
    {
        super.update(screen);
        cell.update(screen);
    }

    @Override
    public boolean keyTyped(char keyChar, int keyCode)
    {
        return cell.keyTyped(keyChar, keyCode) || super.keyTyped(keyChar, keyCode);
    }

    @Override
    public void mouseClicked(int button, int x, int y)
    {
        super.mouseClicked(button, x, y);
        cell.mouseClicked(button, x, y);
    }

    @Override
    public void buttonClicked(int buttonID)
    {
        super.buttonClicked(buttonID);
        cell.buttonClicked(buttonID);
    }
}
