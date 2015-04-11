/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

import net.minecraft.client.gui.FontRenderer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by lukas on 02.06.14.
 */
public class TableElementCell implements TableElement
{
    @Nullable
    protected String id;
    @Nullable
    protected String title;

    @Nonnull
    protected TableCellDefault cell;

    public TableElementCell(String id, String title, @Nonnull TableCellDefault cell)
    {
        this.id = id;
        this.title = title;
        this.cell = cell;
    }

    public TableElementCell(String title, @Nonnull TableCellDefault cell)
    {
        this(null, title, cell);
    }

    public TableElementCell(@Nonnull TableCellDefault cell)
    {
        this(null, "", cell);
    }

    @Nullable
    @Override
    public String getID()
    {
        return id;
    }

    public void setId(@Nonnull String id)
    {
        this.id = id;
    }

    @Override
    public String getTitle()
    {
        return title;
    }

    public void setTitle(@Nullable String title)
    {
        this.title = title;
    }

    @Nonnull
    public TableCellDefault getCell()
    {
        return cell;
    }

    public void setCell(@Nonnull TableCellDefault cell)
    {
        this.cell = cell;
    }

    @Override
    public void initGui(GuiTable screen)
    {
        cell.initGui(screen);
    }

    @Override
    public void setBounds(Bounds bounds)
    {
        cell.setBounds(bounds);
    }

    @Override
    public Bounds bounds()
    {
        return cell.bounds();
    }

    @Override
    public void setHidden(boolean hidden)
    {
        cell.setHidden(hidden);
    }

    @Override
    public boolean isHidden()
    {
        return cell.isHidden();
    }

    public FontRenderer getFontRenderer()
    {
        return cell.getFontRenderer();
    }

    @Override
    public void draw(GuiTable screen, int mouseX, int mouseY, float partialTicks)
    {
        cell.draw(screen, mouseX, mouseY, partialTicks);
    }

    @Override
    public void drawFloating(GuiTable screen, int mouseX, int mouseY, float partialTicks)
    {
        cell.drawFloating(screen, mouseX, mouseY, partialTicks);
    }

    @Override
    public void update(GuiTable screen)
    {
        cell.update(screen);
    }

    @Override
    public boolean keyTyped(char keyChar, int keyCode)
    {
        return cell.keyTyped(keyChar, keyCode);
    }

    @Override
    public void mouseClicked(int button, int x, int y)
    {
        cell.mouseClicked(button, x, y);
    }

    @Override
    public void buttonClicked(int buttonID)
    {
        cell.buttonClicked(buttonID);
    }
}
