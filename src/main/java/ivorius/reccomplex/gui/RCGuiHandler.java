/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.container.IvGuiHandler;
import ivorius.reccomplex.gui.container.IvGuiRegistry;
import ivorius.reccomplex.gui.inventorygen.GuiEditInventoryGen;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollectionRegistry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import ivorius.reccomplex.gui.inventorygen.ContainerEditInventoryGenItems;
import ivorius.reccomplex.gui.inventorygen.GuiEditInventoryGenItems;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollection.Component;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Created by lukas on 26.05.14.
 */
public class RCGuiHandler implements IvGuiHandler
{
    public static final int editInventoryGen = 0;
    public static final int editInventoryGenItems = 1;

    protected static void openComponentGui(EntityPlayer player, String key, Component component, int editInventoryGen)
    {
        if (key == null)
            key = "New Generator";
        if (component == null)
            component = Component.createDefaultComponent();

        ByteBuf buf = Unpooled.buffer();
        ByteBufUtils.writeUTF8String(buf, key);
        GenericItemCollectionRegistry.INSTANCE.writeComponent(buf, component);
        IvGuiRegistry.INSTANCE.openGui(player, RecurrentComplex.MOD_ID, editInventoryGen, buf);
    }

    public static void editInventoryGenComponent(EntityPlayer player, String key, Component component)
    {
        openComponentGui(player, key, component, RCGuiHandler.editInventoryGen);
    }

    public static void editInventoryGenComponentItems(EntityPlayer player, String key, Component component)
    {
        openComponentGui(player, key, component, editInventoryGenItems);
    }

    @Override
    public Container getServerGuiElement(int id, EntityPlayerMP player, ByteBuf data)
    {
        if (id == editInventoryGen)
        {
            return null;
        }
        else if (id == editInventoryGenItems)
        {
            if (!player.canCommandSenderUseCommand(2, "give"))
                return null; // Potential source of spoof otherwise

            String key = ByteBufUtils.readUTF8String(data);
            Component component = GenericItemCollectionRegistry.INSTANCE.readComponent(data);

            if (component != null)
                return new ContainerEditInventoryGenItems(player, key, component);
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, ByteBuf data)
    {
        if (id == editInventoryGen)
        {
            String key = ByteBufUtils.readUTF8String(data);
            Component component = GenericItemCollectionRegistry.INSTANCE.readComponent(data);

            if (component != null)
                return new GuiEditInventoryGen(player, component, key);
        }
        else if (id == editInventoryGenItems)
        {
            String key = ByteBufUtils.readUTF8String(data);
            Component component = GenericItemCollectionRegistry.INSTANCE.readComponent(data);

            if (component != null)
                return new GuiEditInventoryGenItems(player, component, key);
        }

        return null;
    }
}
