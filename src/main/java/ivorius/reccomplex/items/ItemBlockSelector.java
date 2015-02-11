/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.network.PacketItemEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

/**
 * Created by lukas on 11.02.15.
 */
public class ItemBlockSelector extends Item implements ItemEventHandler
{
    @SideOnly(Side.CLIENT)
    public void sendClickToServer(ItemStack usedItem, World world, EntityPlayer player, BlockCoord position)
    {
        ByteBuf buf = Unpooled.buffer();
        BlockCoord.writeCoordToBuffer(position, buf);
        buf.writeBoolean(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL));
        RecurrentComplex.network.sendToServer(new PacketItemEvent(player.inventory.currentItem, buf, "select"));
    }

    @Override
    public void onClientEvent(String context, ByteBuf payload, EntityPlayer sender, ItemStack stack, int itemSlot)
    {
        if ("select".equals(context))
        {
            BlockCoord coord = BlockCoord.readCoordFromBuffer(payload);
            boolean secondary = payload.readBoolean();

            StructureEntityInfo structureEntityInfo = StructureEntityInfo.getStructureEntityInfo(sender);
            if (structureEntityInfo != null)
            {
                if (secondary)
                    structureEntityInfo.selectedPoint2 = coord;
                else
                    structureEntityInfo.selectedPoint1 = coord;

                structureEntityInfo.sendSelectionToClients(sender);
            }
        }
    }
}
