/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by lukas on 02.06.14.
 */
public class TableCellMulti implements TableCell
{
    @Nullable
    protected String id;

    private Bounds bounds = new Bounds(0, 0, 0, 0);
    protected boolean hidden;

    @Nonnull
    protected TableCell[] cells;

    public TableCellMulti(String id, @Nonnull TableCell... cells)
    {
        this.id = id;
        this.cells = cells;
    }

    public TableCellMulti(@Nonnull TableCell... cells)
    {
        this(null, cells);
    }

    public TableCellMulti(String id, @Nonnull List<? extends TableCell> cells)
    {
        this(id, cells.toArray(new TableCell[cells.size()]));
    }

    public TableCellMulti(@Nonnull List<? extends TableCell> cells)
    {
        this(null, cells.toArray(new TableCell[cells.size()]));
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

    @Nonnull
    public TableCell[] getCells()
    {
        return cells;
    }

    public void setCells(@Nonnull TableCell[] cells)
    {
        this.cells = cells;
    }

    @Override
    public void initGui(GuiTable screen)
    {
        for (TableCell cell : cells)
            cell.initGui(screen);
    }

    @Override
    public void setBounds(Bounds bounds)
    {
        int buttonWidth = bounds.getWidth() / cells.length;

        for (int i = 0; i < cells.length; i++)
        {
            TableCell cell = cells[i];
            int realWidth = buttonWidth - (i == cells.length - 1 ? 0 : 2);
            cell.setBounds(Bounds.fromAxes(bounds.getMinX() + buttonWidth * i, realWidth, bounds.getMinY(), bounds.getHeight()));
        }

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
    
    @Override
    public void draw(GuiTable screen, int mouseX, int mouseY, float partialTicks)
    {
        for (TableCell cell : cells)
            if (!cell.isHidden())
                cell.draw(screen, mouseX, mouseY, partialTicks);
    }

    @Override
    public void drawFloating(GuiTable screen, int mouseX, int mouseY, float partialTicks)
    {
        for (TableCell cell : cells)
            if (!cell.isHidden())
                cell.drawFloating(screen, mouseX, mouseY, partialTicks);
    }

    @Override
    public void update(GuiTable screen)
    {
        for (TableCell cell : cells)
            cell.update(screen);
    }

    @Override
    public boolean keyTyped(char keyChar, int keyCode)
    {
        for (TableCell cell : cells)
        {
            if (cell.keyTyped(keyChar, keyCode))
                return true;
        }

        return false;
    }

    @Override
    public void mouseClicked(int button, int x, int y)
    {
        for (TableCell cell : cells)
            cell.mouseClicked(button, x, y);
    }

    @Override
    public void buttonClicked(int buttonID)
    {
        for (TableCell cell : cells)
            cell.buttonClicked(buttonID);
    }
}
