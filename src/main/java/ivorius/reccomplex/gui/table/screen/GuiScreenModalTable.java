/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.screen;

import com.google.common.collect.Lists;
import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
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
    protected void mouseClicked(int x, int y, int button) throws IOException
    {
//        super.mouseClicked(x, y, button);
        if (button == 0) // Simulated from private GuiScreen behavior
        {
            ////////
            // Copy to avoid concurrent modifications, in case the click redraws the table
            List<GuiButton> buttonListCopy = Lists.newArrayList(buttonList);
            ////////
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
                    ReflectionHelper.setPrivateValue(GuiScreen.class, this, event.getButton(), "selectedButton", "field_146290_a");
                    ////////
                    event.getButton().playPressSound(this.mc.getSoundHandler());
                    this.actionPerformed(event.getButton());
                    if (this.equals(this.mc.currentScreen))
                    {
                        MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.ActionPerformedEvent.Post(this, event.getButton(), buttonListCopy));
                    }
                }
            }
        }

        if (tableStack.size() > 0)
            tableStack.peek().mouseClicked(x, y, button);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

        if (tableStack.size() > 0)
            tableStack.peek().mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);

        if (tableStack.size() > 0)
            tableStack.peek().mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();

        currentTable().handleMouseInput();
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) throws IOException
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
    protected void actionPerformed(GuiButton button) throws IOException
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
        //noinspection unchecked
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

    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    public <T extends GuiButton> T addButtonToTable(T button)
    {
        return addButton(button);
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
            tableStack.peek().clearCellCache();

        redrawTable();
    }

    @Override
    public void setLocked(String cell, boolean lock)
    {
        tableStack.peek().setLocked(cell, lock);
    }
}
