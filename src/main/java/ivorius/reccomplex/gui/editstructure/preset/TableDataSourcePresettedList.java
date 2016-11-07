/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.preset;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCellButton;
import ivorius.reccomplex.gui.table.cell.TableCellPresetAction;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceList;
import ivorius.reccomplex.utils.presets.PresettedList;
import ivorius.reccomplex.utils.presets.PresettedObject;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lukas on 04.06.14.
 */
public abstract class TableDataSourcePresettedList<T> extends TableDataSourceList<T, List<T>>
{
    public PresettedObject presettedObject;

    public TableDataSourcePresettedList(PresettedObject object, List<T> list, TableDelegate delegate, TableNavigator navigator)
    {
        super(list, delegate, navigator);
        this.presettedObject = object;
    }

    public TableDataSourcePresettedList(PresettedList<T> list, TableDelegate delegate, TableNavigator navigator)
    {
        super(list.getContents(), delegate, navigator);
        this.presettedObject = list;
    }

    @Nonnull
    public static List<TableCellButton> addActions(Collection<String> ids, String baseKey, boolean enabled)
    {
        return TableCellPresetAction.sorted(ids.stream().map(type ->
        {
            String key = baseKey + type;
            return new TableCellButton(type, type,
                    IvTranslations.get(key),
                    IvTranslations.formatLines(key + ".tooltip"),
                    enabled){
                @Override
                public void draw(GuiTable screen, int mouseX, int mouseY, float partialTicks)
                {
                    super.draw(screen, mouseX, mouseY, partialTicks);

                    String plus = TextFormatting.GREEN + "+";
                    int plusWidth = getFontRenderer().getStringWidth(plus);
                    getFontRenderer().drawString(plus, bounds().getMinX() + 6, bounds().getCenterY() - 4, 0xffffffff, true);
                    getFontRenderer().drawString(plus, bounds().getMaxX() - 6 - plusWidth, bounds().getCenterY() - 4, 0xffffffff, true);
                }
            };
        })).collect(Collectors.toList());
    }

    @Override
    public boolean canEditList()
    {
        return presettedObject.isCustom();
    }
}
