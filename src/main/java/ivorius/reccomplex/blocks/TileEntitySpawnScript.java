/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.reccomplex.gui.worldscripts.multi.GuiEditSpawnScript;
import ivorius.reccomplex.scripts.world.WorldScriptMulti;
import ivorius.reccomplex.structures.StructureLoadContext;
import ivorius.reccomplex.structures.StructurePrepareContext;
import ivorius.reccomplex.structures.StructureSpawnContext;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

/**
 * Created by lukas on 06.06.14.
 */
public class TileEntitySpawnScript extends TileEntity implements GeneratingTileEntity<WorldScriptMulti.InstanceData>, TileEntityWithGUI
{
    public final WorldScriptMulti script = new WorldScriptMulti();

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound)
    {
        super.readFromNBT(nbtTagCompound);

        readSyncedNBT(nbtTagCompound);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound)
    {
        super.writeToNBT(nbtTagCompound);

        writeSyncedNBT(nbtTagCompound);
    }

    @Override
    public WorldScriptMulti.InstanceData prepareInstanceData(StructurePrepareContext context)
    {
        return script.prepareInstanceData(context, new BlockCoord(this), worldObj);
    }

    @Override
    public WorldScriptMulti.InstanceData loadInstanceData(StructureLoadContext context, NBTBase nbt)
    {
        return script.loadInstanceData(context, nbt);
    }

    @Override
    public void generate(StructureSpawnContext context, WorldScriptMulti.InstanceData instanceData)
    {
        script.generate(context, instanceData, new BlockCoord(this));
    }

    @Override
    public boolean shouldPlaceInWorld(StructureSpawnContext context, WorldScriptMulti.InstanceData instanceData)
    {
        return false;
    }

    @Override
    public void writeSyncedNBT(NBTTagCompound compound)
    {
        NBTTagCompound scriptCompound = new NBTTagCompound();
        script.writeToNBT(scriptCompound);
        compound.setTag("script", scriptCompound);
    }

    @Override
    public void readSyncedNBT(final NBTTagCompound compound)
    {
        if (compound.hasKey("script"))
            script.readFromNBT(compound.getCompoundTag("script"));
        else
            script.readFromNBT(new NBTTagCompound());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void openEditGUI()
    {
        Minecraft.getMinecraft().displayGuiScreen(new GuiEditSpawnScript(this));
    }
}
