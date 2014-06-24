/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.gui.editmazeblock;

import ivorius.structuregen.StructureGen;
import ivorius.structuregen.blocks.TileEntityMazeGenerator;
import ivorius.structuregen.gui.table.Bounds;
import ivorius.structuregen.gui.table.GuiScreenModalTable;
import ivorius.structuregen.gui.table.GuiTable;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.input.Keyboard;

/**
 * Created by lukas on 26.05.14.
 */
public class GuiEditMazeBlock extends GuiScreenModalTable
{
    TableDataSourceMazeBlock structureDataSource;

    public GuiEditMazeBlock(TileEntityMazeGenerator structureGenerator)
    {
        GuiTable structureGenProperties = new GuiTable(this, structureDataSource = new TableDataSourceMazeBlock(structureGenerator, this, this));
        structureGenProperties.setHideScrollbarIfUnnecessary(true);
        setTable(structureGenProperties);
    }

    @Override
    public void initGui()
    {
        if (currentTable() != null)
        {
            currentTable().setPropertiesBounds(Bounds.boundsWithSize(width / 2 - 155, 310, height / 2 - 110, 195));
        }
        super.initGui();

        if (tableStack().size() == 1)
        {
            buttonList.add(new GuiButton(0, width / 2 - 155, height / 2 + 90, 310, 20, "Done"));
        }
        else
        {
            buttonList.add(new GuiButton(2, width / 2 - 155, height / 2 + 90, 310, 20, "Back"));
        }
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode)
    {
        if (keyCode == Keyboard.KEY_ESCAPE)
        {
            StructureGen.chEditMazeBlock.sendSaveEdit(this.mc.thePlayer, structureDataSource.getMazeGenerator());
            this.mc.thePlayer.closeScreen();
        }
        else
        {
            super.keyTyped(keyChar, keyCode);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        super.actionPerformed(button);

        if (button.id == 0)
        {
            StructureGen.chEditMazeBlock.sendSaveEdit(this.mc.thePlayer, structureDataSource.getMazeGenerator());
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
