/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.inventorygen;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.RCGuiHandler;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollection;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollectionRegistry;
import ivorius.reccomplex.worldgen.inventory.WeightedItemCollectionRegistry;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Created by lukas on 27.08.16.
 */
public class TableDataSourceEditInventoryGen extends TableDataSourceSegmented
{
    public String key;
    public GenericItemCollection.Component component;

    public EntityPlayer player;

    public TableNavigator navigator;
    public TableDelegate delegate;

    public TableDataSourceEditInventoryGen(String key, GenericItemCollection.Component component, EntityPlayer player, TableNavigator navigator, TableDelegate delegate)
    {
        this.key = key;
        this.component = component;
        this.player = player;
        this.navigator = navigator;
        this.delegate = delegate;

        addManagedSection(1, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.inventorygen.dependencies"), this.component.dependencies));
        addManagedSection(2, TableCellMultiBuilder.create(navigator, delegate)
                .addAction(() -> IvTranslations.get("reccomplex.gui.edit"), null,
                        () -> RCGuiHandler.editInventoryGenComponentItems(this.player, this.key, this.component))
                .buildDataSource(IvTranslations.format("reccomplex.gui.inventorygen.items.summary", String.valueOf(this.component.items.size()))));
    }

    @Override
    public int numberOfSegments()
    {
        return 3;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 0:
                return 2;
            default:
                return super.sizeOfSegment(segment);
        }
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            if (index == 0)
            {
                TableCellString cell = new TableCellString(null, key);
                cell.setTooltip(IvTranslations.formatLines("reccomplex.gui.inventorygen.componentid.tooltip"));
                cell.setShowsValidityState(true);
                cell.setValidityState(currentKeyState());
                cell.addPropertyListener(cell1 ->
                {
                    key = (String) cell1.getPropertyValue();
                    ((TableCellString) cell1).setValidityState(currentKeyState());
                });
                return new TableElementCell(IvTranslations.get("reccomplex.gui.inventorygen.componentid"), cell);
            }
            else if (index == 1)
            {
                TableCellString cell = new TableCellString(null, component.inventoryGeneratorID);
                cell.setTooltip(IvTranslations.formatLines("reccomplex.gui.inventorygen.groupid.tooltip"));
                cell.setShowsValidityState(true);
                cell.setValidityState(currentGroupIDState());
                cell.addPropertyListener(cell1 ->
                {
                    component.inventoryGeneratorID = (String) cell1.getPropertyValue();
                    ((TableCellString) cell1).setValidityState(currentGroupIDState());
                });
                return new TableElementCell(IvTranslations.get("reccomplex.gui.inventorygen.groupid"), cell);
            }
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
