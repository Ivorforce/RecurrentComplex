/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.cell;

import ivorius.reccomplex.gui.table.Bounds;
import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.utils.RCStrings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by lukas on 02.06.14.
 */
public class ExpandableCell extends TableCellDefault
{
    @Nonnull
    protected TableCellButton camo = new TableCellButton(null, null, "");

    protected TableCellDefault cell;

    protected boolean expanded;

    public ExpandableCell(String id, String camoTitle, TableCellDefault cell)
    {
        super(id);
        setCamoTitle(camoTitle);
        this.cell = cell;

        camo.addAction(() -> setExpanded(true));
    }

    public ExpandableCell(String camoTitle, TableCellDefault cell)
    {
        this(null, camoTitle, cell);
    }

    public ExpandableCell(String camoTitle, TableCellDefault cell, boolean enabled)
    {
        this(null, camoTitle, cell);
        setEnabled(enabled);
    }

    public String getCamoTitle()
    {
        return camo.getTitle();
    }

    public void setCamoTitle(String camoTitle)
    {
        camo.setTitle(camoTitle);
    }

    public TableCell getCell()
    {
        return cell;
    }

    public void setCell(TableCellDefault cell)
    {
        this.cell = cell;
    }

    public boolean isExpanded()
    {
        return expanded;
    }

    public void setExpanded(boolean expanded)
    {
        this.expanded = expanded;
        setHidden(isHidden()); // Update visibility
    }

    @Override
    public void setBounds(Bounds bounds)
    {
        super.setBounds(bounds);
        cell.setBounds(bounds);
        camo.setBounds(bounds);
    }

    @Override
    public void setHidden(boolean hidden)
    {
        super.setHidden(hidden);
        cell.setHidden(hidden || !expanded);
        camo.setHidden(hidden || expanded);
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);

        cell.setEnabled(enabled);
        camo.setEnabled(enabled);
    }

    @Override
    public void initGui(GuiTable screen)
    {
        super.initGui(screen);

        cell.initGui(screen);
        camo.initGui(screen);
    }

    @Override
    public void draw(GuiTable screen, int mouseX, int mouseY, float partialTicks)
    {
        super.draw(screen, mouseX, mouseY, partialTicks);

        if (expanded)
            cell.draw(screen, mouseX, mouseY, partialTicks);
        else
            camo.draw(screen, mouseX, mouseY, partialTicks);
    }

    @Override
    public void drawFloating(GuiTable screen, int mouseX, int mouseY, float partialTicks)
    {
        super.drawFloating(screen, mouseX, mouseY, partialTicks);

        if (expanded)
            cell.drawFloating(screen, mouseX, mouseY, partialTicks);
        else
            camo.drawFloating(screen, mouseX, mouseY, partialTicks);
    }

    @Override
    public void update(GuiTable screen)
    {
        super.update(screen);

        cell.update(screen);
        camo.update(screen);
    }

    @Override
    public boolean keyTyped(char keyChar, int keyCode)
    {
        if (expanded)
            return cell.keyTyped(keyChar, keyCode) || super.keyTyped(keyChar, keyCode);
        else
            return camo.keyTyped(keyChar, keyCode) || super.keyTyped(keyChar, keyCode);
    }

    @Override
    public void mouseClicked(int button, int x, int y)
    {
        super.mouseClicked(button, x, y);

        if (expanded)
            cell.mouseClicked(button, x, y);
        else
            camo.mouseClicked(button, x, y);
    }

    @Override
    public void buttonClicked(int buttonID)
    {
        super.buttonClicked(buttonID);

        if (expanded)
            cell.buttonClicked(buttonID);
        else
            camo.buttonClicked(buttonID);
    }
}
