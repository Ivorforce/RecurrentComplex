/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.network;

import ivorius.ivtoolkit.network.SchedulingMessageHandler;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.files.loading.LeveledRegistry;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import ivorius.reccomplex.structures.generic.StructureSaveHandler;
import ivorius.reccomplex.utils.SaveDirectoryData;
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

        String id = message.getStructureID();

        ResourceDirectory saveDir = saveDirectoryDataResult.directory;
        ResourceDirectory delDir = saveDir.opposite();

        StructureRegistry.INSTANCE.register(id, "", genericStructureInfo, saveDir.isActive(), LeveledRegistry.Level.CUSTOM);

        if (RCCommands.informSaveResult(RecurrentComplex.saver.trySave(saveDir.toPath(), StructureSaveHandler.INSTANCE.suffix, id), player, saveDir.subDirectoryName(), "structure", id))
            if (saveDirectoryDataResult.deleteOther)
                RCCommands.informDeleteResult(RecurrentComplex.loader.tryDelete(delDir.toPath(), id, StructureSaveHandler.INSTANCE.suffix), player, "structure", id, delDir.subDirectoryName());
    }

}
