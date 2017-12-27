/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.network;

import ivorius.ivtoolkit.network.SchedulingMessageHandler;
import ivorius.reccomplex.gui.loot.GuiEditLootTable;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketEditLootTableHandler extends SchedulingMessageHandler<PacketEditLootTable, IMessage>
{
    @SideOnly(Side.CLIENT)
    @Override
    public void processClient(PacketEditLootTable message, MessageContext ctx)
    {
        Minecraft.getMinecraft().displayGuiScreen(new GuiEditLootTable(Minecraft.getMinecraft().player, message.getComponent(), message.getKey(), message.getSaveDirectoryData()));
    }
}
