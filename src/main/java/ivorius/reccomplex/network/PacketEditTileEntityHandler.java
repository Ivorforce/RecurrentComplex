/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import ivorius.ivtoolkit.network.SchedulingMessageHandler;
import ivorius.ivtoolkit.tools.IvSideClient;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.blocks.TileEntityWithGUI;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketEditTileEntityHandler extends SchedulingMessageHandler<PacketEditTileEntity, IMessage>
{
    @Override
    public void processServer(PacketEditTileEntity message, MessageContext ctx, WorldServer server)
    {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        World world = player.worldObj;

        TileEntity tileEntity = world.getTileEntity(message.getPos());

        if (tileEntity instanceof TileEntityWithGUI)
        {
            ((TileEntityWithGUI) tileEntity).readSyncedNBT(message.getData());
            tileEntity.markDirty();
            IBlockState iblockstate = tileEntity.getWorld().getBlockState(message.getPos());
            world.notifyBlockUpdate(message.getPos(), iblockstate, iblockstate, 3);
        }
        else
            RecurrentComplex.logger.error("Invalid server TileEntity edit packet: " + tileEntity);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void processClient(PacketEditTileEntity message, MessageContext ctx)
    {
        TileEntity tileEntity = IvSideClient.getClientWorld().getTileEntity(message.getPos());
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
