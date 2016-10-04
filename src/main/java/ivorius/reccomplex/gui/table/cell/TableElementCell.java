/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.cell;

import ivorius.reccomplex.gui.table.Bounds;
import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.gui.table.TableElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

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
    protected TableCell cell;

    protected List<String> titleTooltip;

    public TableElementCell(@Nullable String id, @Nullable String title, @Nonnull TableCell cell)
    {
        this.id = id;
        this.title = title;
        this.cell = cell;
    }

    public TableElementCell(@Nullable String title, @Nonnull TableCell cell)
    {
        this(null, title, cell);
    }

    public TableElementCell(@Nonnull TableCell cell)
    {
        this(null, null, cell);
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

    @Nullable
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
    public TableCell getCell()
    {
        return cell;
    }

    public void setCell(@Nonnull TableCell cell)
    {
        this.cell = cell;
    }

    @Override
    public List<String> getTitleTooltip()
    {
        return titleTooltip;
    }

    public void setTitleTooltip(List<String> titleTooltip)
    {
        this.titleTooltip = titleTooltip;
    }

    public TableElementCell withTitleTooltip(List<String> tooltip)
    {
        setTitleTooltip(tooltip);
        return this;
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
