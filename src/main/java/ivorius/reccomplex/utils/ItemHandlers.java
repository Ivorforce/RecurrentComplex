/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemHandlers
{
    public static boolean has(ICapabilityProvider provider)
    {
        return provider.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)
                || Arrays.stream(EnumFacing.VALUES).anyMatch(f -> provider.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f));
    }

    public static CombinedInvWrapper complete(ICapabilityProvider provider)
    {
        List<IItemHandlerModifiable> handlers = new ArrayList<>();
        Arrays.stream(EnumFacing.VALUES).forEach(f -> {
            if (provider.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f))
            {
                IItemHandler capability = provider.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f);
                if (capability instanceof IItemHandlerModifiable)
                    handlers.add((IItemHandlerModifiable) capability);
            }
        });
        return new CombinedInvWrapper(handlers.toArray(new IItemHandlerModifiable[0]));
    }

    public static boolean hasModifiable(@Nonnull ICapabilityProvider provider, @Nullable EnumFacing side)
    {
        return getModifiable(provider, side) != null;
    }

    public static IItemHandlerModifiable getModifiable(@Nonnull ICapabilityProvider provider, @Nullable EnumFacing side)
    {
        if (!provider.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side))
            return null;
        IItemHandler capability = provider.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
        return (capability instanceof IItemHandlerModifiable) ? (IItemHandlerModifiable) capability : null;
    }

    public static boolean hasModifiable(@Nonnull ICapabilityProvider provider)
    {
        return hasModifiable(provider, null);
    }

    public static IItemHandlerModifiable getModifiable(ICapabilityProvider provider)
    {
        return getModifiable(provider, null);
    }

    public static void clear(IItemHandlerModifiable itemHandler)
    {
        for (int i = 0; i < itemHandler.getSlots() * 5; i++) // Try 5x inventory size, in case it auto-fills back up
            itemHandler.setStackInSlot(Math.max(i, itemHandler.getSlots()), ItemStack.EMPTY);
    }
}
