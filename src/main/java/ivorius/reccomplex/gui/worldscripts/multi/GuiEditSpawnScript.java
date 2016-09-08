/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.multi;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.blocks.TileEntitySpawnScript;
import ivorius.reccomplex.gui.table.Bounds;
import ivorius.reccomplex.gui.table.GuiScreenModalTable;
import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.network.PacketEditTileEntity;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

/**
 * Created by lukas on 26.05.14.
 */
public class GuiEditSpawnScript extends GuiScreenModalTable
{
    TileEntitySpawnScript tileEntity;
    TableDataSourceWorldScriptMulti structureDataSource;

    public GuiEditSpawnScript(TileEntitySpawnScript tileEntity)
    {
        GuiTable structureGenProperties = new GuiTable(this, structureDataSource = new TableDataSourceWorldScriptMulti(tileEntity.script, this, this));
        structureGenProperties.setHideScrollbarIfUnnecessary(true);
        setTable(structureGenProperties);
        this.tileEntity = tileEntity;
    }

    @Override
    public void initGui()
    {
        if (currentTable() != null)
        {
            currentTable().setPropertiesBounds(Bounds.fromAxes(width / 2 - 155, 310, height / 2 - 110, 195));
        }
        super.initGui();

        if (tableStack().size() == 1)
        {
            buttonList.add(new GuiButton(0, width / 2 - 155, height / 2 + 90, 310, 20, IvTranslations.get("gui.done")));
        }
        else
        {
            buttonList.add(new GuiButton(2, width / 2 - 155, height / 2 + 90, 310, 20, IvTranslations.get("gui.back")));
        }
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) throws IOException
    {
        if (keyCode == Keyboard.KEY_ESCAPE)
        {
            RecurrentComplex.network.sendToServer(new PacketEditTileEntity(tileEntity));
            this.mc.thePlayer.closeScreen();
        }
        else
        {
            super.keyTyped(keyChar, keyCode);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);

        if (button.id == 0)
        {
            RecurrentComplex.network.sendToServer(new PacketEditTileEntity(tileEntity));
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
