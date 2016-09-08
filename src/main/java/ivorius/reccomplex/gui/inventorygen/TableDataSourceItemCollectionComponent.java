/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.inventorygen;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.RCGuiHandler;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.TableElementSaveDirectory;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.utils.SaveDirectoryData;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollection;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollectionRegistry;
import ivorius.reccomplex.worldgen.inventory.WeightedItemCollectionRegistry;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Set;

/**
 * Created by lukas on 27.08.16.
 */
public class TableDataSourceItemCollectionComponent extends TableDataSourceSegmented
{
    public String key;
    public GenericItemCollection.Component component;

    private SaveDirectoryData saveDirectoryData;

    public EntityPlayer player;

    public TableNavigator navigator;
    public TableDelegate delegate;

    public TableDataSourceItemCollectionComponent(String key, GenericItemCollection.Component component, SaveDirectoryData saveDirectoryData, EntityPlayer player, TableNavigator navigator, TableDelegate delegate)
    {
        this.key = key;
        this.component = component;
        this.saveDirectoryData = saveDirectoryData;
        this.player = player;
        this.navigator = navigator;
        this.delegate = delegate;

        addManagedSection(1, new TableDataSourceSupplied(() -> TableElementSaveDirectory.create(saveDirectoryData, () -> key, delegate)));
        addManagedSection(3, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.inventorygen.dependencies"), this.component.dependencies, null));
        addManagedSection(4, TableCellMultiBuilder.create(navigator, delegate)
                .addAction(() -> IvTranslations.get("reccomplex.gui.edit"), null,
                        () -> RCGuiHandler.editInventoryGenComponentItems(this.player, this.key, this.component, this.saveDirectoryData))
                .buildDataSource(IvTranslations.format("reccomplex.gui.inventorygen.items.summary", String.valueOf(this.component.items.size()))));
    }

    public SaveDirectoryData getSaveDirectoryData()
    {
        return saveDirectoryData;
    }

    public void setSaveDirectoryData(SaveDirectoryData saveDirectoryData)
    {
        this.saveDirectoryData = saveDirectoryData;
    }

    @Override
    public int numberOfSegments()
    {
        return 5;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 0:
            case 2:
                return 1;
            default:
                return super.sizeOfSegment(segment);
        }
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            TableCellString cell = new TableCellString(null, key);
            cell.setTooltip(IvTranslations.formatLines("reccomplex.gui.inventorygen.componentid.tooltip"));
            cell.setShowsValidityState(true);
            cell.setValidityState(currentKeyState());
            cell.addPropertyConsumer(val ->
            {
                key = val;
                cell.setValidityState(currentKeyState());
            });
            return new TableElementCell(IvTranslations.get("reccomplex.gui.inventorygen.componentid"), cell);
        }
        else if (segment == 2)
        {
            TableCellString cell = new TableCellString(null, component.inventoryGeneratorID);
            cell.setTooltip(IvTranslations.formatLines("reccomplex.gui.inventorygen.groupid.tooltip"));
            cell.setShowsValidityState(true);
            cell.setValidityState(currentGroupIDState());
            cell.addPropertyConsumer(val ->
            {
                component.inventoryGeneratorID = val;
                cell.setValidityState(currentGroupIDState());
            });
            return new TableElementCell(IvTranslations.get("reccomplex.gui.inventorygen.groupid"), cell);
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    private GuiValidityStateIndicator.State currentKeyState()
    {
        return GenericItemCollectionRegistry.INSTANCE.allComponentKeys().contains(key)
                ? GuiValidityStateIndicator.State.SEMI_VALID : GuiValidityStateIndicator.State.VALID;
    }

    private GuiValidityStateIndicator.State currentGroupIDState()
    {
        return WeightedItemCollectionRegistry.itemCollection(component.inventoryGeneratorID) instanceof GenericItemCollection
                ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.SEMI_VALID;
    }
}
