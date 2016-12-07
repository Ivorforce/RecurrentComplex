/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.inspector;

import ivorius.reccomplex.gui.table.screen.GuiScreenEditTable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

/**
 * Created by lukas on 27.08.16.
 */
public class GuiBlockInspector extends GuiScreenEditTable<TableDataSourceBlockInspector>
{
    public GuiBlockInspector(BlockPos pos, IBlockState state)
    {
        setDataSource(new TableDataSourceBlockInspector(pos, state, this, this), ds -> this.mc.player.sendChatMessage(String.format("/setblock %d %d %d %s %d",
                ds.pos.getX(), ds.pos.getY(), ds.pos.getZ(),
                Block.REGISTRY.getNameForObject(ds.state.getBlock()).toString(), ivorius.ivtoolkit.blocks.BlockStates.toMetadata(ds.state))));
    }
}
