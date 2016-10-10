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
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Created by lukas on 27.08.16.
 */
public class GuiScreenEditTable<T extends TableDataSource> extends GuiScreenModalTable
{
    public static final int HEIGHT_INSET = 20;
    public static final int SIDE_INSET = 50;

    private T t;
    private Consumer<T> saver;

    public GuiTable setDataSource(T dataSource, Consumer<T> saver)
    {
        this.saver = saver;
        GuiTable table = new GuiTable(this, this.t = dataSource);
        setTable(table);
        return table;
    }

    public int uWidth()
    {
        return width - SIDE_INSET * 2;
    }

    public int uHeight()
    {
        return height - HEIGHT_INSET * 2;
    }

    @Override
    public void initGui()
    {
        if (currentTable() != null)
        {
            currentTable().setPropertiesBounds(Bounds.fromAxes(SIDE_INSET, uWidth(), HEIGHT_INSET, uHeight() - 22));
        }
        super.initGui();

        if (tableStack().size() == 1)
        {
            buttonList.add(new GuiButton(1, SIDE_INSET, HEIGHT_INSET + uHeight() - 20, uWidth() / 3 - 1, 20, IvTranslations.get("gui.cancel")));
            buttonList.add(new GuiButton(0, SIDE_INSET + uWidth() / 3 + 1, HEIGHT_INSET + uHeight() - 20, uWidth() / 3 - 2, 20, IvTranslations.get("reccomplex.gui.save")));
            buttonList.add(new GuiButton(3, SIDE_INSET + uWidth() / 3 * 2 + 1, HEIGHT_INSET + uHeight() - 20, uWidth() / 3 - 1, 20, IvTranslations.get("reccomplex.gui.hidegui")));
        }
        else
        {
            buttonList.add(new GuiButton(2, SIDE_INSET, HEIGHT_INSET + uHeight() - 20, uWidth() / 2 - 1, 20, IvTranslations.get("gui.back")));
            buttonList.add(new GuiButton(3, SIDE_INSET + uWidth() / 2 + 1, HEIGHT_INSET + uHeight() - 20, uWidth() / 2 - 1, 20, IvTranslations.get("reccomplex.gui.hidegui")));
        }
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) throws IOException
    {
        if (keyCode == Keyboard.KEY_ESCAPE)
        {
            // Prevent quitting without saving
            if (tableStack().size() > 1)
                popTable();
        }
        else
            super.keyTyped(keyChar, keyCode);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);

        if (button.id == 0)
        {
            saver.accept(this.t);

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
        else if (button.id == 3)
        {
            GuiHider.hideGUI();
        }
    }

}
