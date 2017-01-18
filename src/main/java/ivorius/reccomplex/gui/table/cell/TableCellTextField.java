/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.cell;

import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.table.Bounds;
import ivorius.reccomplex.gui.table.GuiTable;
import net.minecraft.client.gui.GuiTextField;

import javax.annotation.Nullable;

/**
 * Created by lukas on 02.06.14.
 */
public abstract class TableCellTextField<T> extends TableCellPropertyDefault<T>
{
    @Nullable
    protected GuiTextField textField;
    @Nullable
    protected GuiValidityStateIndicator stateIndicator;

    protected boolean showsValidityState;
    protected boolean validDataType = true;
    protected GuiValidityStateIndicator.State validityState = GuiValidityStateIndicator.State.VALID;

    protected int maxStringLength = 300;

    @Nullable
    protected Runnable changeListener;

    protected boolean enabled = true;

    public TableCellTextField(String id, T value)
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

    @Nullable
    public Runnable getChangeListener()
    {
        return changeListener;
    }

    public void setChangeListener(@Nullable Runnable changeListener)
    {
        this.changeListener = changeListener;
    }

    @Nullable
    public GuiTextField getTextField()
    {
        return textField;
    }

    @Nullable
    public GuiValidityStateIndicator getStateIndicator()
    {
        return stateIndicator;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        if (textField != null)
            textField.setEnabled(enabled);
    }

    @Override
    public void initGui(GuiTable screen)
    {
        super.initGui(screen);

        Bounds bounds = bounds();
        if (textField == null)
            textField = new GuiTextField(0, getFontRenderer(), 0, 0, 0, 0);
        updateTextFieldBounds(bounds);
        textField.setMaxStringLength(maxStringLength);

        textField.setText(serialize(getPropertyValue()));
        textField.setVisible(!isHidden());
        textField.setEnabled(enabled);

        if (showsValidityState())
        {
            stateIndicator = new GuiValidityStateIndicator(bounds.getMaxX() - 12, bounds.getCenterY() - 5, GuiValidityStateIndicator.State.UNKNWON);
            updateValidityStateIndicator();
            stateIndicator.setVisible(!isHidden());
        }
        else
        {
            stateIndicator = null;
        }
    }

    protected abstract String serialize(T t);

    @Nullable
    protected abstract T deserialize(String string);

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

        T cur = deserialize(textField.getText());
        if (cur != null)
        {
            validDataType = true;
            T prev = property;
            property = cur;

            if (!cur.equals(prev))
                alertListenersOfChange();
            if (changeListener != null)
                changeListener.run();
        }
        else
            validDataType = false;

        updateValidityStateIndicator();

        return used;
    }

    @Override
    public void mouseClicked(int button, int x, int y)
    {
        super.mouseClicked(button, x, y);

        textField.mouseClicked(x, y, button);
        if (changeListener != null)
            changeListener.run();
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
    public void setPropertyValue(T t)
    {
        super.setPropertyValue(t);

        if (textField != null)
            textField.setText(serialize(t));
        if (changeListener != null)
            changeListener.run();
    }

    protected void updateTextFieldBounds(Bounds bounds)
    {
        if (textField != null)
            Bounds.set(textField, Bounds.fromSize(bounds.getMinX() + 2, bounds.getCenterY() - 9, bounds.getWidth() - (showsValidityState() ? 14 : 0) - 4, 18));
    }

    @Override
    public void setBounds(Bounds bounds)
    {
        super.setBounds(bounds);

        updateTextFieldBounds(bounds);
    }

    protected void updateValidityStateIndicator()
    {
        if (stateIndicator != null)
            stateIndicator.setState(!validDataType ? GuiValidityStateIndicator.State.INVALID : validityState);
    }

    public GuiValidityStateIndicator.State getValidityState()
    {
        return validityState;
    }

    public void setValidityState(GuiValidityStateIndicator.State validityState)
    {
        this.validityState = validityState;
        updateValidityStateIndicator();
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
