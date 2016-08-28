/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.network;

import ivorius.ivtoolkit.network.SchedulingMessageHandler;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.inventorygen.GuiEditInventoryGen;
import ivorius.reccomplex.gui.inventorygen.GuiEditInventoryGenItems;
import ivorius.reccomplex.items.ItemInventoryGenComponentTag;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.worldgen.inventory.ItemCollectionSaveHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketEditInvGenHandler extends SchedulingMessageHandler<PacketEditInvGen, IMessage>
{
    @Override
    public void processServer(PacketEditInvGen message, MessageContext ctx, WorldServer server)
    {
        NetHandlerPlayServer playServer = ctx.getServerHandler();
        EntityPlayerMP player = playServer.playerEntity;

        if (RecurrentComplex.checkPerms(player)) return;

        if (ItemCollectionSaveHandler.saveInventoryGenerator(message.getInventoryGenerator(), message.getKey()))
        {
            player.addChatMessage(ServerTranslations.format("inventorygen.save.success", message.getKey()));

            RecurrentComplex.fileTypeRegistry.reloadCustomFiles(Collections.singletonList(ItemCollectionSaveHandler.FILE_SUFFIX));

            ItemStack heldItem = playServer.playerEntity.getHeldItem(EnumHand.MAIN_HAND);
            if (heldItem != null && heldItem.getItem() instanceof ItemInventoryGenComponentTag)
                ItemInventoryGenComponentTag.setComponentKey(heldItem, message.getKey());
            player.openContainer.detectAndSendChanges();
        }
        else
        {
            player.addChatMessage(ServerTranslations.format("inventorygen.save.failure", message.getKey()));
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void processClient(PacketEditInvGen message, MessageContext ctx)
    {
        Minecraft.getMinecraft().displayGuiScreen(new GuiEditInventoryGen(Minecraft.getMinecraft().thePlayer, message.getInventoryGenerator(), message.getKey()));
    }
}
