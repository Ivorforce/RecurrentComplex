/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.cell;

import ivorius.reccomplex.gui.table.Bounds;
import ivorius.reccomplex.gui.table.GuiTable;
import net.minecraft.client.gui.GuiButton;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by lukas on 02.06.14.
 */
public class TableCellPresetAction extends TableCellPropertyDefault<String>
{
    public static final int DIRECTION_BUTTON_WIDTH = 20;

    protected GuiButton leftButton;
    protected GuiButton rightButton;

    protected final List<TableCellButton> actions = new ArrayList<>();

    public TableCellPresetAction(String id, List<TableCellButton> actions)
    {
        super(id, actions.size() > 0 ? actions.get(0).actionID : "");
        this.actions.addAll(actions);
        setPropertyValue(property); // Now that actions is set
    }

    public static Stream<TableCellButton> sorted(Stream<TableCellButton> actions)
    {
        return actions.sorted(Comparator.comparing(o -> o.title));
    }

    public void addListener(TableCellActionListener listener)
    {
        actions.forEach(a -> a.addListener(listener));
    }

    public TableCellActionListener addAction(Consumer<String> consumer)
    {
        TableCellActionListener listener = (cell, action) -> consumer.accept(action);
        actions.forEach(a -> a.addListener(listener));
        return listener;
    }

    public void removeListener(TableCellActionListener listener)
    {
        actions.forEach(a -> a.removeListener(listener));
    }

    public List<TableCellButton> getActions()
    {
        return actions;
    }

    @Override
    public void initGui(GuiTable screen)
    {
        super.initGui(screen);

        Bounds bounds = bounds();

        int buttonY = bounds.getMinY() + (bounds.getHeight() - 20) / 2;
        int presetButtonWidth = bounds.getWidth() - DIRECTION_BUTTON_WIDTH * 2;

        boolean canChange = actions.size() > 1 || (actions.size() == 1 && !Objects.equals(getPropertyValue(), actions.get(0).actionID));

        leftButton = new GuiButton(-1, bounds.getMinX(), buttonY, DIRECTION_BUTTON_WIDTH - 1, 20, "<");
        leftButton.visible = !isHidden();
        leftButton.enabled = canChange;
        screen.addButton(this, 0, leftButton);

        rightButton = new GuiButton(-1, bounds.getMinX() + DIRECTION_BUTTON_WIDTH + presetButtonWidth + 1, buttonY, DIRECTION_BUTTON_WIDTH - 1, 20, ">");
        rightButton.visible = !isHidden();
        rightButton.enabled = canChange;
        screen.addButton(this, 1, rightButton);

        for (TableCellButton action : actions)
            action.initGui(screen);
    }

    @Override
    public void setBounds(Bounds bounds)
    {
        super.setBounds(bounds);

        int buttonY = bounds.getMinY() + (bounds.getHeight() - 20) / 2;
        int directionButtonWidth = 20;
        int presetButtonWidth = bounds.getWidth() - directionButtonWidth * 2;

        actions.forEach(a -> a.setBounds(Bounds.fromSize(bounds.getMinX() + directionButtonWidth + 1, buttonY, presetButtonWidth - 2, 20)));
    }

    @Override
    public void setHidden(boolean hidden)
    {
        super.setHidden(hidden);

        if (leftButton != null)
            leftButton.visible = !hidden;
        if (rightButton != null)
            rightButton.visible = !hidden;
        TableCellButton action = findAction(getPropertyValue());
        if (action != null)
            action.setHidden(hidden);
    }

    @Override
    public void buttonClicked(int buttonID)
    {
        super.buttonClicked(buttonID);

        if (buttonID == 0 || buttonID == 1)
            move(buttonID == 0 ? -1 : 1);
    }

    @Override
    public void setPropertyValue(String value)
    {
        super.setPropertyValue(value);

        if (actions != null)
        {
            actions.forEach(a -> a.setHidden(true));

            TableCellButton actionB = findAction(value);
            if (actionB != null)
                actionB.setHidden(isHidden());
        }
    }

    public void move(int plus)
    {
        setPropertyValue(actions.get((((findIndex(getPropertyValue()) + plus) % actions.size()) + actions.size()) % actions.size()).actionID);
    }

    @Override
    public void draw(GuiTable screen, int mouseX, int mouseY, float partialTicks)
    {
        super.draw(screen, mouseX, mouseY, partialTicks);

        TableCellButton action = findAction(getPropertyValue());
        if (action != null)
            action.draw(screen, mouseX, mouseY, partialTicks);
    }

    @Override
    public void drawFloating(GuiTable screen, int mouseX, int mouseY, float partialTicks)
    {
        super.drawFloating(screen, mouseX, mouseY, partialTicks);

        TableCellButton action = findAction(getPropertyValue());
        if (action != null)
            action.drawFloating(screen, mouseX, mouseY, partialTicks);
    }

    public TableCellButton findAction(String actionID)
    {
        int index = findIndex(actionID);
        return index >= 0 ? actions.get(index) : null;
    }

    protected int findIndex(String actionID)
    {
        int currentIndex = -1;

        for (int i = 0; i < actions.size(); i++)
        {
            TableCellButton action = actions.get(i);

            if (action.actionID.equals(actionID))
                currentIndex = i;
        }

        return currentIndex;
    }
}
