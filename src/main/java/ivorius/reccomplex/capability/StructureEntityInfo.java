/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.capability;

import io.netty.buffer.ByteBuf;
import ivorius.ivtoolkit.network.IvNetworkHelperServer;
import ivorius.ivtoolkit.network.PartialUpdateHandler;
import ivorius.ivtoolkit.tools.NBTCompoundObject;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.operation.Operation;
import ivorius.reccomplex.operation.OperationRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.annotation.Nullable;

/**
 * Created by lukas on 24.05.14.
 */
public class StructureEntityInfo implements NBTCompoundObject, PartialUpdateHandler
{
    public static final String CAPABILITY_KEY = "structureEntityInfo";

    @CapabilityInject(StructureEntityInfo.class)
    public static Capability<StructureEntityInfo> CAPABILITY;

    private Operation.PreviewType previewType = Operation.PreviewType.SHAPE;
    public Operation danglingOperation;
    public boolean showGrid = false;
    private boolean hasChanges;
    private NBTTagCompound cachedExportStructureBlockDataNBT;
    private NBTTagCompound worldDataClipboard;

    @Nullable
    public static StructureEntityInfo getStructureEntityInfo(Object object, @Nullable EnumFacing facing)
    {
        if (object instanceof StructureEntityInfo)
            return (StructureEntityInfo) object;

        if (object instanceof ICapabilityProvider)
            return ((ICapabilityProvider) object).getCapability(CAPABILITY, facing);

        return null;
    }

    public Operation.PreviewType getPreviewType()
    {
        return RecurrentComplex.isLite() ? Operation.PreviewType.NONE : previewType;
    }

    public void setPreviewType(Operation.PreviewType previewType)
    {
        this.previewType = previewType;
    }

    public void sendPreviewTypeToClients(Entity entity)
    {
        if (!entity.worldObj.isRemote && !RecurrentComplex.isLite())
            IvNetworkHelperServer.sendEEPUpdatePacket(entity, CAPABILITY_KEY, null, "previewType", RecurrentComplex.network);
    }

    public void sendOperationToClients(Entity entity)
    {
        if (!entity.worldObj.isRemote && !RecurrentComplex.isLite())
            IvNetworkHelperServer.sendEEPUpdatePacket(entity, CAPABILITY_KEY, null, "operation", RecurrentComplex.network);
    }

    public void sendOptionsToClients(Entity entity)
    {
        if (!entity.worldObj.isRemote && !RecurrentComplex.isLite())
            IvNetworkHelperServer.sendEEPUpdatePacket(entity, CAPABILITY_KEY, null, "options", RecurrentComplex.network);
    }

    public NBTTagCompound getCachedExportStructureBlockDataNBT()
    {
        return cachedExportStructureBlockDataNBT;
    }

    public void setCachedExportStructureBlockDataNBT(NBTTagCompound cachedExportStructureBlockDataNBT)
    {
        this.cachedExportStructureBlockDataNBT = cachedExportStructureBlockDataNBT;
    }

    public NBTTagCompound getWorldDataClipboard()
    {
        return worldDataClipboard;
    }

    public void setWorldDataClipboard(NBTTagCompound worldDataClipboard)
    {
        this.worldDataClipboard = worldDataClipboard;
    }

    public void queueOperation(Operation operation, Entity owner)
    {
        danglingOperation = operation;
        sendOperationToClients(owner);
    }

    public boolean performOperation(WorldServer world, Entity owner)
    {
        if (danglingOperation != null)
        {
            danglingOperation.perform(world);
            danglingOperation = null;
            sendOperationToClients(owner);
            return true;
        }

        return false;
    }

    public boolean cancelOperation(World world, Entity owner)
    {
        if (danglingOperation != null)
        {
            danglingOperation = null;
            sendOperationToClients(owner);
            return true;
        }

        return false;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        compound.setString("previewType", previewType.key);

        if (RCConfig.savePlayerCache)
        {
            if (danglingOperation != null)
                compound.setTag("danglingOperation", OperationRegistry.writeOperation(danglingOperation));
            if (worldDataClipboard != null)
                compound.setTag("worldDataClipboard", worldDataClipboard);
        }

        compound.setBoolean("showGrid", showGrid);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        previewType = Operation.PreviewType.findOrDefault(compound.getString("previewType"), Operation.PreviewType.SHAPE);

        if (RCConfig.savePlayerCache)
        {
            if (compound.hasKey("danglingOperation", Constants.NBT.TAG_COMPOUND))
                danglingOperation = OperationRegistry.readOperation(compound.getCompoundTag("danglingOperation"));
            if (compound.hasKey("worldDataClipboard", Constants.NBT.TAG_COMPOUND))
                worldDataClipboard = compound.getCompoundTag("worldDataClipboard");
        }

        showGrid = compound.getBoolean("showGrid");

        hasChanges = true;
    }

    public void update(Entity entity)
    {
        if (hasChanges)
        {
            hasChanges = false;
            sendPreviewTypeToClients(entity);
            sendOperationToClients(entity);
            sendOptionsToClients(entity);
        }
    }

    @Override
    public void writeUpdateData(ByteBuf buffer, String context, Object... params)
    {
        if ("previewType".equals(context))
        {
            ByteBufUtils.writeUTF8String(buffer, previewType.key);
        }
        else if ("operation".equals(context))
        {
            ByteBufUtils.writeTag(buffer, danglingOperation != null ? OperationRegistry.writeOperation(danglingOperation) : null);
        }
        else if ("options".equals(context))
        {
            buffer.writeBoolean(showGrid);
        }
    }

    @Override
    public void readUpdateData(ByteBuf buffer, String context)
    {
        if ("previewType".equals(context))
        {
            previewType = Operation.PreviewType.findOrDefault(ByteBufUtils.readUTF8String(buffer), Operation.PreviewType.SHAPE);
        }
        else if ("operation".equals(context))
        {
            try
            {
                NBTTagCompound tag = ByteBufUtils.readTag(buffer);
                danglingOperation = tag != null ? OperationRegistry.readOperation(tag) : null;
            }
            catch (Exception e)
            {
                RecurrentComplex.logger.warn("Error reading operation tag", buffer);
            }
        }
        else if ("options".equals(context))
        {
            showGrid = buffer.readBoolean();
        }
    }
}
