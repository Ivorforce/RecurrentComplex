/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.cell;

import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.table.Bounds;
import ivorius.reccomplex.gui.table.GuiTable;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * Created by lukas on 02.06.14.
 */
@SideOnly(Side.CLIENT)
public abstract class TableCellTextField<T> extends TableCellPropertyDefault<T>
{
    @Nullable
    protected final GuiTextField textField;
    @Nullable
    protected final GuiValidityStateIndicator stateIndicator;

    protected boolean showsValidityState;
    protected boolean validDataType = true;
    protected GuiValidityStateIndicator.State validityState = GuiValidityStateIndicator.State.VALID;

    @Nullable
    protected Runnable changeListener;

    public TableCellTextField(String id, T value)
    {
        super(id, value);
        textField = new GuiTextField(-1, getFontRenderer(), 0, 0, 0, 0);
        textField.setMaxStringLength(32500); // From command blocks
        stateIndicator = new GuiValidityStateIndicator(0, 0, GuiValidityStateIndicator.State.UNKNWON);
        setPropertyValue(value);
        updateValidityStateIndicator();
    }

    public int getMaxStringLength()
    {
        return textField.getMaxStringLength();
    }

    public void setMaxStringLength(int maxStringLength)
    {
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

    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        textField.setEnabled(enabled);
    }

    protected abstract String serialize(T t);

    @Nullable
    protected abstract T deserialize(String string);

    @Override
    public void draw(GuiTable screen, int mouseX, int mouseY, float partialTicks)
    {
        super.draw(screen, mouseX, mouseY, partialTicks);

        textField.drawTextBox();
        if (showsValidityState)
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

        textField.setVisible(!hidden);
        stateIndicator.setVisible(!hidden && showsValidityState);
    }

    @Override
    public void setPropertyValue(T t)
    {
        super.setPropertyValue(t);

        if (textField != null) textField.setText(serialize(t)); // super calls this in init
        if (changeListener != null)
            changeListener.run();
    }

    @Override
    public void setBounds(Bounds bounds)
    {
        super.setBounds(bounds);

        Bounds.set(textField, Bounds.fromSize(bounds.getMinX() + 2, bounds.getCenterY() - 9, bounds.getWidth() - (showsValidityState() ? 14 : 0) - 4, 18));
        stateIndicator.xPosition = bounds.getMaxX() - 12;
        stateIndicator.yPosition = bounds.getCenterY() - 5;
    }

    protected void updateValidityStateIndicator()
    {
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
        setBounds(bounds());
        setHidden(isHidden());
    }
}
