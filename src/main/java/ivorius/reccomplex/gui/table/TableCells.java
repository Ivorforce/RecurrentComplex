/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.cell.TableCellButton;
import ivorius.reccomplex.gui.table.cell.TableCellPresetAction;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by lukas on 28.02.15.
 */

@SideOnly(Side.CLIENT)
public class TableCells
{
    public static Float toFloat(Double value)
    {
        return value != null ? (float) (double) value : null;
    }

    public static Double toDouble(Float value)
    {
        return value != null ? (double) value : null;
    }

    public static void reloadExcept(TableDelegate delegate, String... cellIDs)
    {
        for (String cell : cellIDs)
            delegate.setLocked(cell, true);

        delegate.reloadData();

        for (String cell : cellIDs)
            delegate.setLocked(cell, false);
    }

    @Nonnull
    public static TableCellButton edit(boolean enabled, TableNavigator navigator, TableDelegate tableDelegate, Supplier<TableDataSource> table)
    {
        TableCellButton edit = new TableCellButton(null, "edit", IvTranslations.get("reccomplex.gui.edit"), enabled)
        {
            @Override
            public void draw(GuiTable screen, int mouseX, int mouseY, float partialTicks)
            {
                super.draw(screen, mouseX, mouseY, partialTicks);

                int color = 0xe0e0e0;

                if (!this.enabled)
                    color = 0xa0a0a0;
                else if (this.button != null && this.button.isMouseOver())
                    color = 0xffffa0;

                String right = "...";
                int rightWidth = getFontRenderer().getStringWidth(right);
                getFontRenderer().drawString(right, bounds().getMaxX() - 8 - rightWidth, bounds().getCenterY() - 4, color, true);
            }
        };
        edit.addAction(() -> navigator.pushTable(new GuiTable(tableDelegate, table.get())));
        return edit;
    }

    @Nonnull
    public static List<TableCellButton> addManyWithBase(Collection<String> ids, String baseKey, boolean enabled)
    {
        return TableCellPresetAction.sorted(ids.stream().map(type ->
                add(enabled, type, IvTranslations.get(baseKey + type), getTooltip(baseKey, type)))).collect(Collectors.toList());
    }

    @NotNull
    public static List<String> getTooltip(String baseKey, String type)
    {
        return IvTranslations.formatLines(baseKey + type + ".tooltip");
    }

    @Nonnull
    public static TableCellButton add(final boolean enabled, final String id, final String title, final List<String> tooltip)
    {
        return new TableCellButton(id, id,
                title,
                tooltip,
                enabled)
        {
            @Override
            public void draw(GuiTable screen, int mouseX, int mouseY, float partialTicks)
            {
                super.draw(screen, mouseX, mouseY, partialTicks);

                int color = 0x34D546;
                if (!this.enabled)
                    color = 0x889A8E;
                else if (this.button != null && this.button.isMouseOver())
                    color = 0x3BF350;

                String plus = "+";
                int plusWidth = getFontRenderer().getStringWidth(plus);
                getFontRenderer().drawString(plus, bounds().getMinX() + 6, bounds().getCenterY() - 4, color, true);
                getFontRenderer().drawString(plus, bounds().getMaxX() - 6 - plusWidth, bounds().getCenterY() - 4, color, true);
            }
        };
    }
}
