/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.items;

import com.google.common.collect.Iterables;
import ivorius.ivtoolkit.blocks.BlockPositions;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.util.BlockPos;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.network.PacketItemEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;

/**
 * Created by lukas on 11.02.15.
 */
public class ItemBlockSelector extends Item implements ItemEventHandler
{
    @SideOnly(Side.CLIENT)
    public void sendClickToServer(ItemStack usedItem, World world, EntityPlayer player, BlockPos position)
    {
        ByteBuf buf = Unpooled.buffer();

        BlockPositions.maybeWriteToBuffer(position, buf);
        buf.writeBoolean(modifierKeyDown());
        RecurrentComplex.network.sendToServer(new PacketItemEvent(player.inventory.currentItem, buf, "select"));
    }

    @SideOnly(Side.CLIENT)
    public boolean modifierKeyDown()
    {
        boolean modifier = false;
        for (int k : RCConfig.blockSelectorModifierKeys)
            modifier |= Keyboard.isKeyDown(k);
        return modifier;
    }

    @Override
    public void onClientEvent(String context, ByteBuf payload, EntityPlayer sender, ItemStack stack, int itemSlot)
    {
        if ("select".equals(context))
        {
            BlockPos coord = BlockPositions.maybeReadFromBuffer(payload);
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
