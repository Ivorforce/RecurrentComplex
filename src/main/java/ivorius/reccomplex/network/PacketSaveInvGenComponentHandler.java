/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.network;

import ivorius.ivtoolkit.network.SchedulingMessageHandler;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.files.RCFileSaver;
import ivorius.reccomplex.files.loading.LeveledRegistry;
import ivorius.reccomplex.files.loading.RCFileSuffix;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.item.ItemInventoryGenComponentTag;
import ivorius.reccomplex.utils.SaveDirectoryData;
import ivorius.reccomplex.world.storage.loot.GenericItemCollectionRegistry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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

        String id = message.getKey();

        ResourceDirectory saveDir = saveDirectoryDataResult.directory;
        ResourceDirectory delDir = saveDir.opposite();

        GenericItemCollectionRegistry.INSTANCE.register(id, "", message.getInventoryGenerator(), saveDir.isActive(), saveDir.getLevel());

        if (RCCommands.informSaveResult((message.getInventoryGenerator() != null && id != null) &&
                RecurrentComplex.saver.trySave(saveDir.toPath(), RCFileSuffix.INVENTORY_GENERATION_COMPONENT, id), player, saveDir.subDirectoryName(), RCFileSaver.INVENTORY_GENERATION_COMPONENT, id))
        {
            if (saveDirectoryDataResult.deleteOther)
                RCCommands.informDeleteResult(RecurrentComplex.saver.tryDeleteWithID(delDir.toPath(), id, RCFileSuffix.INVENTORY_GENERATION_COMPONENT), player, RCFileSaver.INVENTORY_GENERATION_COMPONENT, id, delDir.subDirectoryName());

            ItemStack heldItem = playServer.playerEntity.getHeldItem(EnumHand.MAIN_HAND);
            if (heldItem != null && heldItem.getItem() instanceof ItemInventoryGenComponentTag)
                ItemInventoryGenComponentTag.setComponentKey(heldItem, id);
            player.openContainer.detectAndSendChanges();
        }
    }
}
