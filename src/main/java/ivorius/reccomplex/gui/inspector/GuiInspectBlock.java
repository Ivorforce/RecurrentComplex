/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.inspector;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.table.screen.GuiScreenEditTable;
import ivorius.reccomplex.network.PacketInspectBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

/**
 * Created by lukas on 27.08.16.
 */
public class GuiInspectBlock extends GuiScreenEditTable<TableDataSourceInspectBlock>
{
    public GuiInspectBlock(BlockPos pos, IBlockState state, NBTTagCompound tileEntityData)
    {
        setDataSource(new TableDataSourceInspectBlock(pos, state, this, this, tileEntityData), ds ->
                RecurrentComplex.network.sendToServer(new PacketInspectBlock(ds.tileEntityData, ds.pos, ds.state)));
    }
}
