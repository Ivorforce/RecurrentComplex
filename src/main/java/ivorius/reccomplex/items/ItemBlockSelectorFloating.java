/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.items;

import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.util.math.BlockPos;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.network.PacketSyncItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemBlockSelectorFloating extends ItemBlockSelector implements ItemSyncable, ItemInputHandler
{
    public static final float SCROLL_DISTANCE_SPEED = 0.004f;

    public ItemBlockSelectorFloating()
    {

    }

    public static BlockPos getHoveredBlock(EntityLivingBase entity, float selectionRange)
    {
        Vec3d look = entity.getLookVec();
        int blockX = MathHelper.floor_double(look.xCoord * selectionRange + entity.posX);
        int blockY = MathHelper.floor_double(look.yCoord * selectionRange + entity.posY + entity.getEyeHeight());
        int blockZ = MathHelper.floor_double(look.zCoord * selectionRange + entity.posZ);

        return new BlockPos(blockX, blockY, blockZ);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        if (worldIn.isRemote)
        {
            BlockPos position = getHoveredBlock(playerIn, getSelectionRange(itemStackIn));
            sendClickToServer(itemStackIn, worldIn, playerIn, position);
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, itemStackIn);
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
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advanced)
    {
        super.addInformation(stack, player, list, advanced);

        list.add(String.format("Range: %.02f", getSelectionRange(stack)));
        list.add("(Hold ctrl and scroll to modify range)");
        list.add("(Hold ctrl for secondary selection)");
    }

    @Override
    public void writeSyncedNBT(NBTTagCompound compound, ItemStack stack)
    {
        compound.setFloat("selectionRange", getSelectionRange(stack));
    }

    @Override
    public void readSyncedNBT(NBTTagCompound compound, ItemStack stack)
    {
        setSelectionRange(stack, compound.getFloat("selectionRange"));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean onMouseInput(EntityPlayer player, ItemStack stack, int button, boolean buttonState, int dWheel)
    {
        if (modifierKeyDown() && dWheel != 0)
        {
            setSelectionRange(stack, MathHelper.clamp_float(getSelectionRange(stack) + dWheel * SCROLL_DISTANCE_SPEED, 0, 40));
            RecurrentComplex.network.sendToServer(new PacketSyncItem(player.inventory.currentItem, stack));

            return true;
        }

        return false;
    }
}
