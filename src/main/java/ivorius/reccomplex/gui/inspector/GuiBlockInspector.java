/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.inspector;

import ivorius.reccomplex.gui.table.Bounds;
import ivorius.reccomplex.gui.table.GuiScreenModalTable;
import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.utils.BlockStates;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

/**
 * Created by lukas on 27.08.16.
 */
public class GuiBlockInspector extends GuiScreenModalTable
{
    TableDataSourceBlockInspector structureDataSource;

    public GuiBlockInspector(BlockPos pos, IBlockState state)
    {
        GuiTable table = new GuiTable(this, structureDataSource = new TableDataSourceBlockInspector(pos, state, this, this));
        setTable(table);
    }

    @Override
    public void initGui()
    {
        if (currentTable() != null)
        {
            currentTable().setPropertiesBounds(Bounds.fromAxes(width / 2 - 155, 310, height / 2 - 110, 205));
        }
        super.initGui();

        if (tableStack().size() == 1)
        {
            buttonList.add(new GuiButton(1, width / 2 - 155, height / 2 + 95, 154, 20, "Cancel"));
            buttonList.add(new GuiButton(0, width / 2 + 1, height / 2 + 95, 154, 20, "Apply"));
        }
        else
        {
            buttonList.add(new GuiButton(2, width / 2 - 155, height / 2 + 95, 310, 20, "Back"));
        }
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) throws IOException
    {
        if (keyCode != Keyboard.KEY_ESCAPE) // Prevent quitting without saving
            super.keyTyped(keyChar, keyCode);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);

        if (button.id == 0)
        {
            this.mc.thePlayer.sendChatMessage(String.format("/setblock %d %d %d %s %d",
                    structureDataSource.pos.getX(), structureDataSource.pos.getY(), structureDataSource.pos.getZ(),
                    Block.REGISTRY.getNameForObject(structureDataSource.state.getBlock()).toString(), BlockStates.toMetadata(structureDataSource.state)));

            this.mc.thePlayer.closeScreen();
        }
        else if (button.id == 1)
        {
            this.mc.thePlayer.closeScreen();
        }
        else if (button.id == 2)
        {
            popTable();
        }
    }
}
