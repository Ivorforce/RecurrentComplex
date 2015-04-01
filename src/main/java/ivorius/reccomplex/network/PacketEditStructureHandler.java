/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.gui.editstructure.GuiEditGenericStructure;
import ivorius.reccomplex.structures.generic.StructureSaveHandler;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.ChatComponentTranslation;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketEditStructureHandler implements IMessageHandler<PacketEditStructure, IMessage>
{
    public static void sendEditStructure(GenericStructureInfo genericStructureInfo, String key, EntityPlayerMP player)
    {
        StructureEntityInfo structureEntityInfo = StructureEntityInfo.getStructureEntityInfo(player);

        if (structureEntityInfo != null)
            structureEntityInfo.setCachedExportStructureBlockDataNBT(genericStructureInfo.worldDataCompound);

        RecurrentComplex.network.sendTo(new PacketEditStructure(key, genericStructureInfo), player);
    }

    @Override
    public IMessage onMessage(PacketEditStructure message, MessageContext ctx)
    {
        if (ctx.side == Side.CLIENT)
        {
            onMessageClient(message, ctx);
        }
        else
        {
            NetHandlerPlayServer netHandlerPlayServer = ctx.getServerHandler();
            EntityPlayerMP player = netHandlerPlayServer.playerEntity;
            StructureEntityInfo structureEntityInfo = StructureEntityInfo.getStructureEntityInfo(player);

            GenericStructureInfo genericStructureInfo = message.getStructureInfo();

            if (structureEntityInfo != null)
                genericStructureInfo.worldDataCompound = structureEntityInfo.getCachedExportStructureBlockDataNBT();

            if (!StructureSaveHandler.saveGenericStructure(genericStructureInfo, message.getKey()))
            {
                player.addChatMessage(ServerTranslations.format("commands.strucExport.failure", message.getKey()));
            }
            else
            {
                player.addChatMessage(ServerTranslations.format("commands.strucExport.success", message.getKey()));
                StructureSaveHandler.reloadAllCustomStructures();
            }
        }

        return null;
    }

    @SideOnly(Side.CLIENT)
    private void onMessageClient(PacketEditStructure message, MessageContext ctx)
    {
        Minecraft.getMinecraft().displayGuiScreen(new GuiEditGenericStructure(message.getKey(), message.getStructureInfo()));
    }
}
