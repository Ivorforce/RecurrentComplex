/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.cell;

import com.google.common.primitives.Ints;
import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.table.Bounds;
import ivorius.reccomplex.gui.table.GuiTable;
import net.minecraft.client.gui.GuiTextField;

/**
 * Created by lukas on 02.06.14.
 */
public class TableCellStringInt extends TableCellPropertyDefault<Integer>
{
    protected GuiTextField textField;
    protected GuiValidityStateIndicator stateIndicator;

    public TableCellStringInt(String id, Integer value)
    {
        super(id, value);
    }

    @Override
    public void initGui(GuiTable screen)
    {
        super.initGui(screen);

        Bounds bounds = bounds();
        textField = new GuiTextField(0, getFontRenderer(), bounds.getMinX(), bounds.getMinY() + (bounds.getHeight() - 20) / 2, bounds.getWidth() - 15, 20);
        textField.setMaxStringLength(100);

        textField.setText(getPropertyValue().toString());
        textField.setVisible(!isHidden());

        stateIndicator = new GuiValidityStateIndicator(bounds.getMinX() + bounds.getWidth() - 10, bounds.getMinY() + (bounds.getHeight() - 10) / 2, getValidityState());
        stateIndicator.setVisible(!isHidden());
    }

    protected GuiValidityStateIndicator.State getValidityState()
    {
        return Ints.tryParse(textField.getText()) != null ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.INVALID;
    }

    @Override
    public void draw(GuiTable screen, int mouseX, int mouseY, float partialTicks)
    {
        super.draw(screen, mouseX, mouseY, partialTicks);

        textField.drawTextBox();

        if (stateIndicator != null)
            stateIndicator.draw();
    }

    @Override
    public void update(GuiTable screen)
    {
        super.update(screen);

        textField.updateCursorCounter();
    }

    @Override
    public boolean keyTyped(char keyChar, int keyCode)
    {
        super.keyTyped(keyChar, keyCode);

        boolean used = textField.textboxKeyTyped(keyChar, keyCode);
        Integer parsed = Ints.tryParse(textField.getText());

        stateIndicator.setState(getValidityState());

        if (parsed != null)
        {
            Integer prev = property;
            property = parsed;

            if (!property.equals(prev))
                alertListenersOfChange();
        }

        return used;
    }

    @Override
    public void mouseClicked(int button, int x, int y)
    {
        super.mouseClicked(button, x, y);

        textField.mouseClicked(x, y, button);
    }

    @Override
    public void setHidden(boolean hidden)
    {
        super.setHidden(hidden);

        if (textField != null)
            textField.setVisible(!hidden);

        if (stateIndicator != null)
            stateIndicator.setVisible(!hidden);
    }

    @Override
    public void setPropertyValue(Integer value)
    {
        super.setPropertyValue(value);

        if (textField != null)
            textField.setText(value.toString());
    }
}
