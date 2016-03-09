/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by lukas on 03.06.14.
 */
public class GuiScreenModalTable extends GuiScreen implements TableDelegate, TableNavigator
{
    private Stack<GuiTable> tableStack = new Stack<>();

    @Override
    public void initGui()
    {
        super.initGui();

        Keyboard.enableRepeatEvents(true);
        buttonList.clear();

        if (tableStack.size() > 0)
            tableStack.peek().initGui();
    }

    @Override
    protected void mouseClicked(int x, int y, int button)
    {
//        super.mouseClicked(x, y, button);
        if (button == 0) // Simulated from private GuiScreen behavior
        {
            List buttonListCopy = (List) ((ArrayList) buttonList).clone();

            for (int l = 0; l < buttonListCopy.size(); ++l)
            {
                GuiButton guibutton = (GuiButton) buttonListCopy.get(l);

                if (guibutton.mousePressed(this.mc, x, y))
                {
                    GuiScreenEvent.ActionPerformedEvent.Pre event = new GuiScreenEvent.ActionPerformedEvent.Pre(this, guibutton, buttonListCopy);
                    if (MinecraftForge.EVENT_BUS.post(event))
                    {
                        break;
                    }
                    ////////
//                    this.selectedButton = event.button;
                    ReflectionHelper.setPrivateValue(GuiScreen.class, this, event.button, "selectedButton", "field_146290_a");
                    ////////
                    event.button.func_146113_a(this.mc.getSoundHandler());
                    this.actionPerformed(event.button);
                    if (this.equals(this.mc.currentScreen))
                    {
                        MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.ActionPerformedEvent.Post(this, event.button, buttonListCopy));
                    }
                }
            }
        }

        if (tableStack.size() > 0)
        {
            tableStack.peek().mouseClicked(x, y, button);
        }
    }

    @Override
    public void handleMouseInput()
    {
        super.handleMouseInput();

        currentTable().handleMouseInput();
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode)
    {
        super.keyTyped(keyChar, keyCode);

        boolean used = false;
        if (tableStack.size() > 0)
        {
            used = tableStack.peek().keyTyped(keyChar, keyCode);
        }

        if (!used)
        {
            if (keyCode == Keyboard.KEY_UP)
            {
                currentTable().tryScrollUp();
            }
            else if (keyCode == Keyboard.KEY_DOWN)
            {
                currentTable().tryScrollDown();
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        super.actionPerformed(button);

        if (tableStack.size() > 0)
        {
            tableStack.peek().actionPerformed(button);
        }
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();

        if (tableStack.size() > 0)
        {
            tableStack.peek().updateScreen();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (tableStack.size() > 0)
        {
            tableStack.peek().drawScreen(this, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();

        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public Stack<GuiTable> tableStack()
    {
        return (Stack<GuiTable>) tableStack.clone();
    }

    public void pushTable(GuiTable table)
    {
        this.tableStack.push(table);

        if (mc != null)
        {
            reloadData();
        }
    }

    @Override
    public GuiTable currentTable()
    {
        return tableStack.peek();
    }

    @Override
    public GuiTable popTable()
    {
        GuiTable table = this.tableStack.pop();

        if (mc != null)
        {
            reloadData();
        }

        return table;
    }

    @Override
    public void setTable(GuiTable table)
    {
        this.tableStack.clear();

        if (table != null)
        {
            pushTable(table);
        }
    }

    @Override
    public void addButton(GuiButton button)
    {
        buttonList.add(button);
    }

    @Override
    public void redrawTable()
    {
        initGui();
    }

    @Override
    public void reloadData()
    {
        if (tableStack.size() > 0)
            tableStack.peek().clearElementCache();

        redrawTable();
    }
}
