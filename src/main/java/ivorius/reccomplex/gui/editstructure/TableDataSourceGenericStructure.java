/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.client.rendering.MazeVisualizationContext;
import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.TableElementSaveDirectory;
import ivorius.reccomplex.gui.table.TableCells;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.*;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSupplied;
import ivorius.reccomplex.utils.SaveDirectoryData;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.Structures;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 05.06.14.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceGenericStructure extends TableDataSourceSegmented
{
    protected GenericStructure structureInfo;
    protected String structureKey;

    protected SaveDirectoryData saveDirectoryData;

    protected TableDelegate tableDelegate;
    protected TableNavigator navigator;

    public TableDataSourceGenericStructure(GenericStructure structureInfo, String structureKey, SaveDirectoryData saveDirectoryData, TableDelegate delegate, TableNavigator navigator, MazeVisualizationContext visualizationContext)
    {
        this.structureInfo = structureInfo;
        this.structureKey = structureKey;
        this.saveDirectoryData = saveDirectoryData;
        this.tableDelegate = delegate;
        this.navigator = navigator;

        addSegment(0, () -> {
            TableCellString cell = new TableCellString(null, this.structureKey);
            cell.addListener(cell1 ->
            {
                this.structureKey = cell.getPropertyValue();
                cell.setValidityState(currentNameState());
                TableCells.reloadExcept(tableDelegate, "structureID");
            });
            cell.setShowsValidityState(true);
            cell.setValidityState(currentNameState());
            return new TitledCell("structureID", IvTranslations.get("reccomplex.structure.id"), cell).withTitleTooltip(IvTranslations.formatLines("reccomplex.structure.id.tooltip"));
        });

        addSegment(1, new TableDataSourceSupplied(() -> TableElementSaveDirectory.create(saveDirectoryData, () -> structureKey, delegate)));

        TableCellMultiBuilder tableCellMultiBuilder3 = TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceMetadata(structureInfo.metadata));
        addSegment(2, tableCellMultiBuilder3.withTitle(IvTranslations.get("reccomplex.structure.metadata"), IvTranslations.getLines("reccomplex.structure.metadata.tooltip")).buildDataSource());

        addSegment(3, () -> {
            TableCellBoolean cellRotatable = new TableCellBoolean("rotatable", structureInfo.rotatable,
                    IvTranslations.get("reccomplex.structure.rotatable.true"),
                    IvTranslations.get("reccomplex.structure.rotatable.false"));
            cellRotatable.addListener(cell -> structureInfo.rotatable = cellRotatable.getPropertyValue());

            TableCellBoolean cellMirrorable = new TableCellBoolean("mirrorable", structureInfo.mirrorable,
                    IvTranslations.format("reccomplex.structure.mirrorable.true"),
                    IvTranslations.format("reccomplex.structure.mirrorable.false"));
            cellMirrorable.addListener(cell -> structureInfo.mirrorable = cellMirrorable.getPropertyValue());

            return new TitledCell(IvTranslations.get("reccomplex.structure.orientation"), new TableCellMulti(cellRotatable, cellMirrorable))
                    .withTitleTooltip(IvTranslations.formatLines("reccomplex.structure.orientation.tooltip"));
        }, () -> {
            TableCellBoolean cellBlocking = new TableCellBoolean("blocking", structureInfo.blocking,
                    IvTranslations.format("reccomplex.structure.blocking.true"),
                    IvTranslations.format("reccomplex.structure.blocking.false"));
            cellBlocking.addListener(cell -> structureInfo.blocking = cellBlocking.getPropertyValue());

            return new TitledCell(IvTranslations.get("reccomplex.structure.blocking"), cellBlocking)
                    .withTitleTooltip(IvTranslations.formatLines("reccomplex.structure.blocking.tooltip"));
        });

        TableCellMultiBuilder tableCellMultiBuilder2 = TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceGenerationType(structureInfo.generationTypes, visualizationContext, delegate, navigator));
        addSegment(4, tableCellMultiBuilder2.withTitle(IvTranslations.get("reccomplex.structure.generation"), IvTranslations.getLines("reccomplex.structure.generation.tooltip")).buildDataSource());

        TableCellMultiBuilder tableCellMultiBuilder1 = TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> structureInfo.transformer.tableDataSource(navigator, delegate));
        addSegment(5, tableCellMultiBuilder1.withTitle(IvTranslations.get("reccomplex.structure.transformers"), IvTranslations.getLines("reccomplex.structure.transformers.tooltip")).buildDataSource());

        TableCellMultiBuilder tableCellMultiBuilder = TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceGenericVariableDomain(delegate, navigator, structureInfo.variableDomain));
        addSegment(6, tableCellMultiBuilder.withTitle(IvTranslations.get("reccomplex.structure.variables"), IvTranslations.getLines("reccomplex.structure.variables.tooltip")).buildDataSource());

        addSegment(7, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.structure.dependencies"), IvTranslations.getLines("reccomplex.structure.dependencies.tooltip"), structureInfo.dependencies, RecurrentComplex.saver));
    }

    public GenericStructure getStructureInfo()
    {
        return structureInfo;
    }

    public void setStructureInfo(GenericStructure structureInfo)
    {
        this.structureInfo = structureInfo;
    }

    public String getStructureKey()
    {
        return structureKey;
    }

    public void setStructureKey(String structureKey)
    {
        this.structureKey = structureKey;
    }

    public SaveDirectoryData getSaveDirectoryData()
    {
        return saveDirectoryData;
    }

    public void setSaveDirectoryData(SaveDirectoryData saveDirectoryData)
    {
        this.saveDirectoryData = saveDirectoryData;
    }

    public TableDelegate getTableDelegate()
    {
        return tableDelegate;
    }

    public void setTableDelegate(TableDelegate tableDelegate)
    {
        this.tableDelegate = tableDelegate;
    }

    public TableNavigator getNavigator()
    {
        return navigator;
    }

    public void setNavigator(TableNavigator navigator)
    {
        this.navigator = navigator;
    }

    @Override
    @Nonnull
    public String title()
    {
        return "Generic Structure";
    }

    private GuiValidityStateIndicator.State currentNameState()
    {
        return Structures.isSimpleID(structureKey)
                ? StructureRegistry.INSTANCE.ids().contains(structureKey)
                ? GuiValidityStateIndicator.State.SEMI_VALID
                : GuiValidityStateIndicator.State.VALID
                : GuiValidityStateIndicator.State.INVALID;
    }
}
