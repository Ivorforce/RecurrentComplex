/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.multi;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.block.TileEntitySpawnScript;
import ivorius.reccomplex.gui.table.GuiScreenEditTable;
import ivorius.reccomplex.network.PacketEditTileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

/**
 * Created by lukas on 26.05.14.
 */
public class GuiEditSpawnScript extends GuiScreenEditTable<TableDataSourceWorldScriptMulti>
{
    public GuiEditSpawnScript(TileEntitySpawnScript tileEntity)
    {
        // Don't directly edit so the sides are in sync
        TileEntitySpawnScript copy = (TileEntitySpawnScript) TileEntity.create(tileEntity.getWorld(), tileEntity.writeToNBT(new NBTTagCompound()));

        setDataSource(new TableDataSourceWorldScriptMulti(tileEntity.script, this, this), ds ->
        {
            tileEntity.readFromNBT(copy.writeToNBT(new NBTTagCompound()));
            RecurrentComplex.network.sendToServer(new PacketEditTileEntity(copy));
        });
    }
}
