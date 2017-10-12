/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.preset;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.files.loading.LeveledRegistry;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.table.TableCells;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCellMultiBuilder;
import ivorius.reccomplex.gui.table.cell.TableCellString;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSupplied;
import ivorius.reccomplex.utils.presets.PresetRegistry;
import ivorius.reccomplex.utils.presets.PresettedObject;
import ivorius.reccomplex.world.gen.feature.structure.Structures;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.stream.Collectors;

/**
 * Created by lukas on 20.09.16.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceSavePreset<T> extends TableDataSourceSegmented
{
    public PresettedObject<T> object;
    public String saverID;

    public String id = "";
    public String title = "";
    public String description = "";

    public TableDelegate delegate;
    public TableNavigator navigator;

    public TableDataSourceSavePreset(PresettedObject<T> object, String saverID, TableDelegate delegate, TableNavigator navigator)
    {
        this.object = object;
        this.saverID = saverID;
        this.delegate = delegate;
        this.navigator = navigator;

        addSegment(0, new TableDataSourceSupplied(() ->
        {
            TableCellString cell = new TableCellString(null, id);
            cell.setShowsValidityState(true);
            cell.setValidityState(currentIDState());
            cell.addListener(s ->
            {
                id = s;
                cell.setValidityState(currentIDState());
                if (object.getPresetRegistry().has(s))
                {
                    title = object.getPresetRegistry().title(s).orElse("");
                    description = object.getPresetRegistry().description(s)
                            .flatMap(d -> d.stream().reduce((s1, s2) -> s1 + "<br>" + s2))
                            .orElse("");
                    TableCells.reloadExcept(delegate, "presetID");
                }
            });
            return new TitledCell("presetID", IvTranslations.get("reccomplex.preset.id"), cell)
                    .withTitleTooltip(IvTranslations.getLines("reccomplex.preset.id.tooltip"));
        }, () ->
        {
            TableCellString cell = new TableCellString(null, title);
            cell.addListener(s -> title = s);
            return new TitledCell(IvTranslations.get("reccomplex.preset.title"), cell)
                    .withTitleTooltip(IvTranslations.getLines("reccomplex.preset.title.tooltip"));
        }, () ->
        {
            TableCellString cell = new TableCellString(null, description);
            cell.addListener(s -> description = s);
            return new TitledCell(IvTranslations.get("reccomplex.preset.description"), cell)
                    .withTitleTooltip(IvTranslations.getLines("reccomplex.preset.description.tooltip").stream()
                            .map(s -> s.replaceAll("<BR>", "<br>")).collect(Collectors.toList()));
        }));
        addSegment(1, TableCellMultiBuilder.create(navigator, delegate)
                .addAction(this::save, () -> IvTranslations.get("reccomplex.gui.save"), null).buildDataSource());
    }

    @Nonnull
    @Override
    public String title()
    {
        return String.format("Save: %s", saverID);
    }

    public boolean save()
    {
        if (!Structures.isSimpleID(id))
            return false;

        object.getPresetRegistry().getRegistry().register(id, "", PresetRegistry.fullPreset(id, object.getContents(), new PresetRegistry.Metadata(title, description.split("<br>"))), true, LeveledRegistry.Level.CUSTOM);

        RecurrentComplex.saver.trySave(ResourceDirectory.ACTIVE.toPath(), saverID, id);

        navigator.popTable();
        object.setPreset(id);

        return true;
    }

    private GuiValidityStateIndicator.State currentIDState()
    {
        return Structures.isSimpleID(id)
                ? object.getPresetRegistry().has(id)
                ? GuiValidityStateIndicator.State.SEMI_VALID
                : GuiValidityStateIndicator.State.VALID
                : GuiValidityStateIndicator.State.INVALID;
    }
}
