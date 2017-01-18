/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.network;

import ivorius.ivtoolkit.network.SchedulingMessageHandler;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.nbt.TableDataSourceNBTTagCompound;
import ivorius.reccomplex.gui.table.screen.GuiScreenEditTable;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketInspectEntityHandler extends SchedulingMessageHandler<PacketInspectEntity, IMessage>
{
    @Override
    @SideOnly(Side.CLIENT)
    public void processClient(PacketInspectEntity message, MessageContext ctx)
    {
        GuiScreenEditTable<TableDataSourceNBTTagCompound> screen = new GuiScreenEditTable<>();
        screen.setDataSource(new TableDataSourceNBTTagCompound(screen, screen, message.getData()){
            @Nonnull
            @Override
            public String title()
            {
                return message.getName();
            }
        }, ds ->
                RecurrentComplex.network.sendToServer(new PacketInspectEntity(ds.getNbt(), message.getUuid(), message.getName())));
        Minecraft.getMinecraft().displayGuiScreen(screen);
    }

    @Override
    public void processServer(PacketInspectEntity message, MessageContext ctx, WorldServer world)
    {
        NetHandlerPlayServer playServer = ctx.getServerHandler();
        EntityPlayerMP player = playServer.playerEntity;

        if (RecurrentComplex.checkPerms(player)) return;

        Entity entity = world.getEntityFromUuid(message.getUuid());

        if (entity != null)
            entity.readFromNBT(message.getData());
    }
}

