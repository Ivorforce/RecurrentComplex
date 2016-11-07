/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.cell;

import com.google.common.collect.Lists;
import ivorius.ivtoolkit.tools.IvGsonHelper;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.Bounds;
import ivorius.reccomplex.gui.table.GuiTable;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by lukas on 02.06.14.
 */
public class TableCellEnum<T> extends TableCellPropertyDefault<T>
{
    protected GuiButton leftButton;
    protected GuiButton rightButton;

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

    public static <T extends Enum> List<Option<T>> options(T[] values, final String baseKey, boolean tooltip)
    {
        return options(Arrays.asList(values), baseKey, tooltip);
    }

    public static <T> List<Option<T>> options(T[] values, final Function<T, String> titleFunc, final Function<T, List<String>> tooltipFunc)
    {
        return options(Arrays.asList(values), titleFunc, tooltipFunc);
    }

    public static <T extends Enum> List<Option<T>> options(List<T> values, final String baseKey, boolean tooltip)
    {
        return options(values, input -> IvTranslations.get(baseKey + IvGsonHelper.serializedName(input)), tooltip ? (Function<T, List<String>>) input -> IvTranslations.getLines(baseKey + IvGsonHelper.serializedName(input) + ".tooltip") : null);
    }

    public static <T> List<Option<T>> options(List<T> values, final Function<T, String> titleFunc, final Function<T, List<String>> tooltipFunc)
    {
        return values.stream()
                .map(input -> new Option<>(input, titleFunc != null ? titleFunc.apply(input) : null, tooltipFunc != null ? tooltipFunc.apply(input) : null))
                .sorted((o1, o2) -> o1.title.compareTo(o2.title))
                .collect(Collectors.toList());
    }

    @Override
    public void initGui(GuiTable screen)
    {
        super.initGui(screen);

        Bounds bounds = bounds();

        int buttonY = bounds.getMinY() + (bounds.getHeight() - 20) / 2;
        int presetButtonWidth = bounds.getWidth() - TableCellPresetAction.DIRECTION_BUTTON_WIDTH * 2;

        leftButton = new GuiButton(-1, bounds.getMinX(), buttonY, TableCellPresetAction.DIRECTION_BUTTON_WIDTH - 1, 20, "<");
        leftButton.visible = !isHidden();
        screen.addButton(this, 0, leftButton);

        rightButton = new GuiButton(-1, bounds.getMinX() + TableCellPresetAction.DIRECTION_BUTTON_WIDTH + presetButtonWidth + 1, buttonY, TableCellPresetAction.DIRECTION_BUTTON_WIDTH - 1, 20, ">");
        leftButton.visible = !isHidden();
        screen.addButton(this, 1, rightButton);
    }

    @Override
    public void setHidden(boolean hidden)
    {
        super.setHidden(hidden);

        if (leftButton != null)
            leftButton.visible = !hidden;
        if (rightButton != null)
            rightButton.visible = !hidden;
    }

    @Override
    public void buttonClicked(int buttonID)
    {
        super.buttonClicked(buttonID);

        if (buttonID == 0 || buttonID == 1)
            move(buttonID == 0 ? -1 : 1);
    }

    public void move(int plus)
    {
        setPropertyValue(options.get((((findIndex(getPropertyValue()) + plus) % options.size()) + options.size()) % options.size()).value);
        alertListenersOfChange();
    }

    @Override
    public void draw(GuiTable screen, int mouseX, int mouseY, float partialTicks)
    {
        FontRenderer fontRenderer = getFontRenderer();
        Bounds bounds = bounds();

        Option<T> cOption = currentOption();
        String option = cOption != null ? cOption.title : getPropertyValue().toString();

        int width = fontRenderer.getStringWidth(option);
        fontRenderer.drawString(option, bounds.getCenterX() - width / 2, bounds.getCenterY() - 4, 0xffffffff, true);

        super.draw(screen, mouseX, mouseY, partialTicks);
    }

    @Override
    public void drawFloating(GuiTable screen, int mouseX, int mouseY, float partialTicks)
    {
        super.drawFloating(screen, mouseX, mouseY, partialTicks);

        Option<T> option = currentOption();
        if (option != null && option.tooltip != null)
        {
            Bounds bounds = bounds();
            screen.drawTooltipRect(option.tooltip, Bounds.fromSize(bounds.getMinX() + TableCellPresetAction.DIRECTION_BUTTON_WIDTH, bounds.getMinY(), bounds.getWidth() - TableCellPresetAction.DIRECTION_BUTTON_WIDTH * 2, bounds.getHeight()), mouseX, mouseY, getFontRenderer());
        }
    }

    private Option<T> currentOption()
    {
        int index = findIndex(getPropertyValue());
        return index >= 0 ? options.get(index) : null;
    }

    protected int findIndex(T option)
    {
        for (int i = 0; i < options.size(); i++)
        {
            if (Objects.equals(options.get(i).value, option))
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
