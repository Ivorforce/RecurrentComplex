/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.reccomplex.gui.worldscripts.command.GuiEditSpawnCommandBlock;
import ivorius.reccomplex.scripts.world.WorldScriptCommand;
import ivorius.reccomplex.structures.StructureLoadContext;
import ivorius.reccomplex.structures.StructurePrepareContext;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.utils.NBTNone;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

/**
 * Created by lukas on 06.06.14.
 */
public class TileEntitySpawnCommand extends TileEntity implements GeneratingTileEntity<NBTNone>, TileEntityWithGUI
{
    public WorldScriptCommand script = new WorldScriptCommand();

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound)
    {
        super.readFromNBT(nbtTagCompound);

        readSyncedNBT(nbtTagCompound);
    }

    @Override
    public void readSyncedNBT(NBTTagCompound compound)
    {
        if (compound.hasKey("script"))
            script.readFromNBT(compound.getCompoundTag("script"));
        else // Legacy
            script.readFromNBT(compound);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void openEditGUI()
    {
        Minecraft.getMinecraft().displayGuiScreen(new GuiEditSpawnCommandBlock(this));
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound)
    {
        super.writeToNBT(nbtTagCompound);

        writeSyncedNBT(nbtTagCompound);
    }

    @Override
    public void writeSyncedNBT(NBTTagCompound compound)
    {
        NBTTagCompound scriptCompound = new NBTTagCompound();
        script.writeToNBT(scriptCompound);
        compound.setTag("script", scriptCompound);
    }

    @Override
    public void generate(StructureSpawnContext context, NBTNone instanceData)
    {
        script.generate(context, instanceData, new BlockCoord(this));
    }

    @Override
    public boolean shouldPlaceInWorld(StructureSpawnContext context, NBTNone instanceData)
    {
        return false;
    }

    @Override
    public NBTNone prepareInstanceData(StructurePrepareContext context)
    {
        return script.prepareInstanceData(context, new BlockCoord(this), worldObj);
    }

    @Override
    public NBTNone loadInstanceData(StructureLoadContext context, NBTBase nbt)
    {
        return script.loadInstanceData(context, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
    {
        readSyncedNBT(pkt.func_148857_g());
    }

    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        this.writeSyncedNBT(nbttagcompound);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, nbttagcompound);
    }
}
