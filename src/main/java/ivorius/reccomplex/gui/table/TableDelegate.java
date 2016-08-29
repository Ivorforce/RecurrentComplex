/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

import net.minecraft.client.gui.GuiButton;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 03.06.14.
 */
public interface TableDelegate
{
    @Nonnull
    <T extends GuiButton> T addButtonToTable(T button);

    void redrawTable();

    void reloadData();

    void setLocked(String element, boolean lock);
}
