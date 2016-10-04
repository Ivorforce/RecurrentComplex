/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Created by lukas on 11.02.15.
 */
public interface ItemInputHandler
{
    boolean onMouseInput(EntityPlayer player, ItemStack stack, int button, boolean buttonState, int dWheel);
}
