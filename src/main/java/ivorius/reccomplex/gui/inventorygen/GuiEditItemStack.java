/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.inventorygen;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.gui.table.screen.GuiScreenEditTable;
import ivorius.reccomplex.network.PacketSyncItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Created by lukas on 17.01.15.
 */
public class GuiEditItemStack<T extends TableDataSourceItem> extends GuiScreenEditTable<T>
{
    protected int playerSlot;
    protected EntityPlayer player;

    public GuiEditItemStack(EntityPlayer player, int playerSlot, T dataSource)
    {
        this.playerSlot = playerSlot;
        this.player = player;

        dataSource.setStack(player.inventory.getStackInSlot(playerSlot).copy());
        GuiTable table = setDataSource(dataSource, this::saveAndSend);
        table.setHideScrollbarIfUnnecessary(true);
    }

    public void saveAndSend(T t)
    {
        ItemStack stack = t.stack.copy();

        player.inventory.setInventorySlotContents(playerSlot, stack);

        RecurrentComplex.network.sendToServer(new PacketSyncItem(playerSlot, stack));
    }
}
