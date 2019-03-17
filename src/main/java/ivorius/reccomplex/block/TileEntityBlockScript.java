/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.block;

import ivorius.reccomplex.world.gen.feature.structure.generic.transformers.RunTransformer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ivorius.ivtoolkit.tools.NBTCompoundObjects;
import ivorius.reccomplex.gui.worldscripts.GuiEditScriptBlock;
import ivorius.reccomplex.world.gen.script.WorldScriptMulti;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureLoadContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructurePrepareContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

/**
 * Created by lukas on 06.06.14.
 */
public class TileEntityBlockScript extends TileEntity implements GeneratingTileEntity<WorldScriptMulti.InstanceData>, TileEntityWithGUI
{
    public final WorldScriptMulti script = new WorldScriptMulti();

    public boolean spawnTriggerable = true;
    public boolean redstoneTriggerable = false;
    public boolean redstoneTriggered = false;

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound)
    {
        super.readFromNBT(nbtTagCompound);

        readSyncedNBT(nbtTagCompound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound)
    {
        super.writeToNBT(nbtTagCompound);

        writeSyncedNBT(nbtTagCompound);

        return nbtTagCompound;
    }

    @Override
    public WorldScriptMulti.InstanceData prepareInstanceData(StructurePrepareContext context)
    {
        return spawnTriggerable ? doPrepareInstanceData(context) : null;
    }

    public WorldScriptMulti.InstanceData doPrepareInstanceData(StructurePrepareContext context)
    {
        return script.prepareInstanceData(context, getPos());
    }

    @Override
    public WorldScriptMulti.InstanceData loadInstanceData(StructureLoadContext context, NBTBase nbt)
    {
        return script.loadInstanceData(context, nbt);
    }

    @Override
    public void generate(StructureSpawnContext context, RunTransformer transformer, WorldScriptMulti.InstanceData instanceData)
    {
        script.generate(context, transformer, instanceData, pos);
    }

    @Override
    public boolean shouldPlaceInWorld(StructurePrepareContext context, WorldScriptMulti.InstanceData instanceData)
    {
        return redstoneTriggerable;
    }

    @Override
    public void writeSyncedNBT(NBTTagCompound compound)
    {
        compound.setTag("script", NBTCompoundObjects.write(script));

        compound.setBoolean("spawnTriggerable", spawnTriggerable);
        compound.setBoolean("redstoneTriggerable", redstoneTriggerable);
        compound.setBoolean("redstoneTriggered", redstoneTriggered);
    }

    @Override
    public void readSyncedNBT(final NBTTagCompound compound)
    {
        script.readFromNBT(compound.getCompoundTag("script"));

        spawnTriggerable = !compound.hasKey("spawnTriggerable", Constants.NBT.TAG_BYTE) // Legacy
                || compound.getBoolean("spawnTriggerable");
        redstoneTriggerable = compound.getBoolean("redstoneTriggerable");
        redstoneTriggered = compound.getBoolean("redstoneTriggered");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void openEditGUI()
    {
        Minecraft.getMinecraft().displayGuiScreen(new GuiEditScriptBlock(this));
    }
}
