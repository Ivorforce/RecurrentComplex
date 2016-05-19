/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import net.minecraft.client.gui.GuiTextField;

/**
 * Created by lukas on 02.06.14.
 */
public class TableCellString extends TableCellPropertyDefault<String>
{
    protected GuiTextField textField;
    protected GuiValidityStateIndicator stateIndicator;

    protected boolean showsValidityState;
    protected GuiValidityStateIndicator.State validityState;

    protected int maxStringLength = 300;

    public TableCellString(String id, String value)
    {
        super(id, value);
    }

    public int getMaxStringLength()
    {
        return maxStringLength;
    }

    public void setMaxStringLength(int maxStringLength)
    {
        this.maxStringLength = maxStringLength;
        if (textField != null)
            textField.setMaxStringLength(maxStringLength);
    }

    @Override
    public void initGui(GuiTable screen)
    {
        super.initGui(screen);

        Bounds bounds = bounds();
        textField = new GuiTextField(0, getFontRenderer(), bounds.getMinX(), bounds.getMinY() + (bounds.getHeight() - 20) / 2, bounds.getWidth() - (showsValidityState ? 15 : 0), 20);
        textField.setMaxStringLength(maxStringLength);

        textField.setText(getPropertyValue());
        textField.setVisible(!isHidden());

        if (showsValidityState)
        {
            stateIndicator = new GuiValidityStateIndicator(bounds.getMinX() + bounds.getWidth() - 10, bounds.getMinY() + (bounds.getHeight() - 10) / 2, validityState);
            stateIndicator.setVisible(!isHidden());
        }
        else
        {
            stateIndicator = null;
        }
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

        String text = textField.getText();
        boolean used = textField.textboxKeyTyped(keyChar, keyCode);
        property = textField.getText();

        if (!text.equals(property))
            alertListenersOfChange();

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
    public void setPropertyValue(String value)
    {
        super.setPropertyValue(value);

        if (textField != null)
            textField.setText(value);
    }

    public GuiValidityStateIndicator.State getValidityState()
    {
        return validityState;
    }

    public void setValidityState(GuiValidityStateIndicator.State validityState)
    {
        this.validityState = validityState;

        if (stateIndicator != null)
            stateIndicator.setState(validityState);
    }

    public boolean showsValidityState()
    {
        return showsValidityState;
    }

    public void setShowsValidityState(boolean showsValidityState)
    {
        this.showsValidityState = showsValidityState;
    }
}
