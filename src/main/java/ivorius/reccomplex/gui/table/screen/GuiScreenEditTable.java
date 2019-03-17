/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.screen;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.GuiHider;
import ivorius.reccomplex.gui.table.Bounds;
import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Created by lukas on 27.08.16.
 */

@SideOnly(Side.CLIENT)
public class GuiScreenEditTable<T extends TableDataSource> extends GuiScreenModalTable
{
    public static final int HEIGHT_INSET = 10;
    public static final int MAX_WIDTH = 360;

    protected T t;
    protected Consumer<T> saver;

    protected GuiButton escButton;

    public GuiScreenEditTable()
    {
    }

    public GuiTable setDataSource(T dataSource, Consumer<T> saver)
    {
        this.saver = saver;
        GuiTable table = new GuiTable(this, this.t = dataSource);
        setTable(table);
        return table;
    }

    public int uHeight()
    {
        return height - HEIGHT_INSET * 2;
    }

    public int uWidth()
    {
        return Math.min(width - 10, MAX_WIDTH);
    }

    public int leftEdge()
    {
        return (width - uWidth()) / 2;
    }

    @Override
    public void initGui()
    {
        if (currentTable() != null) {
            currentTable().setBounds(Bounds.fromAxes(leftEdge(), uWidth(), HEIGHT_INSET, uHeight() - 22));
        }
        super.initGui();

        buttonList.add(escButton = tableStack().size() == 1
                ? new GuiButton(1, leftEdge(), HEIGHT_INSET + uHeight() - 20, uWidth() / 3 - 1, 20, TextFormatting.GOLD + IvTranslations.get("gui.cancel"))
                : new GuiButton(2, leftEdge(), HEIGHT_INSET + uHeight() - 20, uWidth() / 3 - 1, 20, IvTranslations.format("gui.back.page", tableStack().size() - 1)));
        buttonList.add(new GuiButton(0, leftEdge() + uWidth() / 3 + 1, HEIGHT_INSET + uHeight() - 20, uWidth() / 3 - 2, 20, TextFormatting.GREEN + IvTranslations.get("reccomplex.gui.save")));
        GuiButton hideButton = new GuiButton(3, leftEdge() + uWidth() / 3 * 2 + 1, HEIGHT_INSET + uHeight() - 20, uWidth() / 3 - 1, 20,
                currentTable().getDataSource().canVisualize() ? IvTranslations.get("reccomplex.gui.visualizegui") : IvTranslations.get("reccomplex.gui.hidegui"));
        hideButton.enabled = GuiHider.canHide();
        buttonList.add(hideButton);
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) throws IOException
    {
        if (keyCode == Keyboard.KEY_ESCAPE) // Would otherwise close GUI
        {
            if (escButton != null)
                actionPerformed(escButton);

            return;
        }

        super.keyTyped(keyChar, keyCode);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);

        if (button.id == 0) {
            saver.accept(this.t);

            this.mc.player.closeScreen();
        }
        else if (button.id == 1) {
            this.mc.player.closeScreen();
        }
        else if (button.id == 2) {
            popTable();
        }
        else if (button.id == 3) {
            if (currentTable().getDataSource().canVisualize())
                GuiHider.hideGUI(currentTable().getDataSource().visualizer());
            else
                GuiHider.hideGUI();
        }
    }
}
