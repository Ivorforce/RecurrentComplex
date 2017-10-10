/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.worldscripts;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.block.TileEntityBlockScript;
import ivorius.reccomplex.gui.table.screen.GuiScreenEditTable;
import ivorius.reccomplex.network.PacketEditTileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by lukas on 26.05.14.
 */

@SideOnly(Side.CLIENT)
public class GuiEditScriptBlock extends GuiScreenEditTable<TableDataSourceScriptBlock>
{
    public GuiEditScriptBlock(TileEntityBlockScript tileEntity)
    {
        // Don't directly edit so the sides are in sync
        TileEntityBlockScript copy = (TileEntityBlockScript) TileEntity.create(tileEntity.getWorld(), tileEntity.writeToNBT(new NBTTagCompound()));

        setDataSource(new TableDataSourceScriptBlock(copy, this, this), ds ->
        {
            tileEntity.readFromNBT(ds.script.writeToNBT(new NBTTagCompound()));
            RecurrentComplex.network.sendToServer(new PacketEditTileEntity(ds.script));
        });
    }
}
