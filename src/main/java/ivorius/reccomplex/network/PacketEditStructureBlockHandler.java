/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import ivorius.ivtoolkit.tools.IvSideClient;
import ivorius.reccomplex.blocks.TileEntityStructureGenerator;
import ivorius.reccomplex.gui.editstructureblock.GuiEditStructureBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketEditStructureBlockHandler implements IMessageHandler<PacketEditStructureBlock, IMessage>
{
    @Override
    public IMessage onMessage(PacketEditStructureBlock message, MessageContext ctx)
    {
        if (ctx.side == Side.CLIENT)
        {
            TileEntity tileEntity = IvSideClient.getClientWorld().getTileEntity(message.getX(), message.getY(), message.getZ());
            if (tileEntity instanceof TileEntityStructureGenerator)
            {
                TileEntityStructureGenerator tileEntityStructureGenerator = ((TileEntityStructureGenerator) tileEntity);

                tileEntityStructureGenerator.readStructureDataFromNBT(message.getData());
                Minecraft.getMinecraft().displayGuiScreen(new GuiEditStructureBlock(tileEntityStructureGenerator));
            }
        }
        else
        {
            EntityPlayer player = ctx.getServerHandler().playerEntity;
            World world = player.worldObj;

            TileEntity tileEntity = world.getTileEntity(message.getX(), message.getY(), message.getZ());

            if (tileEntity instanceof TileEntityStructureGenerator)
            {
                ((TileEntityStructureGenerator) tileEntity).readStructureDataFromNBT(message.getData());
                tileEntity.markDirty();
                world.markBlockForUpdate(message.getX(), message.getY(), message.getZ());
            }
        }

        return null;
    }
}
