/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import com.mojang.realmsclient.gui.ChatFormatting;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.files.RCFileTypeRegistry;
import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;

import java.util.Set;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceGenericStructure extends TableDataSourceSegmented implements TableCellPropertyListener
{
    private Set<String> structuresInActive;
    private Set<String> structuresInInactive;

    private GenericStructureInfo structureInfo;
    private String structureKey;

    private boolean saveAsActive;
    private boolean deleteOther = true;

    private TableDelegate tableDelegate;
    private TableNavigator navigator;

    public TableDataSourceGenericStructure(GenericStructureInfo structureInfo, String structureKey, boolean saveAsActive, Set<String> structuresInActive, Set<String> structuresInInactive, TableDelegate tableDelegate, TableNavigator navigator)
    {
        this.structureInfo = structureInfo;
        this.structureKey = structureKey;
        this.saveAsActive = saveAsActive;
        this.structuresInActive = structuresInActive;
        this.structuresInInactive = structuresInInactive;
        this.tableDelegate = tableDelegate;
        this.navigator = navigator;

        addManagedSection(1, TableCellMultiBuilder.create(navigator, tableDelegate)
                .addNavigation(() -> IvTranslations.get("reccomplex.gui.edit"), null,
                        () -> new GuiTable(tableDelegate, new TableDataSourceMetadata(structureInfo.metadata))
                ).buildDataSource(IvTranslations.get("reccomplex.structure.metadata")));
        addManagedSection(3, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.structure.dependencies"), structureInfo.dependencies));
        addManagedSection(4, TableCellMultiBuilder.create(navigator, tableDelegate)
                .addNavigation(() -> IvTranslations.get("reccomplex.gui.edit"), null,
                        () -> new GuiTable(tableDelegate, new TableDataSourceStructureGenerationInfoList(structureInfo.generationInfos, tableDelegate, navigator))
                ).buildDataSource(IvTranslations.get("reccomplex.structure.generation")));
        addManagedSection(5, TableCellMultiBuilder.create(navigator, tableDelegate)
                .addNavigation(() -> IvTranslations.get("reccomplex.gui.edit"), null,
                        () -> new GuiTable(tableDelegate, new TableDataSourceTransformerList(structureInfo.transformers, tableDelegate, navigator))
                ).buildDataSource(IvTranslations.get("reccomplex.structure.transformers")));
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

    public boolean isSaveAsActive()
    {
        return saveAsActive;
    }

    public void setSaveAsActive(boolean saveAsActive)
    {
        this.saveAsActive = saveAsActive;
    }

    public boolean isDeleteOther()
    {
        return deleteOther;
    }

    public void setDeleteOther(boolean deleteOther)
    {
        this.deleteOther = deleteOther;
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
        return 6;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 0:
                return 2;
            case 2:
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
                    TableCellString cell = new TableCellString("name", structureKey);
                    cell.setTooltip(IvTranslations.formatLines("reccomplex.structure.id.tooltip"));
                    cell.addPropertyListener(this);
                    cell.setShowsValidityState(true);
                    cell.setValidityState(currentNameState());
                    return new TableElementCell(IvTranslations.get("reccomplex.structure.id"), cell);
                }
                else if (index == 1)
                {
                    TableCellBoolean cellFolder = new TableCellBoolean("activeFolder", saveAsActive,
                            IvTranslations.format("reccomplex.structure.savePath", String.format("%s/%s%s", ChatFormatting.AQUA, RCFileTypeRegistry.getDirectoryName(true), ChatFormatting.RESET)),
                            IvTranslations.format("reccomplex.structure.savePath", String.format("%s/%s%s", ChatFormatting.AQUA, RCFileTypeRegistry.getDirectoryName(false), ChatFormatting.RESET)));
                    cellFolder.addPropertyListener(this);

                    if (saveAsActive ? structuresInInactive.contains(structureKey) : structuresInActive.contains(structureKey))
                    {
                        String path = RCFileTypeRegistry.getDirectoryName(!saveAsActive);
                        TableCellBoolean cellDelete = new TableCellBoolean("deleteOther", deleteOther,
                                IvTranslations.format("reccomplex.structure.deleteOther.true", ChatFormatting.RED, ChatFormatting.RESET, String.format("%s/%s%s", ChatFormatting.AQUA, path, ChatFormatting.RESET)),
                                IvTranslations.format("reccomplex.structure.deleteOther.false", ChatFormatting.YELLOW, ChatFormatting.RESET, String.format("%s/%s%s", ChatFormatting.AQUA, path, ChatFormatting.RESET)));
                        cellDelete.addPropertyListener(this);
                        cellDelete.setTooltip(IvTranslations.formatLines("reccomplex.structure.deleteOther.tooltip",
                                ChatFormatting.AQUA + RCFileTypeRegistry.getDirectoryName(false) + ChatFormatting.RESET,
                                ChatFormatting.AQUA + RCFileTypeRegistry.getDirectoryName(true) + ChatFormatting.RESET));

                        return new TableElementCell(new TableCellMulti(cellFolder, cellDelete));
                    }

                    return new TableElementCell(new TableCellMulti(cellFolder, new TableCellButton("", "", "-", false)));
                }
                break;
            case 2:
            {
                TableCellBoolean cellRotatable = new TableCellBoolean("rotatable", structureInfo.rotatable,
                        IvTranslations.get("reccomplex.structure.rotatable.true"),
                        IvTranslations.get("reccomplex.structure.rotatable.false"));
                cellRotatable.setTooltip(IvTranslations.formatLines("reccomplex.structure.rotatable.tooltip"));
                cellRotatable.addPropertyListener(this);

                TableCellBoolean cellMirrorable = new TableCellBoolean("mirrorable", structureInfo.mirrorable,
                        IvTranslations.format("reccomplex.structure.mirrorable.true"),
                        IvTranslations.format("reccomplex.structure.mirrorable.false"));
                cellMirrorable.setTooltip(IvTranslations.formatLines("reccomplex.structure.mirrorable.tooltip"));
                cellMirrorable.addPropertyListener(this);

                return new TableElementCell(new TableCellMulti(cellRotatable, cellMirrorable));
            }
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    @Override
    public void valueChanged(TableCellPropertyDefault cell)
    {
        if (cell.getID() != null)
        {
            switch (cell.getID())
            {
                case "name":
                    structureKey = (String) cell.getPropertyValue();
                    ((TableCellString) cell).setValidityState(currentNameState());
                    break;
                case "activeFolder":
                    saveAsActive = (boolean) cell.getPropertyValue();
                    tableDelegate.reloadData(); // Delete other cell might get added
                    break;
                case "deleteOther":
                    deleteOther = (boolean) cell.getPropertyValue();
                    break;
                case "rotatable":
                    structureInfo.rotatable = (boolean) cell.getPropertyValue();
                    break;
                case "mirrorable":
                    structureInfo.mirrorable = (boolean) cell.getPropertyValue();
                    break;
            }
        }
    }

    private GuiValidityStateIndicator.State currentNameState()
    {
        if (StructureRegistry.INSTANCE.allStructureIDs().contains(structureKey))
            return GuiValidityStateIndicator.State.SEMI_VALID;

        return structureKey.trim().length() > 0 && !structureKey.contains(" ")
                ? GuiValidityStateIndicator.State.VALID
                : GuiValidityStateIndicator.State.INVALID;
    }
}
