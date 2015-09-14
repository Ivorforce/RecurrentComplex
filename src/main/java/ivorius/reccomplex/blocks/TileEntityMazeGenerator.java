/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.reccomplex.gui.editmazeblock.GuiEditMazeBlock;
import ivorius.reccomplex.scripts.world.WorldScriptMazeGenerator;
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
public class TileEntityMazeGenerator extends TileEntity implements GeneratingTileEntity<WorldScriptMazeGenerator.InstanceData>, TileEntityWithGUI
{
    public WorldScriptMazeGenerator script = new WorldScriptMazeGenerator();

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
    public WorldScriptMazeGenerator.InstanceData prepareInstanceData(StructurePrepareContext context)
    {
        return script.prepareInstanceData(context, new BlockCoord(this), worldObj);
    }

    @Override
    public WorldScriptMazeGenerator.InstanceData loadInstanceData(StructureLoadContext context, NBTBase nbt)
    {
        return script.loadInstanceData(context, nbt);
    }

    @Override
    public void generate(StructureSpawnContext context, WorldScriptMazeGenerator.InstanceData instanceData)
    {
        script.generate(context, instanceData, new BlockCoord(this));
    }

    @Override
    public boolean shouldPlaceInWorld(StructureSpawnContext context, WorldScriptMazeGenerator.InstanceData instanceData)
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
        else // Legacy
            script.readFromNBT(compound);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void openEditGUI()
    {
        Minecraft.getMinecraft().displayGuiScreen(new GuiEditMazeBlock(this));
    }

}
