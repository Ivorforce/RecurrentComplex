/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.entities;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.network.IvNetworkHelperServer;
import ivorius.ivtoolkit.network.PartialUpdateHandler;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.operation.Operation;
import ivorius.reccomplex.operation.OperationRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

/**
 * Created by lukas on 24.05.14.
 */
public class StructureEntityInfo implements IExtendedEntityProperties, PartialUpdateHandler
{
    public static final String EEP_KEY = "structureEntityInfo";
    public BlockCoord selectedPoint1;
    public BlockCoord selectedPoint2;
    public int previewType = Operation.PREVIEW_TYPE_BOUNDING_BOX;
    public Operation danglingOperation;
    public boolean showGrid = false;
    private boolean hasChanges;
    private NBTTagCompound cachedExportStructureBlockDataNBT;
    private NBTTagCompound worldDataClipboard;

    @Nullable
    public static StructureEntityInfo getStructureEntityInfo(Entity entity)
    {
        return (StructureEntityInfo) entity.getExtendedProperties(EEP_KEY);
    }

    public static void initInEntity(Entity entity)
    {
        entity.registerExtendedProperties(EEP_KEY, new StructureEntityInfo());
    }

    public boolean hasValidSelection()
    {
        return selectedPoint1 != null && selectedPoint2 != null;
    }

    public void sendSelectionToClients(Entity entity)
    {
        if (!entity.worldObj.isRemote)
            IvNetworkHelperServer.sendEEPUpdatePacket(entity, EEP_KEY, "selection", RecurrentComplex.network);
    }

    public void sendPreviewTypeToClients(Entity entity)
    {
        if (!entity.worldObj.isRemote)
            IvNetworkHelperServer.sendEEPUpdatePacket(entity, EEP_KEY, "previewType", RecurrentComplex.network);
    }

    public void sendOperationToClients(Entity entity)
    {
        if (!entity.worldObj.isRemote)
            IvNetworkHelperServer.sendEEPUpdatePacket(entity, EEP_KEY, "operation", RecurrentComplex.network);
    }

    public void sendOptionsToClients(Entity entity)
    {
        if (!entity.worldObj.isRemote)
            IvNetworkHelperServer.sendEEPUpdatePacket(entity, EEP_KEY, "options", RecurrentComplex.network);
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

    public boolean performOperation(World world, Entity owner)
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
    public void saveNBTData(NBTTagCompound compound)
    {
        BlockCoord.writeCoordToNBT("selectedPoint1", selectedPoint1, compound);
        BlockCoord.writeCoordToNBT("selectedPoint2", selectedPoint2, compound);

        compound.setInteger("previewType", previewType);

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
    public void loadNBTData(NBTTagCompound compound)
    {
        selectedPoint1 = BlockCoord.readCoordFromNBT("selectedPoint1", compound);
        selectedPoint2 = BlockCoord.readCoordFromNBT("selectedPoint2", compound);

        previewType = compound.getInteger("previewType");

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

    @Override
    public void init(Entity entity, World world)
    {

    }

    public void update(Entity entity)
    {
        if (hasChanges)
        {
            hasChanges = false;
            sendSelectionToClients(entity);
            sendPreviewTypeToClients(entity);
            sendOperationToClients(entity);
            sendOptionsToClients(entity);
        }
    }

    @Override
    public void writeUpdateData(ByteBuf buffer, String context, Object... params)
    {
        if ("selection".equals(context))
        {
            BlockCoord.writeCoordToBuffer(selectedPoint1, buffer);
            BlockCoord.writeCoordToBuffer(selectedPoint2, buffer);
        }
        else if ("previewType".equals(context))
        {
            buffer.writeInt(previewType);
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
        if ("selection".equals(context))
        {
            selectedPoint1 = BlockCoord.readCoordFromBuffer(buffer);
            selectedPoint2 = BlockCoord.readCoordFromBuffer(buffer);
        }
        else if ("previewType".equals(context))
        {
            previewType = buffer.readInt();
        }
        else if ("operation".equals(context))
        {
            NBTTagCompound tag = ByteBufUtils.readTag(buffer);
            danglingOperation = tag != null ? OperationRegistry.readOperation(tag) : null;
        }
        else if ("options".equals(context))
        {
            showGrid = buffer.readBoolean();
        }
    }
}
