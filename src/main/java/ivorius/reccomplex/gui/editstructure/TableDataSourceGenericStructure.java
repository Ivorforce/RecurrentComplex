/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import joptsimple.internal.Strings;

import java.util.*;

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
            case 2:
                return 1;
            case 1:
                return 2;
            case 3:
                return 1;
            case 4:
                return 1;
        }

        return 0;
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        switch (segment)
        {
            case 0:
                if (index == 0)
                {
                    TableElementString element = new TableElementString("name", "Name", structureKey);
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
                    TableElementBoolean element = new TableElementBoolean("rotatable", "Rotatable", structureInfo.rotatable);
                    element.addPropertyListener(this);
                    return element;
                }
                else if (index == 1)
                {
                    TableElementBoolean element = new TableElementBoolean("mirrorable", "Mirrorable", structureInfo.mirrorable);
                    element.addPropertyListener(this);
                    return element;
                }
                break;
            case 2:
            {
                TableElementString element = new TableElementString("dependencies", "Dependencies (A,B,...)", Strings.join(structureInfo.dependencies, ","));
                element.setValidityState(currentDependencyState());
                element.setShowsValidityState(true);
                element.addPropertyListener(this);
                return element;
            }
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

        return null;
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
            GuiTable editTransformersProperties = new GuiTable(tableDelegate, new TableDataSourceBlockTransformerList(structureInfo.blockTransformers, tableDelegate, navigator));
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
        else if ("dependencies".equals(element.getID()))
        {
            structureInfo.dependencies.clear();
            String[] dependencies = ((String) element.getPropertyValue()).split(",");
            if (dependencies.length != 1 || dependencies[0].trim().length() > 0)
            {
                Collections.addAll(structureInfo.dependencies, dependencies);
            }

            ((TableElementString) element).setValidityState(currentDependencyState());
        }
    }

    private GuiValidityStateIndicator.State currentNameState()
    {
        if (StructureRegistry.getAllStructureNames().contains(structureKey))
        {
            return GuiValidityStateIndicator.State.SEMI_VALID;
        }

        return structureKey.trim().length() > 0 && !structureKey.contains(" ")
                ? GuiValidityStateIndicator.State.VALID
                : GuiValidityStateIndicator.State.INVALID;
    }

    private GuiValidityStateIndicator.State currentDependencyState()
    {
        return structureInfo.areDependenciesResolved()
                ? GuiValidityStateIndicator.State.VALID
                : GuiValidityStateIndicator.State.SEMI_VALID;
    }
}
