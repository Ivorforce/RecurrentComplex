/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.inventorygen;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.table.Bounds;
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
public class GuiEditItemStack<T extends TableDataSourceItem> extends GuiScreenModalTable
{
    protected int playerSlot;
    protected EntityPlayer player;

    protected T tableDataSource;

    public GuiEditItemStack(EntityPlayer player, int playerSlot, T dataSource)
    {
        this.playerSlot = playerSlot;
        this.player = player;

        dataSource.setStack(player.inventory.getStackInSlot(playerSlot));
        GuiTable structureGenProperties = new GuiTable(this, tableDataSource = dataSource);

        structureGenProperties.setHideScrollbarIfUnnecessary(true);
        setTable(structureGenProperties);
    }

    @Override
    public void initGui()
    {
        if (currentTable() != null)
        {
            currentTable().setPropertiesBounds(Bounds.fromAxes(width / 2 - 155, 310, height / 2 - 110, 195));
        }
        super.initGui();

        if (tableStack().size() == 1)
            buttonList.add(new GuiButton(0, width / 2 - 155, height / 2 + 90, 310, 20, IvTranslations.get("gui.done")));
        else
            buttonList.add(new GuiButton(2, width / 2 - 155, height / 2 + 90, 310, 20, IvTranslations.get("gui.back")));
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) throws IOException
    {
        if (keyCode == Keyboard.KEY_ESCAPE)
        {
            saveAndSend();
            this.mc.thePlayer.closeScreen();
        }
        else
        {
            super.keyTyped(keyChar, keyCode);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);

        if (button.id == 0)
        {
            saveAndSend();
            this.mc.thePlayer.closeScreen();
        }
        else if (button.id == 1)
        {
            this.mc.thePlayer.closeScreen();
        }
        else if (button.id == 2)
        {
            popTable();
        }
    }

    private void saveAndSend()
    {
        ItemStack stack = player.inventory.getStackInSlot(playerSlot);

        if (stack == null)
        {
            stack = tableDataSource.stack;
            player.inventory.setInventorySlotContents(playerSlot, stack);
        }
        else
            stack.readFromNBT(tableDataSource.stack.writeToNBT(new NBTTagCompound()));

        RecurrentComplex.network.sendToServer(new PacketSyncItem(playerSlot, stack));
    }

}
