/*
 * Copyright (c) 2014, Lukas Tenbrink.
 * http://lukas.axxim.net
 *
 * You are free to:
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes, unless you have a permit by the creator.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package ivorius.ivtoolkit.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.inventory.Container;
import net.minecraft.network.NetHandlerPlayServer;

/**
* Created by lukas on 02.07.14.
*/
public class PacketGuiActionHandler implements IMessageHandler<PacketGuiAction, IMessage>
{
    @Override
    public IMessage onMessage(PacketGuiAction message, MessageContext ctx)
    {
        NetHandlerPlayServer netHandler = ctx.getServerHandler();

        Container container = netHandler.playerEntity.openContainer;
        if (container instanceof PacketGuiAction.ActionHandler)
        {
            ((PacketGuiAction.ActionHandler) container).handleAction(message.getContext(), message.getPayload());
        }

        return null;
    }
}
