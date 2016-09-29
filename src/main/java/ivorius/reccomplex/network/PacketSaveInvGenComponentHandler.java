/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.network;

import ivorius.ivtoolkit.network.SchedulingMessageHandler;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.files.RCFileSuffix;
import ivorius.reccomplex.files.RCFileTypeRegistry;
import ivorius.reccomplex.items.ItemInventoryGenComponentTag;
import ivorius.reccomplex.utils.SaveDirectoryData;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.worldgen.inventory.ItemCollectionSaveHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Collections;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketSaveInvGenComponentHandler extends SchedulingMessageHandler<PacketSaveInvGenComponent, IMessage>
{
    @Override
    public void processServer(PacketSaveInvGenComponent message, MessageContext ctx, WorldServer server)
    {
        NetHandlerPlayServer playServer = ctx.getServerHandler();
        EntityPlayerMP player = playServer.playerEntity;

        if (RecurrentComplex.checkPerms(player)) return;

        SaveDirectoryData.Result saveDirectoryDataResult = message.getSaveDirectoryDataResult();

        String path = RCFileTypeRegistry.getDirectoryName(saveDirectoryDataResult.saveAsActive) + "/";
        String id = message.getKey();

        if ((message.getInventoryGenerator() != null && id != null) &&
                ItemCollectionSaveHandler.INSTANCE.save(message.getInventoryGenerator(), id, saveDirectoryDataResult.saveAsActive))
        {
            player.addChatMessage(ServerTranslations.format("inventorygen.save.success", path + id));

            if (saveDirectoryDataResult.deleteOther && ItemCollectionSaveHandler.INSTANCE.has(id, !saveDirectoryDataResult.saveAsActive))
            {
                String otherPath = RCFileTypeRegistry.getDirectoryName(!saveDirectoryDataResult.saveAsActive) + "/";

                if (ItemCollectionSaveHandler.INSTANCE.delete(id, !saveDirectoryDataResult.saveAsActive))
                    player.addChatMessage(ServerTranslations.format("inventorygen.delete.success", otherPath + id));
                else
                    player.addChatMessage(ServerTranslations.format("inventorygen.delete.failure", otherPath + id));
            }

            RecurrentComplex.fileTypeRegistry.loadCustomFiles(Collections.singletonList(RCFileSuffix.INVENTORY_GENERATION_COMPONENT));

            ItemStack heldItem = playServer.playerEntity.getHeldItem(EnumHand.MAIN_HAND);
            if (heldItem != null && heldItem.getItem() instanceof ItemInventoryGenComponentTag)
                ItemInventoryGenComponentTag.setComponentKey(heldItem, id);
            player.openContainer.detectAndSendChanges();
        }
        else
        {
            player.addChatMessage(ServerTranslations.format("inventorygen.save.failure", path + id));
        }
    }
}
