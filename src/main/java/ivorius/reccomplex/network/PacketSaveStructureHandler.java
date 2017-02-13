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
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.Path;
import java.util.Set;

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

        write(player, genericStructureInfo, id, saveDir, saveDirectoryDataResult.deleteOther, true);
    }

    public static boolean write(ICommandSender sender, GenericStructureInfo structure, String id, ResourceDirectory saveDir, boolean deleteOther, boolean inform)
    {
        StructureRegistry.INSTANCE.register(id, "", structure, saveDir.isActive(), saveDir.getLevel());

        ResourceDirectory delDir = saveDir.opposite();
        boolean saveResult = RecurrentComplex.saver.trySave(saveDir.toPath(), RCFileSaver.STRUCTURE, id);

        if (!inform || RCCommands.informSaveResult(saveResult, sender, saveDir.subDirectoryName(), RCFileSaver.STRUCTURE, id))
        {
            if (deleteOther)
            {
                Pair<Set<Path>, Set<Path>> deleteResult = RecurrentComplex.saver.tryDeleteWithID(delDir.toPath(), RCFileSaver.STRUCTURE, id);

                if (inform)
                    RCCommands.informDeleteResult(deleteResult, sender, RCFileSaver.STRUCTURE, id, delDir.subDirectoryName());
            }
        }

        return saveResult;
    }

}
