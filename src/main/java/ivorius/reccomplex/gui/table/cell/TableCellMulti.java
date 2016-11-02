/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.cell;

import gnu.trove.impl.Constants;
import gnu.trove.map.TIntFloatMap;
import gnu.trove.map.hash.TIntFloatHashMap;
import ivorius.reccomplex.gui.table.Bounds;
import ivorius.reccomplex.gui.table.GuiTable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Created by lukas on 02.06.14.
 */
public class TableCellMulti implements TableCell
{
    public static final int CELL_MIN_WIDTH = 17;

    @Nullable
    protected String id;

    private Bounds bounds = new Bounds(0, 0, 0, 0);
    protected boolean hidden;

    @Nonnull
    protected final List<TableCell> cells = new ArrayList<>();
    protected TIntFloatMap cellsSize = new TIntFloatHashMap(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1, 1);

    public TableCellMulti(String id, @Nonnull List<? extends TableCell> cells)
    {
        this.id = id;
        this.cells.addAll(cells);
    }

    public TableCellMulti(String id, @Nonnull TableCell... cells)
    {
        this(id, Arrays.asList(cells));
    }

    public TableCellMulti(@Nonnull List<? extends TableCell> cells)
    {
        this(null, cells);
    }

    public TableCellMulti(@Nonnull TableCell... cells)
    {
        this(null, cells);
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

    public void setSize(int cell, float size)
    {
        cellsSize.put(cell, size);
    }

    public float getScalingSize(int cell)
    {
        return cellsSize.get(cell);
    }

    public int getScaledSize(int width, float total, int cell)
    {
        return (int) (width * (getScalingSize(cell) / total));
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
        float total = (float) IntStream.range(0, cells.size()).mapToDouble(this::getScalingSize).sum();

        int spreadableWidth = bounds.getWidth();
        for (int n = 0; n < cells.size(); n++) // Do it enough times in case a cell is too small next time
            for (int i = 0; i < cells.size(); i++)
            {
                if (getScaledSize(spreadableWidth, total, i) < CELL_MIN_WIDTH)
                {
                    total -= getScalingSize(i);
                    spreadableWidth -= CELL_MIN_WIDTH;
                }
            }

        int curPos = 0;
        for (int i = 0; i < cells.size(); i++)
        {
            int buttonWidth = Math.max(CELL_MIN_WIDTH, getScaledSize(spreadableWidth, total, i));
            TableCell cell = cells.get(i);
            int realWidth = buttonWidth - (i == cells.size() - 1 ? 0 : 2);
            cell.setBounds(Bounds.fromAxes(bounds.getMinX() + curPos, realWidth, bounds.getMinY(), bounds.getHeight()));

            curPos += buttonWidth;
        }

        this.bounds = bounds;
    }

    @Override
    public Bounds bounds()
    {
        return bounds;
    }

    @Override
    public boolean isHidden()
    {
        return hidden;
    }

    @Override
    public void setHidden(boolean hidden)
    {
        this.hidden = hidden;
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
