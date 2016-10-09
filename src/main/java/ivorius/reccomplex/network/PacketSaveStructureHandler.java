/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.network;

import ivorius.ivtoolkit.network.SchedulingMessageHandler;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.capability.StructureEntityInfo;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.files.RCFileSaver;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.utils.SaveDirectoryData;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructureInfo;
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

        StructureEntityInfo structureEntityInfo = StructureEntityInfo.get(player, null);
        GenericStructureInfo genericStructureInfo = message.getStructureInfo();

        if (structureEntityInfo != null)
            genericStructureInfo.worldDataCompound = structureEntityInfo.getCachedExportStructureBlockDataNBT();

        SaveDirectoryData.Result saveDirectoryDataResult = message.getSaveDirectoryDataResult();

        String id = message.getStructureID();

        ResourceDirectory saveDir = saveDirectoryDataResult.directory;
        ResourceDirectory delDir = saveDir.opposite();

        StructureRegistry.INSTANCE.register(id, "", genericStructureInfo, saveDir.isActive(), saveDir.getLevel());

        if (RCCommands.informSaveResult(RecurrentComplex.saver.trySave(saveDir.toPath(), RCFileSaver.STRUCTURE, id), player, saveDir.subDirectoryName(), RCFileSaver.STRUCTURE, id))
            if (saveDirectoryDataResult.deleteOther)
                RCCommands.informDeleteResult(RecurrentComplex.saver.tryDeleteWithID(delDir.toPath(), RCFileSaver.STRUCTURE, id), player, RCFileSaver.STRUCTURE, id, delDir.subDirectoryName());
    }

}
