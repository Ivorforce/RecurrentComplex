/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.preset;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.utils.presets.PresettedObject;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lukas on 19.09.16.
 */
public abstract class TableDataSourcePresettedObject<T> extends TableDataSourceSegmented
{
    public TableDelegate delegate;
    public TableNavigator navigator;
    PresettedObject<T> object;

    public TableDataSourcePresettedObject(PresettedObject<T> object, TableDelegate delegate, TableNavigator navigator)
    {
        this.object = object;

        this.delegate = delegate;
        this.navigator = navigator;
    }

    @Nonnull
    public static <T> TableElement getCustomizeElement(PresettedObject<T> object, TableDelegate delegate, String basePresetKey)
    {
        String title = !object.isCustom() ? IvTranslations.get(basePresetKey + object.getPreset()) : IvTranslations.get("reccomplex.gui.custom");
        TableCellButton cell = new TableCellButton("customize", "customize", IvTranslations.get("reccomplex.gui.customize"), !object.isCustom());
        cell.addAction(() ->
        {
            object.setToCustom();
            delegate.reloadData();
        });
        return new TableElementCell(title, cell);
    }

    @Nonnull
    public static <T> TableElement getSetElement(PresettedObject<T> object, TableDelegate delegate, TableCellButton[] actions)
    {
        TableCellPresetAction cell = new TableCellPresetAction("preset", IvTranslations.get("reccomplex.gui.apply"), actions);
        cell.addAction((actionID) ->
        {
            object.setPreset(actionID);
            delegate.reloadData();
        });
        return new TableElementCell(IvTranslations.get("reccomplex.gui.presets"), cell);
    }

    @Nonnull
    public static <T> TableCellButton[] getPresetActions(PresettedObject<T> object, String basePresetKey)
    {
        Collection<String> allTypes = object.getPresetRegistry().allIDs();
        List<TableCellButton> actions = new ArrayList<>(allTypes.size());

        actions.addAll(allTypes.stream().map(type -> new TableCellButton(type, type,
                IvTranslations.get(basePresetKey + type),
                IvTranslations.formatLines(basePresetKey + type + ".tooltip")
        )).collect(Collectors.toList()));
        return actions.toArray(new TableCellButton[actions.size()]);
    }

    @Override
    public int numberOfSegments()
    {
        return 1;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return segment == 0 ? 2 : super.sizeOfSegment(segment);
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            if (index == 0)
                return getSetElement(object, delegate, getPresetActions());
            else if (index == 1)
                return getCustomizeElement(object, delegate, getBasePresetKey());
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    public abstract String getBasePresetKey();

    public TableCellButton[] getPresetActions()
    {
        return getPresetActions(object, getBasePresetKey());
    }
}
