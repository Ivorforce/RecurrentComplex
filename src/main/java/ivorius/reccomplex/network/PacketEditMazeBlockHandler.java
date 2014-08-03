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
import ivorius.reccomplex.blocks.TileEntityMazeGenerator;
import ivorius.reccomplex.gui.editmazeblock.GuiEditMazeBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketEditMazeBlockHandler implements IMessageHandler<PacketEditMazeBlock, IMessage>
{
    @Override
    public IMessage onMessage(PacketEditMazeBlock message, MessageContext ctx)
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

            if (tileEntity instanceof TileEntityMazeGenerator)
            {
                ((TileEntityMazeGenerator) tileEntity).readMazeDataFromNBT(message.getData());
                tileEntity.markDirty();
                world.markBlockForUpdate(message.getX(), message.getY(), message.getZ());
            }
        }

        return null;
    }

    @SideOnly(Side.CLIENT)
    private void onMessageClient(PacketEditMazeBlock message, MessageContext ctx)
    {
        TileEntity tileEntity = IvSideClient.getClientWorld().getTileEntity(message.getX(), message.getY(), message.getZ());
        if (tileEntity instanceof TileEntityMazeGenerator)
        {
            TileEntityMazeGenerator tileEntityMazeGenerator = ((TileEntityMazeGenerator) tileEntity);

            tileEntityMazeGenerator.readMazeDataFromNBT(message.getData());
            Minecraft.getMinecraft().displayGuiScreen(new GuiEditMazeBlock(tileEntityMazeGenerator));
        }
    }
}
