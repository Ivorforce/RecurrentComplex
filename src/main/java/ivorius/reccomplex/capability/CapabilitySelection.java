/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.capability;

import io.netty.buffer.ByteBuf;
import ivorius.ivtoolkit.blocks.BlockPositions;
import ivorius.ivtoolkit.network.IvNetworkHelperServer;
import ivorius.ivtoolkit.network.PartialUpdateHandler;
import ivorius.ivtoolkit.tools.NBTCompoundObject;
import ivorius.reccomplex.RecurrentComplex;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;

/**
 * Created by lukas on 24.05.14.
 */
public class CapabilitySelection implements NBTCompoundObject, PartialUpdateHandler, SelectionOwner
{
    public static final String CAPABILITY_KEY = "rc_selection";

    @CapabilityInject(CapabilitySelection.class)
    public static Capability<CapabilitySelection> CAPABILITY;

    private boolean hasChanges;

    public BlockPos selectedPoint1;
    public BlockPos selectedPoint2;

    @Nullable
    public static CapabilitySelection get(ICapabilityProvider provider, @Nullable EnumFacing facing)
    {
        return provider.getCapability(CAPABILITY, null);
    }

    @Nullable
    @Override
    public BlockPos getSelectedPoint1()
    {
        return selectedPoint1;
    }

    @Override
    public void setSelectedPoint1(@Nullable BlockPos pos)
    {
        selectedPoint1 = pos;
        hasChanges = true;
    }

    @Nullable
    @Override
    public BlockPos getSelectedPoint2()
    {
        return selectedPoint2;
    }

    @Override
    public void setSelectedPoint2(@Nullable BlockPos pos)
    {
        selectedPoint2 = pos;
        hasChanges = true;
    }

    public void sendSelectionToClients(Entity entity)
    {
        if (!entity.world.isRemote && !RecurrentComplex.isLite())
            IvNetworkHelperServer.sendEEPUpdatePacket(entity, CAPABILITY_KEY, null, "selection", RecurrentComplex.network);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        BlockPositions.writeToNBT("selectedPoint1", selectedPoint1, compound);
        BlockPositions.writeToNBT("selectedPoint2", selectedPoint2, compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        selectedPoint1 = BlockPositions.readFromNBT("selectedPoint1", compound);
        selectedPoint2 = BlockPositions.readFromNBT("selectedPoint2", compound);

        hasChanges = true; // Hax
    }

    public void update(Entity entity)
    {
        if (hasChanges)
            sendChanges(entity);
    }

    public void sendChanges(Entity entity)
    {
        hasChanges = false;
        sendSelectionToClients(entity);
    }

    @Override
    public void writeUpdateData(ByteBuf buffer, String context, Object... params)
    {
        if ("selection".equals(context))
        {
            BlockPositions.maybeWriteToBuffer(selectedPoint1, buffer);
            BlockPositions.maybeWriteToBuffer(selectedPoint2, buffer);
        }
    }

    @Override
    public void readUpdateData(ByteBuf buffer, String context)
    {
        if ("selection".equals(context))
        {
            selectedPoint1 = BlockPositions.maybeReadFromBuffer(buffer);
            selectedPoint2 = BlockPositions.maybeReadFromBuffer(buffer);
        }
    }
}
