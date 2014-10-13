/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.events;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.Random;

/**
 * Created by lukas on 13.10.14.
 */
@Cancelable
public class ItemGenerationEvent extends Event
{
    public final IInventory inventory;
    public final Random random;
    public final ItemStack fromStack;
    public final int fromSlot;

    public ItemGenerationEvent(IInventory inventory, Random random, ItemStack fromStack, int fromSlot)
    {
        this.inventory = inventory;
        this.random = random;
        this.fromStack = fromStack;
        this.fromSlot = fromSlot;
    }

    public static class Artifact extends ItemGenerationEvent
    {
        public Artifact(IInventory inventory, Random random, ItemStack fromStack, int fromSlot)
        {
            super(inventory, random, fromStack, fromSlot);
        }
    }

    public static class Book extends ItemGenerationEvent
    {
        public Book(IInventory inventory, Random random, ItemStack fromStack, int fromSlot)
        {
            super(inventory, random, fromStack, fromSlot);
        }
    }
}
