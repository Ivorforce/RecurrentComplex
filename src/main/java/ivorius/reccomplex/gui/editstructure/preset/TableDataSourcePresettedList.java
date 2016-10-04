/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.preset;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.cell.TableCellButton;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceList;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.utils.presets.PresettedList;
import ivorius.reccomplex.utils.presets.PresettedObject;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    public static TableCellButton[] addActions(Collection<String> ids, String baseKey, boolean enabled)
    {
        List<TableCellButton> actions = new ArrayList<>(ids.size());
        for (String type : ids)
        {
            String key = baseKey + type;
            actions.add(new TableCellButton(type, type,
                    IvTranslations.get(key),
                    IvTranslations.formatLines(key + ".tooltip"),
                    enabled
            ));
        }
        return actions.toArray(new TableCellButton[actions.size()]);
    }

    @Override
    public boolean canEditList()
    {
        return presettedObject.isCustom();
    }
}
