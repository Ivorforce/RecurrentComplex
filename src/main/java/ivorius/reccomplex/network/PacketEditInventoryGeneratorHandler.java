/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import ivorius.reccomplex.gui.editinventorygen.GuiEditInventoryGen;
import ivorius.reccomplex.items.ItemInventoryGenerationTag;
import ivorius.reccomplex.worldgen.inventory.InventoryGeneratorSaveHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.ChatComponentTranslation;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketEditInventoryGeneratorHandler implements IMessageHandler<PacketEditInventoryGenerator, IMessage>
{
    @Override
    public IMessage onMessage(PacketEditInventoryGenerator message, MessageContext ctx)
    {
        if (ctx.side == Side.CLIENT)
        {
            Minecraft.getMinecraft().displayGuiScreen(new GuiEditInventoryGen(Minecraft.getMinecraft().thePlayer, message.getInventoryGenerator(), message.getKey()));
        }
        else
        {
            NetHandlerPlayServer playServer = ctx.getServerHandler();
            EntityPlayerMP player = playServer.playerEntity;

            if (InventoryGeneratorSaveHandler.saveInventoryGenerator(message.getInventoryGenerator(), message.getKey()))
            {
                player.addChatMessage(new ChatComponentTranslation("inventorygen.save.success", message.getKey()));

                InventoryGeneratorSaveHandler.reloadAllCustomInventoryGenerators();

                ItemStack heldItem = playServer.playerEntity.getHeldItem();
                if (heldItem != null && heldItem.getItem() instanceof ItemInventoryGenerationTag)
                {
                    ItemInventoryGenerationTag.setItemStackGeneratorKey(heldItem, message.getKey());
                }
            }
            else
            {
                player.addChatMessage(new ChatComponentTranslation("inventorygen.save.failure", message.getKey()));
            }
        }

        return null;
    }
}
