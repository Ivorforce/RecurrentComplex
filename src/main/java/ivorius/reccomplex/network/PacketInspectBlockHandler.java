/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.network;

import ivorius.ivtoolkit.network.SchedulingMessageHandler;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.inspector.GuiInspectBlock;
import ivorius.reccomplex.world.gen.feature.structure.OperationClearArea;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
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
        Minecraft.getMinecraft().displayGuiScreen(new GuiInspectBlock(message.getPos(), message.getState(), message.getTileEntityData()));
    }

    @Override
    public void processServer(PacketInspectBlock message, MessageContext ctx, WorldServer world)
    {
        NetHandlerPlayServer playServer = ctx.getServerHandler();
        EntityPlayerMP player = playServer.player;

        if (RecurrentComplex.checkPerms(player)) return;

        setBlock(world, message.getPos(), message.getState(), message.getTileEntityData());
    }

    public static void setBlock(WorldServer world, BlockPos pos, IBlockState state, NBTTagCompound tileEntityData)
    {
        OperationClearArea.emptyOut(world, pos);
        world.setBlockState(pos, state, 2);

        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity != null && tileEntityData != null)
        {
            tileEntityData.setInteger("x", pos.getX());
            tileEntityData.setInteger("y", pos.getY());
            tileEntityData.setInteger("z", pos.getZ());
            tileEntity.readFromNBT(tileEntityData);
        }
    }
}

