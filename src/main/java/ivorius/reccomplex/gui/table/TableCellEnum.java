/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import ivorius.ivtoolkit.tools.IvGsonHelper;
import ivorius.reccomplex.utils.IvTranslations;
import net.minecraft.client.gui.GuiButton;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by lukas on 02.06.14.
 */
public class TableCellEnum<T> extends TableCellPropertyDefault<T>
{
    private GuiButton button;

    private List<Option<T>> options;

    public TableCellEnum(String id, T value, List<Option<T>> options)
    {
        super(id, value);
        this.options = Lists.newArrayList(options);
    }

    @SafeVarargs
    public TableCellEnum(String id, T value, Option<T>... options)
    {
        this(id, value, Arrays.asList(options));
    }

    public static <T extends Enum> List<Option<T>> options(List<T> values, final String baseKey, boolean tooltip)
    {
        return options(values, new Function<T, String>()
        {
            @Nullable
            @Override
            public String apply(@Nullable T input)
            {
                return IvTranslations.get(baseKey + IvGsonHelper.serializedName(input));
            }
        }, tooltip ? new Function<T, List<String>>()
        {
            @Nullable
            @Override
            public List<String> apply(@Nullable T input)
            {
                return IvTranslations.getLines(baseKey + IvGsonHelper.serializedName(input) + ".tooltip");
            }
        } : null);
    }

    public static <T extends Enum> List<Option<T>> options(List<T> values, final Function<T, String> titleFunc, final Function<T, List<String>> tooltipFunc)
    {
        return Lists.transform(values, new Function<T, Option<T>>()
        {
            @Nullable
            @Override
            public Option<T> apply(T input)
            {
                return new Option<>(input, titleFunc != null ? titleFunc.apply(input) : null, tooltipFunc != null ? tooltipFunc.apply(input) : null);
            }
        });
    }

    @Override
    public void initGui(GuiTable screen)
    {
        super.initGui(screen);

        Bounds bounds = bounds();
        button = new GuiButton(-1, bounds.getMinX(), bounds.getMinY() + (bounds.getHeight() - 20) / 2, bounds.getWidth(), 20, "");

        button.visible = !isHidden();
        updateButtonTitle();

        screen.addButton(this, 0, button);
    }

    @Override
    public void setHidden(boolean hidden)
    {
        super.setHidden(hidden);

        if (button != null)
            button.visible = !hidden;
    }

    @Override
    public void setPropertyValue(T value)
    {
        super.setPropertyValue(value);

        updateButtonTitle();
    }

    @Override
    public void buttonClicked(int buttonID)
    {
        super.buttonClicked(buttonID);

        int prevOptionIndex = currentOptionIndex();
        int optionIndex = prevOptionIndex < 0 ? 0 : (prevOptionIndex + 1) % options.size();

        setPropertyValue(options.get(optionIndex).value);

        alertListenersOfChange();
    }

    @Override
    public void drawFloating(GuiTable screen, int mouseX, int mouseY, float partialTicks)
    {
        super.drawFloating(screen, mouseX, mouseY, partialTicks);

        Option<T> option = currentOption();
        if (option != null && option.tooltip != null)
            screen.drawTooltipRect(option.tooltip, bounds(), mouseX, mouseY, getFontRenderer());
    }

    private void updateButtonTitle()
    {
        if (button != null)
        {
            int index = currentOptionIndex();
            button.displayString = index >= 0 ? options.get(index).title
                    : getPropertyValue() != null
                    ? getPropertyValue().toString()
                    : "null";
        }
    }

    private Option<T> currentOption()
    {
        int index = currentOptionIndex();
        return index >= 0 ? options.get(index) : null;
    }

    private int currentOptionIndex()
    {
        for (int i = 0; i < options.size(); i++)
        {
            if (Objects.equals(options.get(i).value, getPropertyValue()))
                return i;
        }

        return -1;
    }

    public static class Option<T>
    {
        public T value;
        public String title;
        public List<String> tooltip;

        public Option(T value, String title)
        {
            this.value = value;
            this.title = title;
        }

        public Option(T value, String title, List<String> tooltip)
        {
            this.value = value;
            this.title = title;
            this.tooltip = tooltip;
        }
    }
}
