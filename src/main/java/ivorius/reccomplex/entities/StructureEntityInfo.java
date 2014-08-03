/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.entities;

import io.netty.buffer.ByteBuf;
import ivorius.ivtoolkit.network.IvNetworkHelperServer;
import ivorius.ivtoolkit.network.PartialUpdateHandler;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.ivtoolkit.blocks.BlockCoord;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

/**
 * Created by lukas on 24.05.14.
 */
public class StructureEntityInfo implements IExtendedEntityProperties, PartialUpdateHandler
{
    private boolean hasChanges;

    public BlockCoord selectedPoint1;
    public BlockCoord selectedPoint2;

    private NBTTagCompound cachedExportStructureBlockDataNBT;

    private NBTTagCompound worldDataClipboard;

    public static StructureEntityInfo getStructureEntityInfo(Entity entity)
    {
        return (StructureEntityInfo) entity.getExtendedProperties("structureEntityInfo");
    }

    public static void initInEntity(Entity entity)
    {
        entity.registerExtendedProperties("structureEntityInfo", new StructureEntityInfo());
    }

    public boolean hasValidSelection()
    {
        return selectedPoint1 != null && selectedPoint2 != null;
    }

    public void sendSelectionChangesToClients(Entity entity)
    {
        IvNetworkHelperServer.sendEEPUpdatePacket(entity, "structureEntityInfo", "selection", RecurrentComplex.network);
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

    @Override
    public void saveNBTData(NBTTagCompound compound)
    {
        BlockCoord.writeCoordToNBT("selectedPoint1", selectedPoint1, compound);
        BlockCoord.writeCoordToNBT("selectedPoint2", selectedPoint2, compound);
    }

    @Override
    public void loadNBTData(NBTTagCompound compound)
    {
        selectedPoint1 = BlockCoord.readCoordFromNBT("selectedPoint1", compound);
        selectedPoint2 = BlockCoord.readCoordFromNBT("selectedPoint2", compound);
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
            sendSelectionChangesToClients(entity);
        }
    }

    @Override
    public void writeUpdateData(ByteBuf buffer, String context)
    {
        if ("selection".equals(context))
        {
            BlockCoord.writeCoordToBuffer(selectedPoint1, buffer);
            BlockCoord.writeCoordToBuffer(selectedPoint2, buffer);
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
    }
}
