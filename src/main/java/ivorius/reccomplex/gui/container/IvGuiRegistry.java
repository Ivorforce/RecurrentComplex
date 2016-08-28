/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.container;

import io.netty.buffer.ByteBuf;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.network.PacketOpenGui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lukas on 28.08.16.
 */
public class IvGuiRegistry
{
    public static final IvGuiRegistry INSTANCE = new IvGuiRegistry();

    private final Map<String, IvGuiHandler> handlers = new HashMap<>();

    public void register(String modID, IvGuiHandler handler)
    {
        handlers.put(modID, handler);
    }

    // From FMLNetworkHandler.openGui
    public void openGui(EntityPlayer entityPlayer, String modid, int modGuiId, ByteBuf data)
    {
        IvGuiHandler handler = handlers.get(modid);
        if (handler == null)
            return;

        if (entityPlayer instanceof EntityPlayerMP && !(entityPlayer instanceof FakePlayer))
        {
            EntityPlayerMP entityPlayerMP = (EntityPlayerMP) entityPlayer;
            ByteBuf dataCopy = data.copy();

            Container remoteGuiContainer = handler.getServerGuiElement(modGuiId, entityPlayerMP, data);

            entityPlayerMP.getNextWindowId();
            entityPlayerMP.closeContainer();
            int windowId = entityPlayerMP.currentWindowId;
            RecurrentComplex.network.sendTo(new PacketOpenGui(windowId, modid, modGuiId, dataCopy), entityPlayerMP);

            if (remoteGuiContainer != null) // If null, it's client only
            {
                entityPlayerMP.openContainer = remoteGuiContainer;
                entityPlayerMP.openContainer.windowId = windowId;
                entityPlayerMP.openContainer.addListener(entityPlayerMP);
            }
        }
        else if (entityPlayer instanceof FakePlayer)
        {
            // NO OP - I won't even log a message!
        }
        else if (FMLCommonHandler.instance().getSide().equals(Side.CLIENT))
        {
            RecurrentComplex.network.sendToServer(new PacketOpenGui(0, modid, modGuiId, data));
        }
        else
        {
            FMLLog.fine("Invalid attempt to open a local GUI on a dedicated server. This is likely a bug. GUI ID: %s,%d", modid, modGuiId);
        }
    }

    public void openGuiJustClient(EntityPlayer entityPlayer, String modid, int modGuiId, ByteBuf data)
    {
        IvGuiHandler handler = handlers.get(modid);
        if (handler == null)
            return;

        Object guiContainer = handler.getClientGuiElement(modGuiId, entityPlayer, data);
        FMLCommonHandler.instance().showGuiScreen(guiContainer);
    }
}
