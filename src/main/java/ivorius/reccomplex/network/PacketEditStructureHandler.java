/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import ivorius.ivtoolkit.network.SchedulingMessageHandler;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.files.RCFileTypeRegistry;
import ivorius.reccomplex.gui.editstructure.GuiEditGenericStructure;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import ivorius.reccomplex.structures.generic.StructureSaveHandler;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Collections;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketEditStructureHandler extends SchedulingMessageHandler<PacketEditStructure, IMessage>
{
    public static void openEditStructure(GenericStructureInfo structureInfo, String structureID, boolean saveAsActive, EntityPlayerMP player)
    {
        StructureEntityInfo structureEntityInfo = StructureEntityInfo.getStructureEntityInfo(player);

        if (structureEntityInfo != null)
            structureEntityInfo.setCachedExportStructureBlockDataNBT(structureInfo.worldDataCompound);

        RecurrentComplex.network.sendTo(new PacketEditStructure(structureInfo, structureID, saveAsActive,
                StructureSaveHandler.INSTANCE.hasGenericStructure(structureID, true),
                StructureSaveHandler.INSTANCE.hasGenericStructure(structureID, false)), player);
    }

    public static void finishEditStructure(GenericStructureInfo structureInfo, String structureID, boolean saveAsActive, boolean deleteOther)
    {
        RecurrentComplex.network.sendToServer(new PacketEditStructure(structureInfo, structureID, saveAsActive, deleteOther));
    }

    @Override
    public void processServer(PacketEditStructure message, MessageContext ctx, WorldServer server)
    {
        NetHandlerPlayServer netHandlerPlayServer = ctx.getServerHandler();
        EntityPlayerMP player = netHandlerPlayServer.playerEntity;
        StructureEntityInfo structureEntityInfo = StructureEntityInfo.getStructureEntityInfo(player);

        GenericStructureInfo genericStructureInfo = message.getStructureInfo();

        if (structureEntityInfo != null)
            genericStructureInfo.worldDataCompound = structureEntityInfo.getCachedExportStructureBlockDataNBT();

        String path = RCFileTypeRegistry.getStructuresDirectoryName(message.isSaveAsActive()) + "/";
        String structureID = message.getStructureID();

        if (!StructureSaveHandler.INSTANCE.saveGenericStructure(genericStructureInfo, structureID, message.isSaveAsActive()))
        {
            player.addChatMessage(ServerTranslations.format("structure.save.failure", path + structureID));
        }
        else
        {
            player.addChatMessage(ServerTranslations.format("structure.save.success", path + structureID));

            if (message.isDeleteOther() && StructureSaveHandler.INSTANCE.hasGenericStructure(structureID, !message.isSaveAsActive()))
            {
                String otherPath = RCFileTypeRegistry.getStructuresDirectoryName(!message.isSaveAsActive()) + "/";

                if (StructureSaveHandler.INSTANCE.deleteGenericStructure(structureID, !message.isSaveAsActive()))
                    player.addChatMessage(ServerTranslations.format("structure.delete.success", otherPath + structureID));
                else
                    player.addChatMessage(ServerTranslations.format("structure.delete.failure", otherPath + structureID));
            }

            RecurrentComplex.fileTypeRegistry.reloadCustomFiles(Collections.singletonList(StructureSaveHandler.FILE_SUFFIX));
        }
    }

    @Override
    public void processClient(PacketEditStructure message, MessageContext ctx)
    {
        Minecraft.getMinecraft().displayGuiScreen(new GuiEditGenericStructure(message.getStructureID(), message.getStructureInfo(), message.isSaveAsActive(), message.isStructureInActive(), message.isStructureInInactive()));
    }
}
