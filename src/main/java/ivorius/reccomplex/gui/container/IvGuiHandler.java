/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.container;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;

/**
 * Created by lukas on 28.08.16.
 */ // From IGuiHandler
public interface IvGuiHandler
{
    /**
     * Returns a Server side Container to be displayed to the user.
     *
     * @param id     The Gui ID Number
     * @param player The player viewing the Gui
     * @param data   Data
     * @return A GuiScreen/Container to be displayed to the user, null if none.
     */
    Container getServerGuiElement(int id, EntityPlayerMP player, ByteBuf data);

    /**
     * Returns a Container to be displayed to the user. On the client side, this
     * needs to return a instance of GuiScreen On the server side, this needs to
     * return a instance of Container
     *
     * @param id     The Gui ID Number
     * @param player The player viewing the Gui
     * @param data   Data
     * @return A GuiScreen/Container to be displayed to the user, null if none.
     */
    Object getClientGuiElement(int id, EntityPlayer player, ByteBuf data);
}
