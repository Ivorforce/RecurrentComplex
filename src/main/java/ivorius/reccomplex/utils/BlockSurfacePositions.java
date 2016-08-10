/*
 * Copyright 2015 Lukas Tenbrink
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package ivorius.reccomplex.utils;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collection;

/**
 * Created by lukas on 21.07.15.
 */
public final class BlockSurfacePositions
{
    private BlockSurfacePositions()
    {
    }

    public static BlockSurfacePos fromIntArray(int[] array)
    {
        if (array.length != 3)
            throw new IllegalArgumentException();

        return new BlockSurfacePos(array[0], array[2]);
    }

    public static int[] toIntArray(BlockSurfacePos pos)
    {
        return new int[]{pos.getX(), pos.getZ()};
    }

    public static BlockSurfacePos readWithBase(NBTTagCompound compound, String keyBase)
    {
        return new BlockSurfacePos(compound.getInteger(keyBase + "_x"), compound.getInteger(keyBase + "_z"));
    }

    public static void writeToNBT(String keyBase, BlockSurfacePos coord, NBTTagCompound compound)
    {
        if (coord != null)
        {
            compound.setInteger(keyBase + "_x", coord.getX());
            compound.setInteger(keyBase + "_z", coord.getZ());
        }
    }

    public static BlockSurfacePos readFromNBT(String keyBase, NBTTagCompound compound)
    {
        return compound.hasKey(keyBase + "_x") && compound.hasKey(keyBase + "_y") && compound.hasKey(keyBase + "_z")
                ? new BlockSurfacePos(compound.getInteger(keyBase + "_x"), compound.getInteger(keyBase + "_z"))
                : null;

    }

    public static void maybeWriteToBuffer(BlockSurfacePos coord, ByteBuf buffer)
    {
        buffer.writeBoolean(coord != null);

        if (coord != null)
            writeToBuffer(coord, buffer);
    }

    public static BlockSurfacePos maybeReadFromBuffer(ByteBuf buffer)
    {
        return buffer.readBoolean() ? readFromBuffer(buffer) : null;
    }

    public static void writeToBuffer(BlockSurfacePos coord, ByteBuf buffer)
    {
        buffer.writeInt(coord.getX());
        buffer.writeInt(coord.getZ());
    }

    public static BlockSurfacePos readFromBuffer(ByteBuf buffer)
    {
        return new BlockSurfacePos(buffer.readInt(), buffer.readInt());
    }

    public static BlockSurfacePos getLowerCorner(Collection<BlockSurfacePos> positions)
    {
        int x = 0, z = 0;
        boolean first = true;

        for (BlockSurfacePos position : positions)
        {
            if (first)
            {
                x = position.getX();
                z = position.getZ();
                first = false;
            }

            x = Math.min(x, position.getX());
            z = Math.min(z, position.getZ());
        }

        if (first)
            throw new ArrayIndexOutOfBoundsException();

        return new BlockSurfacePos(x, z);
    }

    public static BlockSurfacePos getHigherCorner(Collection<BlockSurfacePos> positions)
    {
        int x = 0, z = 0;
        boolean first = true;

        for (BlockSurfacePos position : positions)
        {
            if (first)
            {
                x = position.getX();
                z = position.getZ();
                first = false;
            }

            x = Math.max(x, position.getX());
            z = Math.max(z, position.getZ());
        }

        if (first)
            throw new ArrayIndexOutOfBoundsException();

        return new BlockSurfacePos(x, z);
    }

    public static BlockSurfacePos getLowerCorner(BlockSurfacePos one, BlockSurfacePos two)
    {
        return new BlockSurfacePos(Math.min(one.getX(), two.getX()), Math.min(one.getZ(), two.getZ()));
    }

    public static BlockSurfacePos getHigherCorner(BlockSurfacePos one, BlockSurfacePos two)
    {
        return new BlockSurfacePos(Math.max(one.getX(), two.getX()), Math.max(one.getZ(), two.getZ()));
    }

    public static BlockSurfacePos invert(BlockSurfacePos pos)
    {
        return new BlockSurfacePos(-pos.getX(), -pos.getZ());
    }

    public static BlockSurfacePos sub(BlockSurfacePos pos, BlockSurfacePos sub)
    {
        return new BlockSurfacePos(pos.getX() - sub.getX(), pos.getZ() - sub.getZ());
    }
}