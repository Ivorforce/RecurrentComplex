/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

import net.minecraft.client.gui.GuiButton;

/**
 * Created by lukas on 03.06.14.
 */
public interface TableDelegate
{
    void addButton(GuiButton button);

    void redrawTable();

    void reloadData();
}
