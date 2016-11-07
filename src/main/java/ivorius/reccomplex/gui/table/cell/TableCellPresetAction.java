/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.cell;

import ivorius.reccomplex.gui.table.Bounds;
import ivorius.reccomplex.gui.table.GuiTable;
import net.minecraft.client.gui.GuiButton;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by lukas on 02.06.14.
 */
public class TableCellPresetAction extends TableCellDefault
{
    protected GuiButton leftButton;
    protected GuiButton rightButton;

    protected String currentActionID;

    protected List<TableCellButton> actions = new ArrayList<>();

    public TableCellPresetAction(String id, List<TableCellButton> actions)
    {
        super(id);
        this.actions.addAll(actions);
        if (actions.size() > 0)
            setCurrentAction(actions.get(0).actionID);
    }

    public static Stream<TableCellButton> sorted(Stream<TableCellButton> actions)
    {
        return actions.sorted((o1, o2) -> o1.title.compareTo(o2.title));
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
        int directionButtonWidth = 20;
        int presetButtonWidth = bounds.getWidth() - directionButtonWidth * 2;

        leftButton = new GuiButton(-1, bounds.getMinX(), buttonY, directionButtonWidth - 1, 20, "<");
        leftButton.visible = !isHidden();
        screen.addButton(this, 0, leftButton);

        rightButton = new GuiButton(-1, bounds.getMinX() + directionButtonWidth + presetButtonWidth + 1, buttonY, directionButtonWidth - 1, 20, ">");
        leftButton.visible = !isHidden();
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
        TableCellButton action = findAction(currentActionID);
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

    public void setCurrentAction(String action)
    {
        currentActionID = action;

        actions.forEach(a -> a.setHidden(true));

        TableCellButton actionB = findAction(currentActionID);
        if (actionB != null)
            actionB.setHidden(isHidden());
    }

    public void move(int plus)
    {
        setCurrentAction(actions.get((((findIndex(currentActionID) + plus) % actions.size()) + actions.size()) % actions.size()).actionID);
    }

    @Override
    public void draw(GuiTable screen, int mouseX, int mouseY, float partialTicks)
    {
        super.draw(screen, mouseX, mouseY, partialTicks);

        TableCellButton action = findAction(currentActionID);
        if (action != null)
            action.draw(screen, mouseX, mouseY, partialTicks);
    }

    @Override
    public void drawFloating(GuiTable screen, int mouseX, int mouseY, float partialTicks)
    {
        super.drawFloating(screen, mouseX, mouseY, partialTicks);

        TableCellButton action = findAction(currentActionID);
        if (action != null)
            action.drawFloating(screen, mouseX, mouseY, partialTicks);
    }

    protected TableCellButton findAction(String actionID)
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
