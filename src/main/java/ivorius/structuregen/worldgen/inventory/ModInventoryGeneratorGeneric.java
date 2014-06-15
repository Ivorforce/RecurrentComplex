/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.worldgen.inventory;

import cpw.mods.fml.common.Loader;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 07.06.14.
 */
public class ModInventoryGeneratorGeneric extends ModInventoryGenerator
{
    private GenericInventoryGenerator genericInventoryGenerator;

    public ModInventoryGeneratorGeneric(String modName, GenericInventoryGenerator genericInventoryGenerator)
    {
        super(modName);
        this.genericInventoryGenerator = genericInventoryGenerator;
    }

    @Override
    public void generateInInventory(Random random, IInventory inventory)
    {
        genericInventoryGenerator.generateInInventory(random, inventory);
    }

    @Override
    public void generateInInventorySlot(Random random, IInventory inventory, int slot)
    {
        genericInventoryGenerator.generateInInventorySlot(random, inventory, slot);
    }

    @Override
    public GenericInventoryGenerator copyAsGenericInventoryGenerator()
    {
        return (GenericInventoryGenerator) genericInventoryGenerator.clone();
    }

    @Override
    public boolean areDependenciesResolved()
    {
        return genericInventoryGenerator.areDependenciesResolved();
    }
}
