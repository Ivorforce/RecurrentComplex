/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.reccomplex.gui.editstructureblock.GuiEditStructureBlock;
import ivorius.reccomplex.scripts.world.WorldScriptStructureGenerator;
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
public class TileEntityStructureGenerator extends TileEntity implements GeneratingTileEntity<WorldScriptStructureGenerator.InstanceData>, TileEntityWithGUI
{
    public WorldScriptStructureGenerator script = new WorldScriptStructureGenerator();

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
    public WorldScriptStructureGenerator.InstanceData prepareInstanceData(StructurePrepareContext context)
    {
        return script.prepareInstanceData(context, new BlockCoord(this), worldObj);
    }

    @Override
    public WorldScriptStructureGenerator.InstanceData loadInstanceData(StructureLoadContext context, NBTBase nbt)
    {
        return script.loadInstanceData(context, nbt);
    }

    @Override
    public void generate(StructureSpawnContext context, WorldScriptStructureGenerator.InstanceData instanceData)
    {
        script.generate(context, instanceData, new BlockCoord(this));
    }

    @Override
    public boolean shouldPlaceInWorld(StructureSpawnContext context, WorldScriptStructureGenerator.InstanceData instanceData)
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
        Minecraft.getMinecraft().displayGuiScreen(new GuiEditStructureBlock(this));
    }

}
