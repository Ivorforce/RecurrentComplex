/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui;

import cpw.mods.fml.common.network.IGuiHandler;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.editinventorygen.ContainerEditInventoryGen;
import ivorius.reccomplex.gui.editinventorygen.GuiEditInventoryGen;
import ivorius.reccomplex.items.InventoryGeneratorHolder;
import ivorius.reccomplex.worldgen.inventory.GenericInventoryGenerator;
import ivorius.reccomplex.worldgen.inventory.InventoryGenerationHandler;
import ivorius.reccomplex.worldgen.inventory.InventoryGenerator;
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

            if (refStack.getItem() instanceof InventoryGeneratorHolder)
            {
                String key = ((InventoryGeneratorHolder) refStack.getItem()).inventoryKey(refStack);

                if (key == null)
                {
                    return new ContainerEditInventoryGen(player, GenericInventoryGenerator.createDefaultGenerator());
                }
                else
                {
                    InventoryGenerator generator = InventoryGenerationHandler.generator(key);
                    GenericInventoryGenerator genericInventoryGenerator  = generator.copyAsGenericInventoryGenerator();

                    if (genericInventoryGenerator == null)
                    {
                        RecurrentComplex.logger.error("editInventoryGen should not be called with a non-generic inventory gen!");
                        return null;
                    }

                    return new ContainerEditInventoryGen(player, genericInventoryGenerator);
                }
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

            if (refStack.getItem() instanceof InventoryGeneratorHolder)
            {
                String key = ((InventoryGeneratorHolder) refStack.getItem()).inventoryKey(refStack);

                if (key == null)
                {
                    return new GuiEditInventoryGen(player, GenericInventoryGenerator.createDefaultGenerator(), "NewGenerator");
                }
                else
                {
                    InventoryGenerator generator = InventoryGenerationHandler.generator(key);
                    GenericInventoryGenerator genericInventoryGenerator  = generator.copyAsGenericInventoryGenerator();

                    if (genericInventoryGenerator == null)
                    {
                        RecurrentComplex.logger.error("editInventoryGen should not be called with a non-generic inventory gen!");
                        return null;
                    }

                    return new GuiEditInventoryGen(player, genericInventoryGenerator, key);
                }
            }
        }

        return null;
    }
}
