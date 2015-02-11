/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ivorius.ivtoolkit.tools.IvSideClient;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.blocks.TileEntityWithGUI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketEditTileEntityHandler implements IMessageHandler<PacketEditTileEntity, IMessage>
{
    @Override
    public IMessage onMessage(PacketEditTileEntity message, MessageContext ctx)
    {
        if (ctx.side == Side.CLIENT)
        {
            onMessageClient(message, ctx);
        }
        else
        {
            EntityPlayer player = ctx.getServerHandler().playerEntity;
            World world = player.worldObj;

            TileEntity tileEntity = world.getTileEntity(message.getX(), message.getY(), message.getZ());

            if (tileEntity instanceof TileEntityWithGUI)
            {
                ((TileEntityWithGUI) tileEntity).readSyncedNBT(message.getData());
                tileEntity.markDirty();
                world.markBlockForUpdate(message.getX(), message.getY(), message.getZ());
            }
            else
                RecurrentComplex.logger.error("Invalid server TileEntity edit packet: " + tileEntity);
        }

        return null;
    }

    @SideOnly(Side.CLIENT)
    private void onMessageClient(PacketEditTileEntity message, MessageContext ctx)
    {
        TileEntity tileEntity = IvSideClient.getClientWorld().getTileEntity(message.getX(), message.getY(), message.getZ());
        if (tileEntity instanceof TileEntityWithGUI)
        {
            TileEntityWithGUI tileEntityGUI = (TileEntityWithGUI) tileEntity;

            tileEntityGUI.readSyncedNBT(message.getData());
            tileEntityGUI.openEditGUI();
        }
        else
            RecurrentComplex.logger.error("Invalid client TileEntity edit packet: " + tileEntity);
    }
}
