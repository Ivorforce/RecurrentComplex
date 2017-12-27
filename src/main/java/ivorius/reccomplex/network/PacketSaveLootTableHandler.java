/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.network;

import ivorius.ivtoolkit.network.SchedulingMessageHandler;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.files.RCFileSaver;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.item.ItemLootTableComponentTag;
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
public class PacketSaveLootTableHandler extends SchedulingMessageHandler<PacketSaveLootTable, IMessage>
{
    @Override
    public void processServer(PacketSaveLootTable message, MessageContext ctx, WorldServer server)
    {
        NetHandlerPlayServer playServer = ctx.getServerHandler();
        EntityPlayerMP player = playServer.player;

        if (RecurrentComplex.checkPerms(player)) return;

        SaveDirectoryData.Result saveDirectoryDataResult = message.getSaveDirectoryDataResult();

        String id = message.getKey();

        ResourceDirectory saveDir = saveDirectoryDataResult.directory;
        ResourceDirectory delDir = saveDir.opposite();

        GenericItemCollectionRegistry.INSTANCE.register(id, "", message.getComponent(), saveDir.isActive(), saveDir.getLevel());

        if (RCCommands.informSaveResult((message.getComponent() != null && id != null) &&
                RecurrentComplex.saver.trySave(saveDir.toPath(), RCFileSaver.INVENTORY_GENERATION_COMPONENT, id), player, saveDir, RCFileSaver.INVENTORY_GENERATION_COMPONENT, id))
        {
            if (saveDirectoryDataResult.deleteOther)
                RCCommands.informDeleteResult(RecurrentComplex.saver.tryDeleteWithID(delDir.toPath(), RCFileSaver.INVENTORY_GENERATION_COMPONENT, id), player, RCFileSaver.INVENTORY_GENERATION_COMPONENT, id, delDir);

            ItemStack heldItem = playServer.player.getHeldItem(EnumHand.MAIN_HAND);
            if (heldItem != null && heldItem.getItem() instanceof ItemLootTableComponentTag)
                ItemLootTableComponentTag.setComponentKey(heldItem, id);
            player.openContainer.detectAndSendChanges();
        }
    }
}
