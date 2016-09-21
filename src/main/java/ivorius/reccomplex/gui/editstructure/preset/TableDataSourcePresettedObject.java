/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.preset;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.utils.PresetRegistry;
import ivorius.reccomplex.utils.presets.PresettedObject;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 19.09.16.
 */
public class TableDataSourcePresettedObject<T> extends TableDataSourceSegmented
{
    public TableDelegate delegate;
    public TableNavigator navigator;
    public PresettedObject<T> object;
    public Runnable applyPresetAction;

    public TableDataSourcePresettedObject(PresettedObject<T> object, TableDelegate delegate, TableNavigator navigator)
    {
        this.object = object;

        this.delegate = delegate;
        this.navigator = navigator;
    }

    @Nonnull
    public static <T> TableElement getCustomizeElement(PresettedObject<T> object, TableDelegate delegate, TableNavigator navigator)
    {
        if (!object.isCustom())
        {
            @SuppressWarnings("OptionalGetWithoutIsPresent")
            String title = !object.isCustom() ? object.presetTitle().get() : IvTranslations.get("reccomplex.gui.custom");
            TableCellButton cell = new TableCellButton("customize", "customize", IvTranslations.get("reccomplex.gui.customize"), true);
            cell.addAction(() ->
            {
                object.setToCustom();
                delegate.reloadData();
            });
            return new TableElementCell(title, cell);
        }
        else
        {
            return TableCellMultiBuilder.create(navigator, delegate)
                    .addNavigation(() -> IvTranslations.get("reccomplex.preset.save"), null,
                            () -> new TableDataSourceSavePreset<>(object, delegate, navigator)
                    ).buildElement();
        }
    }

    @Nonnull
    public static <T> TableElement getSetElement(PresettedObject<T> object, TableDelegate delegate, TableCellButton[] actions, Runnable applyPresetAction)
    {
        if (actions.length == 0)
            return new TableElementCell(new TableCellButton(null, null, IvTranslations.get("reccomplex.presets"), false));

        TableCellPresetAction cell = new TableCellPresetAction("preset", IvTranslations.get("reccomplex.gui.apply"), actions);
        cell.addAction((actionID) ->
        {
            object.setPreset(actionID);
            if (applyPresetAction != null)
                applyPresetAction.run();
            delegate.reloadData();
        });
        return new TableElementCell(IvTranslations.get("reccomplex.presets"), cell);
    }

    @Nonnull
    public static <T> TableCellButton[] getPresetActions(PresettedObject<T> object)
    {
        PresetRegistry<T> registry = object.getPresetRegistry();
        //noinspection OptionalGetWithoutIsPresent
        return registry.allIDs().stream().map(type -> new TableCellButton(type, type,
                registry.title(type).orElse(type),
                registry.description(type).orElse(null)
        )).toArray(TableCellButton[]::new);
    }

    public TableDataSourcePresettedObject<T> withApplyPresetAction(Runnable applyPresetAction)
    {
        this.applyPresetAction = applyPresetAction;
        return this;
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
                return getSetElement(object, delegate, getPresetActions(), applyPresetAction);
            else if (index == 1)
                return getCustomizeElement(object, delegate, navigator);
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    public TableCellButton[] getPresetActions()
    {
        return getPresetActions(object);
    }
}
