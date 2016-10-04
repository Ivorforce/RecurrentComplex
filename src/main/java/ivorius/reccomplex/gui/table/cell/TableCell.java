/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.cell;

import ivorius.reccomplex.gui.table.Bounds;
import ivorius.reccomplex.gui.table.GuiTable;

import javax.annotation.Nullable;

/**
 * Created by lukas on 30.05.14.
 */
public interface TableCell
{
    @Nullable
    String getID();

    void initGui(GuiTable screen);

    void setBounds(Bounds bounds);

    Bounds bounds();

    void setHidden(boolean hidden);

    boolean isHidden();

    void draw(GuiTable screen, int mouseX, int mouseY, float partialTicks);

    void drawFloating(GuiTable screen, int mouseX, int mouseY, float partialTicks);

    void update(GuiTable screen);

    boolean keyTyped(char keyChar, int keyCode);

    void mouseClicked(int button, int x, int y);

    void buttonClicked(int buttonID);
}
