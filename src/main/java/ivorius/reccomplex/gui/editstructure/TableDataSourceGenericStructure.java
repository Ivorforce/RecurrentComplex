/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import ivorius.reccomplex.utils.IvTranslations;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceGenericStructure extends TableDataSourceSegmented implements TableElementActionListener, TableElementPropertyListener
{
    private GenericStructureInfo structureInfo;
    private String structureKey;

    private TableDelegate tableDelegate;
    private TableNavigator navigator;

    public TableDataSourceGenericStructure(GenericStructureInfo structureInfo, String structureKey, TableDelegate tableDelegate, TableNavigator navigator)
    {
        this.structureInfo = structureInfo;
        this.structureKey = structureKey;
        this.tableDelegate = tableDelegate;
        this.navigator = navigator;

        addManagedSection(2, new TableDataSourceExpression<>("Dependencies", "reccomplex.expression.dependency.tooltip", structureInfo.dependencies));
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
        return 5;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 0:
                return 2;
            case 1:
                return 2;
            case 3:
                return 1;
            case 4:
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
                    TableElementString element = new TableElementString("name", IvTranslations.get("reccomplex.structure.id"), structureKey);
                    element.setTooltip(IvTranslations.formatLines("reccomplex.structure.id.tooltip"));
                    element.addPropertyListener(this);
                    element.setShowsValidityState(true);
                    element.setValidityState(currentNameState());
                    return element;
                }
                else if (index == 1)
                {
                    TableElementButton element = new TableElementButton("metadata", "", new TableElementButton.Action("metadata", "Metadata"));
                    element.addListener(this);
                    return element;
                }
                break;
            case 1:
                if (index == 0)
                {
                    TableElementBoolean element = new TableElementBoolean("rotatable", IvTranslations.get("reccomplex.structure.rotatable"), structureInfo.rotatable);
                    element.setTooltip(IvTranslations.formatLines("reccomplex.structure.rotatable.tooltip"));
                    element.addPropertyListener(this);
                    return element;
                }
                else if (index == 1)
                {
                    TableElementBoolean element = new TableElementBoolean("mirrorable", IvTranslations.format("reccomplex.structure.mirrorable"), structureInfo.mirrorable);
                    element.setTooltip(IvTranslations.formatLines("reccomplex.structure.mirrorable.tooltip"));
                    element.addPropertyListener(this);
                    return element;
                }
                break;
            case 3:
            {
                TableElementButton element = new TableElementButton("editGenerationInfos", "Generation", new TableElementButton.Action("edit", "Edit"));
                element.addListener(this);
                return element;
            }
            case 4:
            {
                TableElementButton element = new TableElementButton("editTransformers", "Transformers", new TableElementButton.Action("edit", "Edit"));
                element.addListener(this);
                return element;
            }
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    @Override
    public void actionPerformed(TableElement element, String actionID)
    {
        if ("metadata".equals(actionID))
        {
            GuiTable table = new GuiTable(tableDelegate, new TableDataSourceMetadata(structureInfo.metadata));
            navigator.pushTable(table);
        }
        else if ("editTransformers".equals(element.getID()) && "edit".equals(actionID))
        {
            GuiTable editTransformersProperties = new GuiTable(tableDelegate, new TableDataSourceTransformerList(structureInfo.transformers, tableDelegate, navigator));
            navigator.pushTable(editTransformersProperties);
        }
        else if ("editGenerationInfos".equals(element.getID()) && "edit".equals(actionID))
        {
            GuiTable editGenerationProperties = new GuiTable(tableDelegate, new TableDataSourceStructureGenerationInfoList(structureInfo.generationInfos, tableDelegate, navigator));
            navigator.pushTable(editGenerationProperties);
        }
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        if ("name".equals(element.getID()))
        {
            structureKey = (String) element.getPropertyValue();
            ((TableElementString) element).setValidityState(currentNameState());
        }
        else if ("rotatable".equals(element.getID()))
        {
            structureInfo.rotatable = (boolean) element.getPropertyValue();
        }
        else if ("mirrorable".equals(element.getID()))
        {
            structureInfo.mirrorable = (boolean) element.getPropertyValue();
        }
    }

    private GuiValidityStateIndicator.State currentNameState()
    {
        if (StructureRegistry.allStructureIDs().contains(structureKey))
        {
            return GuiValidityStateIndicator.State.SEMI_VALID;
        }

        return structureKey.trim().length() > 0 && !structureKey.contains(" ")
                ? GuiValidityStateIndicator.State.VALID
                : GuiValidityStateIndicator.State.INVALID;
    }
}
