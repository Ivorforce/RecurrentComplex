/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.events;

import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.Random;

/**
 * Created by lukas on 13.10.14.
 */
@Cancelable
public class ItemGenerationEvent extends Event
{
    public final WorldServer server;
    public final IInventory inventory;
    public final Random random;
    public final ItemStack fromStack;
    public final int fromSlot;

    public ItemGenerationEvent(WorldServer server, IInventory inventory, Random random, ItemStack fromStack, int fromSlot)
    {
        this.server = server;
        this.inventory = inventory;
        this.random = random;
        this.fromStack = fromStack;
        this.fromSlot = fromSlot;
    }

    public static class Artifact extends ItemGenerationEvent
    {
        public Artifact(WorldServer server, IInventory inventory, Random random, ItemStack fromStack, int fromSlot)
        {
            super(server, inventory, random, fromStack, fromSlot);
        }
    }

    public static class Book extends ItemGenerationEvent
    {
        public Book(WorldServer server, IInventory inventory, Random random, ItemStack fromStack, int fromSlot)
        {
            super(server, inventory, random, fromStack, fromSlot);
        }
    }
}
