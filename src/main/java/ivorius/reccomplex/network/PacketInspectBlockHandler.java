/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.network;

import ivorius.ivtoolkit.network.SchedulingMessageHandler;
import ivorius.reccomplex.gui.inspector.GuiBlockInspector;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketInspectBlockHandler extends SchedulingMessageHandler<PacketInspectBlock, IMessage>
{
    @Override
    @SideOnly(Side.CLIENT)
    public void processClient(PacketInspectBlock message, MessageContext ctx)
    {
        Minecraft.getMinecraft().displayGuiScreen(new GuiBlockInspector(message.getPos(), message.getState()));
    }
}

