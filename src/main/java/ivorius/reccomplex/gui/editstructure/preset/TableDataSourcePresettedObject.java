/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.preset;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.*;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.utils.PresetRegistry;
import ivorius.reccomplex.utils.presets.PresettedObject;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lukas on 19.09.16.
 */
public class TableDataSourcePresettedObject<T> extends TableDataSourceSegmented
{
    public TableDelegate delegate;
    public TableNavigator navigator;

    public PresettedObject<T> object;
    public String saverID;

    public Runnable applyPresetAction;

    public TableDataSourcePresettedObject(PresettedObject<T> object, String saverID, TableDelegate delegate, TableNavigator navigator)
    {
        this.object = object;
        this.saverID = saverID;

        this.delegate = delegate;
        this.navigator = navigator;
    }

    @Nonnull
    public static <T> TableCell getCustomizeElement(PresettedObject<T> object, String saverID, TableDelegate delegate, TableNavigator navigator, Runnable applyPresetAction)
    {
        if (!object.isCustom())
        {
            @SuppressWarnings("OptionalGetWithoutIsPresent")
            TableCellButton cell = new TableCellButton("customize", "customize", TextFormatting.AQUA + "O", true);
            cell.setTooltip(IvTranslations.formatLines("reccomplex.preset.customize", object.presetTitle().get()));
            cell.addAction(() ->
            {
                object.setToCustom();
                if (applyPresetAction != null)
                    applyPresetAction.run();
                delegate.reloadData();
            });
            return cell;
        }
        else
        {
            return TableCellMultiBuilder.create(navigator, delegate)
                    .addNavigation(() -> String.format("%s+", TextFormatting.GREEN), () -> IvTranslations.getLines("reccomplex.preset.save"),
                            () -> new TableDataSourceSavePreset<>(object, saverID, delegate, navigator)
                    ).enabled(() -> saverID != null).build();
        }
    }

    @Nonnull
    public static <T> TableCell getSetElement(PresettedObject<T> object, TableDelegate delegate, List<TableCellButton> actions, Runnable applyPresetAction)
    {
        if (actions.isEmpty())
            return new TableCellButton(null, null, "-", false);

        TableCellPresetAction cell = new TableCellPresetAction("preset", actions);
        cell.addAction((actionID) ->
        {
            object.setPreset(actionID);
            if (applyPresetAction != null)
                applyPresetAction.run();
            delegate.reloadData();
        });
        if (object.getPreset() != null)
            cell.setPropertyValue(object.getPreset());
        return cell;
    }

    @Nonnull
    public static <T> List<TableCellButton> getActions(PresettedObject<T> object)
    {
        PresetRegistry<T> registry = object.getPresetRegistry();
        //noinspection OptionalGetWithoutIsPresent
        return TableCellPresetAction.sorted(registry.allIDs().stream().map(type -> new TableCellButton(type, type,
                IvTranslations.format(type.equals(registry.defaultID()) ? "reccomplex.preset.use.default" : "reccomplex.preset.use", registry.title(type).orElse(type)),
                registry.description(type).orElse(null)
        ))).collect(Collectors.toList());
    }

    public TableDataSourcePresettedObject<T> withApplyPresetAction(Runnable applyPresetAction)
    {
        this.applyPresetAction = applyPresetAction;
        return this;
    }

    public TableDataSourcePresettedObject<T> withCurrentOnTop(boolean currentOnTop)
    {
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
        return segment == 0 ? 1 : super.sizeOfSegment(segment);
    }

    @Override
    public TableCell cellForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            TableCellMulti multi = new TableCellMulti(getSetElement(object, delegate, getActions(), applyPresetAction),
                    getCustomizeElement(object, saverID, delegate, navigator, applyPresetAction));
            multi.setSize(0, 7);
            return new TitledCell(IvTranslations.get("reccomplex.presets"), multi);
        }

        return super.cellForIndexInSegment(table, index, segment);
    }

    public List<TableCellButton> getActions()
    {
        return getActions(object);
    }
}
