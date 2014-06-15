package ivorius.structuregen.gui;

import net.minecraft.client.gui.Gui;

/**
 * Created by lukas on 28.05.14.
 */
public interface GuiControlListener<G extends Gui>
{
    void valueChanged(G gui);
}
