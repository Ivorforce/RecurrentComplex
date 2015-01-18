/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui;

import cpw.mods.fml.common.network.IGuiHandler;
import ivorius.reccomplex.gui.editinventorygen.ContainerEditInventoryGen;
import ivorius.reccomplex.gui.editinventorygen.GuiEditInventoryGen;
import ivorius.reccomplex.items.ItemInventoryGenComponentTag;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollection.Component;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollectionRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * Created by lukas on 26.05.14.
 */
public class RCGuiHandler implements IGuiHandler
{
    public static final int editInventoryGen = 0;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        if (ID == editInventoryGen)
        {
            ItemStack refStack = player.inventory.getStackInSlot(x);
            String key = ItemInventoryGenComponentTag.componentKey(refStack);

            if (key == null)
            {
                return new ContainerEditInventoryGen(player, Component.createDefaultComponent());
            }
            else
            {
                Component generator = GenericItemCollectionRegistry.component(key);

                if (generator != null)
                    return new ContainerEditInventoryGen(player, generator.copy());
            }
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        if (ID == editInventoryGen)
        {
            ItemStack refStack = player.inventory.getStackInSlot(x);
            String key = ItemInventoryGenComponentTag.componentKey(refStack);

            if (key == null)
            {
                return new GuiEditInventoryGen(player, Component.createDefaultComponent(), "NewGenerator");
            }
            else
            {
                Component generator = GenericItemCollectionRegistry.component(key);

                if (generator != null)
                    return new GuiEditInventoryGen(player, generator, key);
            }
        }

        return null;
    }
}
