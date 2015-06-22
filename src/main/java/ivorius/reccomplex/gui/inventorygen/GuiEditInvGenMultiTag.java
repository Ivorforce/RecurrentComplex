/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.inventorygen;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.items.RCItems;
import ivorius.reccomplex.network.PacketSyncItem;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;

/**
 * Created by lukas on 17.01.15.
 */
public class GuiEditInvGenMultiTag extends GuiScreenModalTable
{
    int playerSlot;
    EntityPlayer player;

    TableDataSourceInvGenMultiTag tableDataSource;

    public GuiEditInvGenMultiTag(EntityPlayer player, int playerSlot)
    {
        this.playerSlot = playerSlot;
        this.player = player;

        ItemStack stack = player.inventory.getStackInSlot(playerSlot);

        GuiTable structureGenProperties = new GuiTable(this, tableDataSource = new TableDataSourceInvGenMultiTag(RCItems.inventoryGenerationTag.getGenerationCount(stack)));

        structureGenProperties.setHideScrollbarIfUnnecessary(true);
        setTable(structureGenProperties);
    }

    @Override
    public void initGui()
    {
        if (currentTable() != null)
        {
            currentTable().setPropertiesBounds(Bounds.fromSize(width / 2 - 155, 310, height / 2 - 110, 195));
        }
        super.initGui();

        if (tableStack().size() == 1)
            buttonList.add(new GuiButton(0, width / 2 - 155, height / 2 + 90, 310, 20, "Done"));
        else
            buttonList.add(new GuiButton(2, width / 2 - 155, height / 2 + 90, 310, 20, "Back"));
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode)
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
    protected void actionPerformed(GuiButton button)
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
        RCItems.inventoryGenerationTag.setGenerationCount(stack, tableDataSource.itemCount);

        RecurrentComplex.network.sendToServer(new PacketSyncItem(playerSlot, stack));
    }
}
