/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import ivorius.ivtoolkit.network.SchedulingMessageHandler;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.editinventorygen.GuiEditInventoryGen;
import ivorius.reccomplex.items.ItemInventoryGenComponentTag;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.worldgen.inventory.ItemCollectionSaveHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Collections;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketEditInventoryGeneratorHandler extends SchedulingMessageHandler<PacketEditInventoryGenerator, IMessage>
{
    @Override
    public void processServer(PacketEditInventoryGenerator message, MessageContext ctx, WorldServer server)
    {
        NetHandlerPlayServer playServer = ctx.getServerHandler();
        EntityPlayerMP player = playServer.playerEntity;

        if (ItemCollectionSaveHandler.saveInventoryGenerator(message.getInventoryGenerator(), message.getKey()))
        {
            player.addChatMessage(ServerTranslations.format("inventorygen.save.success", message.getKey()));

            RecurrentComplex.fileTypeRegistry.reloadCustomFiles(Collections.singletonList(ItemCollectionSaveHandler.FILE_SUFFIX));

            ItemStack heldItem = playServer.playerEntity.getHeldItem();
            if (heldItem != null && heldItem.getItem() instanceof ItemInventoryGenComponentTag)
                ItemInventoryGenComponentTag.setComponentKey(heldItem, message.getKey());
            player.openContainer.detectAndSendChanges();
        }
        else
        {
            player.addChatMessage(ServerTranslations.format("inventorygen.save.failure", message.getKey()));
        }
    }

    @Override
    public void processClient(PacketEditInventoryGenerator message, MessageContext ctx)
    {
        Minecraft.getMinecraft().displayGuiScreen(new GuiEditInventoryGen(Minecraft.getMinecraft().thePlayer, message.getInventoryGenerator(), message.getKey()));
    }
}
