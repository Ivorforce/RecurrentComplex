/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.tools.IvCollections;
import ivorius.reccomplex.gui.editspawncommandblock.GuiEditSpawnCommandBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 06.06.14.
 */
public class TileEntitySpawnCommand extends TileEntity implements GeneratingTileEntity, TileEntityWithGUI
{
    public List<Entry> entries = new ArrayList<>();

    public List<Entry> getEntries()
    {
        return Collections.unmodifiableList(entries);
    }

    public void setEntries(List<Entry> entries)
    {
        IvCollections.setContentsOfList(this.entries, entries);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound)
    {
        super.readFromNBT(nbtTagCompound);

        readSyncedNBT(nbtTagCompound);
    }

    @Override
    public void readSyncedNBT(NBTTagCompound nbtTagCompound)
    {
        entries.clear();
        NBTTagList entryNBTs = nbtTagCompound.getTagList("commands", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < entryNBTs.tagCount(); i++)
            entries.add(new Entry(entryNBTs.getCompoundTagAt(i)));
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
    public void writeSyncedNBT(NBTTagCompound nbtTagCompound)
    {
        NBTTagList structureNBTList = new NBTTagList();
        for (Entry entry : entries)
            structureNBTList.appendTag(entry.writeToNBT());
        nbtTagCompound.setTag("commands", structureNBTList);
    }

    @Override
    public void generate(World world, Random random, AxisAlignedTransform2D transform, int layer)
    {
        world.setBlockToAir(xCoord, yCoord, zCoord);

        if (entries.size() > 0)
        {
            Entry entry = (Entry) WeightedRandom.getRandomItem(random, entries);
            SpawnCommandLogic logic = new SpawnCommandLogic(this, entry.command);
            logic.executeCommand(world);
        }
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

    public static class Entry extends WeightedRandom.Item
    {
        public String command;

        public Entry(int weight, String command)
        {
            super(weight);
            this.command = command;
        }

        public Entry(NBTTagCompound compound)
        {
            this(compound.getInteger("weight"), compound.getString("command"));
        }

        public NBTTagCompound writeToNBT()
        {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setString("command", command);
            compound.setInteger("weight", itemWeight);
            return compound;
        }
    }
}
