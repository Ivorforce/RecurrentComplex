/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.items;

import ivorius.structuregen.entities.StructureEntityInfo;
import ivorius.structuregen.ivtoolkit.blocks.BlockCoord;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class ItemBlockSelectorFloating extends Item
{
    public float selectionRange;

    public ItemBlockSelectorFloating(float selectionRange)
    {
        this.selectionRange = selectionRange;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack usedItem, World world, EntityPlayer player)
    {
        if (!world.isRemote)
        {
            StructureEntityInfo structureEntityInfo = StructureEntityInfo.getStructureEntityInfo(player);

            if (structureEntityInfo != null)
            {
                BlockCoord position = getHoveredBlock(player, selectionRange);

                boolean second = player.isSneaking();

                if (!second)
                {
                    structureEntityInfo.selectedPoint1 = position;
                }
                else
                {
                    structureEntityInfo.selectedPoint2 = position;
                }

                structureEntityInfo.sendSelectionChangesToClients(player);

                player.addChatMessage(new ChatComponentText((second ? "Second" : "First") + " position set at: " + position.x + ", " + position.y + ", " + position.z));
            }
        }

        return usedItem;
    }

    public static BlockCoord getHoveredBlock(EntityLivingBase par2EntityLiving, float selectionRange)
    {
        Vec3 look = par2EntityLiving.getLookVec();
        int blockX = MathHelper.floor_double(look.xCoord * selectionRange + par2EntityLiving.posX);
        int blockY = MathHelper.floor_double(look.yCoord * selectionRange + par2EntityLiving.posY + par2EntityLiving.getEyeHeight());
        int blockZ = MathHelper.floor_double(look.zCoord * selectionRange + par2EntityLiving.posZ);

        return new BlockCoord(blockX, blockY, blockZ);
    }
}
