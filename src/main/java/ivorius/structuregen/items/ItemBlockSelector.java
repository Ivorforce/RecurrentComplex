/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.items;

import ivorius.structuregen.entities.StructureEntityInfo;
import ivorius.structuregen.ivtoolkit.blocks.BlockCoord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class ItemBlockSelector extends Item
{
    public ItemBlockSelector()
    {
    }

    @Override
    public boolean onItemUse(ItemStack usedItem, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
    {
        if (!world.isRemote)
        {
            StructureEntityInfo structureEntityInfo = StructureEntityInfo.getStructureEntityInfo(player);

            if (structureEntityInfo != null)
            {
                BlockCoord position = new BlockCoord(x, y, z);

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

        return true;
    }
}
