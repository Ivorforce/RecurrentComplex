/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.inventorygen;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.table.Bounds;
import ivorius.reccomplex.gui.table.GuiScreenEditTable;
import ivorius.reccomplex.gui.table.GuiScreenModalTable;
import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.network.PacketSyncItem;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

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
        ItemStack stack = player.inventory.getStackInSlot(playerSlot);

        if (stack == null)
        {
            stack = t.stack;
            player.inventory.setInventorySlotContents(playerSlot, stack);
        }
        else
            stack.readFromNBT(t.stack.writeToNBT(new NBTTagCompound()));

        RecurrentComplex.network.sendToServer(new PacketSyncItem(playerSlot, stack));
    }
}
