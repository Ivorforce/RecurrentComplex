/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.files.SimpleLeveledRegistry;
import ivorius.reccomplex.files.loading.RCFileSuffix;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.gui.container.IvGuiHandler;
import ivorius.reccomplex.gui.container.IvGuiRegistry;
import ivorius.reccomplex.gui.inventorygen.GuiEditInventoryGen;
import ivorius.reccomplex.utils.SaveDirectoryData;
import ivorius.reccomplex.world.storage.loot.GenericItemCollectionRegistry;
import ivorius.reccomplex.world.storage.loot.ItemCollectionSaveHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import ivorius.reccomplex.gui.inventorygen.ContainerEditInventoryGenItems;
import ivorius.reccomplex.gui.inventorygen.GuiEditInventoryGenItems;
import ivorius.reccomplex.world.storage.loot.GenericItemCollection.Component;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Created by lukas on 26.05.14.
 */
public class RCGuiHandler implements IvGuiHandler
{
    public static final int editInventoryGen = 0;
    public static final int editInventoryGenItems = 1;

    protected static void openComponentGui(EntityPlayer player, String key, Component component, SaveDirectoryData saveDirectoryData, int guiID)
    {
        if (key == null)
            key = "New Generator";
        if (component == null)
            component = Component.createDefaultComponent();
        if (saveDirectoryData == null)
        {
            SimpleLeveledRegistry<Component>.Status status = GenericItemCollectionRegistry.INSTANCE.status(key);

            saveDirectoryData = SaveDirectoryData.defaultData(key, status != null && status.isActive(),
                    RecurrentComplex.loader.tryFindIDs(ResourceDirectory.ACTIVE.toPath(), RCFileSuffix.INVENTORY_GENERATION_COMPONENT),
                    RecurrentComplex.loader.tryFindIDs(ResourceDirectory.INACTIVE.toPath(), RCFileSuffix.INVENTORY_GENERATION_COMPONENT));
        }

        ByteBuf buf = Unpooled.buffer();

        ByteBufUtils.writeUTF8String(buf, key);
        ItemCollectionSaveHandler.INSTANCE.write(buf, component);
        saveDirectoryData.writeTo(buf);

        IvGuiRegistry.INSTANCE.openGui(player, RecurrentComplex.MOD_ID, guiID, buf);
    }

    public static void editInventoryGenComponent(EntityPlayer player, String key, Component component, SaveDirectoryData saveDirectoryData)
    {
        openComponentGui(player, key, component, saveDirectoryData, RCGuiHandler.editInventoryGen);
    }

    public static void editInventoryGenComponentItems(EntityPlayer player, String key, Component component, SaveDirectoryData saveDirectoryData)
    {
        openComponentGui(player, key, component, saveDirectoryData, editInventoryGenItems);
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
            if (!player.canUseCommand(2, "give"))
                return null; // Potential source of spoof otherwise

            String key = ByteBufUtils.readUTF8String(data);
            Component component = ItemCollectionSaveHandler.INSTANCE.read(data);

            if (component != null)
                return new ContainerEditInventoryGenItems(player, key, component);
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, ByteBuf data)
    {
        if (id == editInventoryGen || id == editInventoryGenItems)
        {
            String key = ByteBufUtils.readUTF8String(data);
            Component component = ItemCollectionSaveHandler.INSTANCE.read(data);
            SaveDirectoryData saveDirectoryData = SaveDirectoryData.readFrom(data);

            if (component != null)
            {
                if (id == editInventoryGen)
                    return new GuiEditInventoryGen(player, component, key, saveDirectoryData);
                else
                    return new GuiEditInventoryGenItems(player, component, key, saveDirectoryData);
            }
        }

        return null;
    }
}
