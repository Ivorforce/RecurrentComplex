/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.inventorygen;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.RCGuiHandler;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.TableElementSaveDirectory;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCellMultiBuilder;
import ivorius.reccomplex.gui.table.cell.TableCellString;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSupplied;
import ivorius.reccomplex.utils.SaveDirectoryData;
import ivorius.reccomplex.world.storage.loot.GenericItemCollection;
import ivorius.reccomplex.world.storage.loot.GenericItemCollectionRegistry;
import ivorius.reccomplex.world.storage.loot.WeightedItemCollectionRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 27.08.16.
 */

@SideOnly(Side.CLIENT)
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

        addSegment(0, () -> {
            TableCellString cell = new TableCellString(null, this.key);
            cell.setShowsValidityState(true);
            cell.setValidityState(currentKeyState());
            cell.addListener(val ->
            {
                this.key = val;
                cell.setValidityState(currentKeyState());
            });
            return new TitledCell(IvTranslations.get("reccomplex.gui.inventorygen.componentid"), cell)
                    .withTitleTooltip(IvTranslations.formatLines("reccomplex.gui.inventorygen.componentid.tooltip"));
        });

        addSegment(1, new TableDataSourceSupplied(() -> TableElementSaveDirectory.create(saveDirectoryData, () -> key, delegate)));

        addSegment(2, () -> {
            TableCellString cell = new TableCellString(null, component.inventoryGeneratorID);
            cell.setShowsValidityState(true);
            cell.setValidityState(currentGroupIDState());
            cell.addListener(val ->
            {
                component.inventoryGeneratorID = val;
                cell.setValidityState(currentGroupIDState());
            });
            return new TitledCell(IvTranslations.get("reccomplex.gui.inventorygen.groupid"), cell)
                    .withTitleTooltip(IvTranslations.formatLines("reccomplex.gui.inventorygen.groupid.tooltip"));
        });

        addSegment(3, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.inventorygen.dependencies"), IvTranslations.getLines("reccomplex.inventorygen.dependencies.tooltip"), this.component.dependencies, RecurrentComplex.saver));
        addSegment(4, TableCellMultiBuilder.create(navigator, delegate)
                .addAction(() -> RCGuiHandler.editInventoryGenComponentItems(this.player, this.key, this.component, this.saveDirectoryData), () -> IvTranslations.get("reccomplex.gui.edit"), null
                ).withTitle(IvTranslations.format("reccomplex.gui.inventorygen.items.summary", String.valueOf(this.component.items.size()))).buildDataSource());
    }

    public SaveDirectoryData getSaveDirectoryData()
    {
        return saveDirectoryData;
    }

    public void setSaveDirectoryData(SaveDirectoryData saveDirectoryData)
    {
        this.saveDirectoryData = saveDirectoryData;
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Item Generation Component";
    }

    private GuiValidityStateIndicator.State currentKeyState()
    {
        return key.trim().isEmpty() ? GuiValidityStateIndicator.State.INVALID
                : GenericItemCollectionRegistry.INSTANCE.ids().contains(key)
                ? GuiValidityStateIndicator.State.SEMI_VALID : GuiValidityStateIndicator.State.VALID;
    }

    private GuiValidityStateIndicator.State currentGroupIDState()
    {
        return component.inventoryGeneratorID.trim().isEmpty() ? GuiValidityStateIndicator.State.INVALID
                : WeightedItemCollectionRegistry.INSTANCE.get(component.inventoryGeneratorID) instanceof GenericItemCollection
                ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.SEMI_VALID;
    }
}
