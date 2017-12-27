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
import ivorius.reccomplex.gui.loot.ContainerEditLootTableItems;
import ivorius.reccomplex.gui.loot.GuiEditLootTable;
import ivorius.reccomplex.gui.loot.GuiEditLootTableItems;
import ivorius.reccomplex.utils.SaveDirectoryData;
import ivorius.reccomplex.world.storage.loot.GenericLootTable.Component;
import ivorius.reccomplex.world.storage.loot.GenericItemCollectionRegistry;
import ivorius.reccomplex.world.storage.loot.ItemCollectionSaveHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.network.ByteBufUtils;

/**
 * Created by lukas on 26.05.14.
 */

public class RCGuiHandler implements IvGuiHandler
{
    public static final int editLootTable = 0;
    public static final int editLootTableItems = 1;

    protected static void openComponentGui(EntityPlayer player, String key, Component component, SaveDirectoryData saveDirectoryData, int guiID)
    {
        if (key == null)
            key = "New Generator";
        if (component == null)
            component = Component.createDefaultComponent();
        if (saveDirectoryData == null)
        {
            SimpleLeveledRegistry<Component>.Status status = GenericItemCollectionRegistry.INSTANCE.status(key);

            saveDirectoryData = SaveDirectoryData.defaultData(key, status != null ? ResourceDirectory.custom(status.isActive()) : null,
                    RecurrentComplex.loader.tryFindIDs(ResourceDirectory.ACTIVE.toPath(), RCFileSuffix.INVENTORY_GENERATION_COMPONENT),
                    RecurrentComplex.loader.tryFindIDs(ResourceDirectory.INACTIVE.toPath(), RCFileSuffix.INVENTORY_GENERATION_COMPONENT));
        }

        ByteBuf buf = Unpooled.buffer();

        ByteBufUtils.writeUTF8String(buf, key);
        ItemCollectionSaveHandler.INSTANCE.write(buf, component);
        saveDirectoryData.writeTo(buf);

        IvGuiRegistry.INSTANCE.openGui(player, RecurrentComplex.MOD_ID, guiID, buf);
    }

    public static void editLootTableComponent(EntityPlayer player, String key, Component component, SaveDirectoryData saveDirectoryData)
    {
        openComponentGui(player, key, component, saveDirectoryData, RCGuiHandler.editLootTable);
    }

    public static void editLootTableComponentItems(EntityPlayer player, String key, Component component, SaveDirectoryData saveDirectoryData)
    {
        openComponentGui(player, key, component, saveDirectoryData, editLootTableItems);
    }

    @Override
    public Container getServerGuiElement(int id, EntityPlayerMP player, ByteBuf data)
    {
        if (id == editLootTable)
        {
            return null;
        }
        else if (id == editLootTableItems)
        {
            if (!player.canUseCommand(2, "give"))
                return null; // Potential source of spoof otherwise

            String key = ByteBufUtils.readUTF8String(data);
            Component component = ItemCollectionSaveHandler.INSTANCE.read(data);

            if (component != null)
                return new ContainerEditLootTableItems(player, key, component);
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, ByteBuf data)
    {
        if (id == editLootTable || id == editLootTableItems)
        {
            String key = ByteBufUtils.readUTF8String(data);
            Component component = ItemCollectionSaveHandler.INSTANCE.read(data);
            SaveDirectoryData saveDirectoryData = SaveDirectoryData.readFrom(data);

            if (component != null)
            {
                if (id == editLootTable)
                    return new GuiEditLootTable(player, component, key, saveDirectoryData);
                else
                    return new GuiEditLootTableItems(player, component, key, saveDirectoryData);
            }
        }

        return null;
    }
}
