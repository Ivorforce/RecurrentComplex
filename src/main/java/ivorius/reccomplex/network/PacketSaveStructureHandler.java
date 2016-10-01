/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.network;

import ivorius.ivtoolkit.network.SchedulingMessageHandler;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.files.LeveledRegistry;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import ivorius.reccomplex.structures.generic.StructureSaveHandler;
import ivorius.reccomplex.utils.SaveDirectoryData;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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

        String path = saveDirectoryDataResult.directory.directoryName() + "/";
        String id = message.getStructureID();

        StructureRegistry.INSTANCE.register(id, "", genericStructureInfo, saveDirectoryDataResult.directory.isActive(), LeveledRegistry.Level.CUSTOM);

        if (RecurrentComplex.fileTypeRegistry.tryWrite(saveDirectoryDataResult.directory, StructureSaveHandler.INSTANCE.getSuffix(), id))
        {
            player.addChatMessage(ServerTranslations.format("structure.save.success", path + id));

            if (saveDirectoryDataResult.deleteOther)
            {
                if (RecurrentComplex.fileTypeRegistry.tryDelete(saveDirectoryDataResult.directory.opposite(), id, StructureSaveHandler.INSTANCE.getSuffix()).size() > 0)
                    player.addChatMessage(ServerTranslations.format("structure.delete.failure", id));
                else
                    player.addChatMessage(ServerTranslations.format("structure.delete.success", id));
            }
        }
        else
        {
            player.addChatMessage(ServerTranslations.format("structure.save.failure", path + id));
        }
    }
}
