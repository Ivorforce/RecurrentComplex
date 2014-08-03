/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.gui.editstructure.GuiEditGenericStructure;
import ivorius.reccomplex.worldgen.StructureSaveHandler;
import ivorius.reccomplex.worldgen.genericStructures.GenericStructureInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.ChatComponentTranslation;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketEditStructureHandler implements IMessageHandler<PacketEditStructure, IMessage>
{
    @Override
    public IMessage onMessage(PacketEditStructure message, MessageContext ctx)
    {
        if (ctx.side == Side.CLIENT)
        {
            Minecraft.getMinecraft().displayGuiScreen(new GuiEditGenericStructure(message.getKey(), message.getStructureInfo()));
        }
        else
        {
            NetHandlerPlayServer netHandlerPlayServer = ctx.getServerHandler();
            EntityPlayerMP player = netHandlerPlayServer.playerEntity;
            StructureEntityInfo structureEntityInfo = StructureEntityInfo.getStructureEntityInfo(player);

            GenericStructureInfo genericStructureInfo = message.getStructureInfo();

            if (structureEntityInfo != null)
            {
                genericStructureInfo.worldDataCompound = structureEntityInfo.getCachedExportStructureBlockDataNBT();
            }

            if (!StructureSaveHandler.saveGenericStructure(genericStructureInfo, message.getKey()))
            {
                player.addChatMessage(new ChatComponentTranslation("commands.strucExport.failure", message.getKey()));
            }
            else
            {
                player.addChatMessage(new ChatComponentTranslation("commands.strucExport.success", message.getKey()));
                StructureSaveHandler.reloadAllCustomStructures();
            }
        }

        return null;
    }
}
