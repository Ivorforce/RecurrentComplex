package ivorius.structuregen.gui.table;

import net.minecraft.client.gui.GuiButton;

/**
 * Created by lukas on 03.06.14.
 */
public interface TableDelegate
{
    void addButton(GuiButton button);

    void reloadData();
}
