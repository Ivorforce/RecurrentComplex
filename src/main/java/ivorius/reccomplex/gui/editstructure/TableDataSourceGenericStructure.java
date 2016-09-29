/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.TableElementSaveDirectory;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.StructureInfos;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import ivorius.reccomplex.utils.SaveDirectoryData;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceGenericStructure extends TableDataSourceSegmented
{
    private GenericStructureInfo structureInfo;
    private String structureKey;

    private SaveDirectoryData saveDirectoryData;

    private TableDelegate tableDelegate;
    private TableNavigator navigator;

    public TableDataSourceGenericStructure(GenericStructureInfo structureInfo, String structureKey, SaveDirectoryData saveDirectoryData, TableDelegate delegate, TableNavigator navigator)
    {
        this.structureInfo = structureInfo;
        this.structureKey = structureKey;
        this.saveDirectoryData = saveDirectoryData;
        this.tableDelegate = delegate;
        this.navigator = navigator;

        addManagedSegment(1, new TableDataSourceSupplied(() -> TableElementSaveDirectory.create(saveDirectoryData, () -> structureKey, delegate)));

        addManagedSegment(2, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceMetadata(structureInfo.metadata)
                ).buildDataSource(IvTranslations.get("reccomplex.structure.metadata"), IvTranslations.getLines("reccomplex.structure.metadata.tooltip")));

        addManagedSegment(4, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceStructureGenerationInfoList(structureInfo.generationInfos, delegate, navigator)
                ).buildDataSource(IvTranslations.get("reccomplex.structure.generation"), IvTranslations.getLines("reccomplex.structure.generation.tooltip")));

        addManagedSegment(5, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> structureInfo.transformer.tableDataSource(navigator, delegate))
                .buildDataSource(IvTranslations.get("reccomplex.structure.transformers"), IvTranslations.getLines("reccomplex.structure.transformers.tooltip")));

        addManagedSegment(6, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.structure.dependencies"), structureInfo.dependencies, null));
    }

    public GenericStructureInfo getStructureInfo()
    {
        return structureInfo;
    }

    public void setStructureInfo(GenericStructureInfo structureInfo)
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
    public int numberOfSegments()
    {
        return 7;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 0:
                return 1;
            case 3:
                return 1;
        }

        return super.sizeOfSegment(segment);
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        switch (segment)
        {
            case 0:
                if (index == 0)
                {
                    TableCellString cell = new TableCellString(null, structureKey);
                    cell.addPropertyConsumer(cell1 ->
                    {
                        structureKey = cell.getPropertyValue();
                        cell.setValidityState(currentNameState());
                        TableElements.reloadExcept(tableDelegate, "structureID");
                    });
                    cell.setShowsValidityState(true);
                    cell.setValidityState(currentNameState());
                    return new TableElementCell("structureID", IvTranslations.get("reccomplex.structure.id"), cell).withTitleTooltip(IvTranslations.formatLines("reccomplex.structure.id.tooltip"));
                }
            case 3:
            {
                TableCellBoolean cellRotatable = new TableCellBoolean("rotatable", structureInfo.rotatable,
                        IvTranslations.get("reccomplex.structure.rotatable.true"),
                        IvTranslations.get("reccomplex.structure.rotatable.false"));
                cellRotatable.setTooltip(IvTranslations.formatLines("reccomplex.structure.rotatable.tooltip"));
                cellRotatable.addPropertyConsumer(cell -> structureInfo.rotatable = cellRotatable.getPropertyValue());

                TableCellBoolean cellMirrorable = new TableCellBoolean("mirrorable", structureInfo.mirrorable,
                        IvTranslations.format("reccomplex.structure.mirrorable.true"),
                        IvTranslations.format("reccomplex.structure.mirrorable.false"));
                cellMirrorable.setTooltip(IvTranslations.formatLines("reccomplex.structure.mirrorable.tooltip"));
                cellMirrorable.addPropertyConsumer(cell -> structureInfo.mirrorable = cellMirrorable.getPropertyValue());

                return new TableElementCell(new TableCellMulti(cellRotatable, cellMirrorable));
            }
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    private GuiValidityStateIndicator.State currentNameState()
    {
        return StructureInfos.isSimpleID(structureKey)
                ? StructureRegistry.INSTANCE.ids().contains(structureKey)
                ? GuiValidityStateIndicator.State.SEMI_VALID
                : GuiValidityStateIndicator.State.VALID
                : GuiValidityStateIndicator.State.INVALID;
    }
}
