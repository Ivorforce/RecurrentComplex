/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.network;

import ivorius.ivtoolkit.network.SchedulingMessageHandler;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.files.RCFileSuffix;
import ivorius.reccomplex.files.RCFileTypeRegistry;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import ivorius.reccomplex.structures.generic.StructureSaveHandler;
import ivorius.reccomplex.utils.SaveDirectoryData;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Collections;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketSaveStructureHandler extends SchedulingMessageHandler<PacketSaveStructure, IMessage>
{
    public static void saveStructure(GenericStructureInfo structureInfo, String structureID, SaveDirectoryData.Result saveDirectoryDataResult)
    {
        RecurrentComplex.network.sendToServer(new PacketSaveStructure(structureInfo, structureID, saveDirectoryDataResult));
    }

    @Override
    public void processServer(PacketSaveStructure message, MessageContext ctx, WorldServer server)
    {
        NetHandlerPlayServer netHandlerPlayServer = ctx.getServerHandler();
        EntityPlayerMP player = netHandlerPlayServer.playerEntity;

        if (RecurrentComplex.checkPerms(player)) return;

        StructureEntityInfo structureEntityInfo = StructureEntityInfo.getStructureEntityInfo(player);
        GenericStructureInfo genericStructureInfo = message.getStructureInfo();

        if (structureEntityInfo != null)
            genericStructureInfo.worldDataCompound = structureEntityInfo.getCachedExportStructureBlockDataNBT();

        SaveDirectoryData.Result saveDirectoryDataResult = message.getSaveDirectoryDataResult();

        String path = RCFileTypeRegistry.getDirectoryName(saveDirectoryDataResult.saveAsActive) + "/";
        String id = message.getStructureID();

        if (StructureSaveHandler.INSTANCE.save(genericStructureInfo, id, saveDirectoryDataResult.saveAsActive))
        {
            player.addChatMessage(ServerTranslations.format("structure.save.success", path + id));

            if (saveDirectoryDataResult.deleteOther && StructureSaveHandler.INSTANCE.has(id, !saveDirectoryDataResult.saveAsActive))
            {
                String otherPath = RCFileTypeRegistry.getDirectoryName(!saveDirectoryDataResult.saveAsActive) + "/";

                if (StructureSaveHandler.INSTANCE.delete(id, !saveDirectoryDataResult.saveAsActive))
                    player.addChatMessage(ServerTranslations.format("structure.delete.success", otherPath + id));
                else
                    player.addChatMessage(ServerTranslations.format("structure.delete.failure", otherPath + id));
            }

            RecurrentComplex.fileTypeRegistry.reloadCustomFiles(Collections.singletonList(RCFileSuffix.STRUCTURE));
        }
        else
        {
            player.addChatMessage(ServerTranslations.format("structure.save.failure", path + id));
        }
    }
}
