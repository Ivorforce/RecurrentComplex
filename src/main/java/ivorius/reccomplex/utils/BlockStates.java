/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;

/**
 * Created by lukas on 06.05.16.
 */
public class BlockStates
{
    public static int toMetadata(IBlockState state)
    {
        return state.getBlock().getMetaFromState(state);
    }

    public static IBlockState readBlockState(ByteBuf buf)
    {
        Block block = Block.REGISTRY.getObject(new ResourceLocation(ByteBufUtils.readUTF8String(buf)));
        return block.getStateFromMeta(buf.readInt());
    }

    public static void writeBlockState(ByteBuf buf, IBlockState state)
    {
        ByteBufUtils.writeUTF8String(buf, Block.REGISTRY.getNameForObject(state.getBlock()).toString());
        buf.writeInt(BlockStates.toMetadata(state));
    }
}
