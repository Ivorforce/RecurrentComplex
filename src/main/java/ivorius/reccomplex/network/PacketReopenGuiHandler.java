/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.network;

import ivorius.ivtoolkit.network.SchedulingMessageHandler;
import ivorius.reccomplex.gui.GuiHider;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketReopenGuiHandler extends SchedulingMessageHandler<PacketReopenGui, IMessage>
{
    @Override
    @SideOnly(Side.CLIENT)
    public void processClient(PacketReopenGui message, MessageContext ctx)
    {
        GuiHider.tryReopenGUI();
    }
}

