/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.preset;

import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceList;
import ivorius.reccomplex.utils.presets.PresettedList;
import ivorius.reccomplex.utils.presets.PresettedObject;

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

    @Override
    public boolean canEditList()
    {
        return presettedObject.isCustom();
    }
}
