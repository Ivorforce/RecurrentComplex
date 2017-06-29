/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.item;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.network.PacketSyncItem;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class ItemBlockSelectorFloating extends ItemBlockSelector implements ItemSyncableTags, ItemInputHandler
{
    public static final float SCROLL_DISTANCE_SPEED = 0.004f;

    public ItemBlockSelectorFloating()
    {

    }

    public static BlockPos getHoveredBlock(EntityLivingBase entity, float selectionRange)
    {
        Vec3d look = entity.getLookVec();
        int blockX = MathHelper.floor(look.x * selectionRange + entity.posX);
        int blockY = MathHelper.floor(look.y * selectionRange + entity.posY + entity.getEyeHeight());
        int blockZ = MathHelper.floor(look.z * selectionRange + entity.posZ);

        return new BlockPos(blockX, blockY, blockZ);
    }

    @Nullable
    @Override
    public BlockPos hoveredBlock(ItemStack stack, EntityLivingBase entity)
    {
        return getHoveredBlock(entity, getSelectionRange(stack));
    }

    public float getSelectionRange(ItemStack stack)
    {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey("selectionRange", Constants.NBT.TAG_FLOAT)
                ? stack.getTagCompound().getFloat("selectionRange")
                : 2.0f;
    }

    public void setSelectionRange(ItemStack stack, float range)
    {
        stack.setTagInfo("selectionRange", new NBTTagFloat(range));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        tooltip.add(String.format("Range: %.02f", getSelectionRange(stack)));
        tooltip.add("(Hold ctrl and scroll to modify range)");
        tooltip.add("(Hold ctrl for secondary selection)");
    }

    @Override
    public List<Pair<String, Integer>> getSyncedNBTTags()
    {
        return Collections.singletonList(Pair.of("selectionRange", Constants.NBT.TAG_FLOAT));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean onMouseInput(EntityPlayer player, ItemStack stack, int button, boolean buttonState, int dWheel)
    {
        if (modifierKeyDown() && dWheel != 0)
        {
            setSelectionRange(stack, MathHelper.clamp(getSelectionRange(stack) + dWheel * SCROLL_DISTANCE_SPEED, 0, 40));
            RecurrentComplex.network.sendToServer(new PacketSyncItem(player.inventory.currentItem, stack));

            return true;
        }

        return false;
    }
}
