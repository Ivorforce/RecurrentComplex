/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.ivtoolkit.maze.MazePath;
import ivorius.ivtoolkit.maze.MazeRoom;
import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.worldgen.StructureRegistry;
import ivorius.reccomplex.worldgen.genericStructures.GenerationYSelector;
import ivorius.reccomplex.worldgen.genericStructures.GenericStructureInfo;
import ivorius.reccomplex.worldgen.genericStructures.SavedMazeComponent;
import ivorius.reccomplex.worldgen.genericStructures.Selection;
import ivorius.reccomplex.worldgen.genericStructures.gentypes.MazeGenerationInfo;
import ivorius.reccomplex.worldgen.genericStructures.gentypes.NaturalGenerationInfo;
import ivorius.reccomplex.worldgen.genericStructures.gentypes.VanillaStructureSpawnInfo;
import joptsimple.internal.Strings;

import java.util.*;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceGenericStructure implements TableDataSource, TableElementButton.Listener, TableElementPropertyListener
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
    public boolean has(GuiTable table, int index)
    {
        return index >= 0 && index < 7;
    }

    @Override
    public TableElement elementForIndex(GuiTable table, int index)
    {
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
            TableElementBoolean element = new TableElementBoolean("rotatable", "Rotatable", structureInfo.rotatable);
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 2)
        {
            TableElementBoolean element = new TableElementBoolean("mirrorable", "Mirrorable", structureInfo.mirrorable);
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 3)
        {
            TableElementString element = new TableElementString("dependencies", "Dependencies (A,B,...)", Strings.join(structureInfo.dependencies, ","));
            element.setValidityState(currentDependencyState());
            element.setShowsValidityState(true);
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 4)
        {
            TableElementButton elementEditTransformers = new TableElementButton("editTransformers", "Transformers", new TableElementButton.Action("edit", "Edit"));
            elementEditTransformers.addListener(this);
            return elementEditTransformers;
        }
        else if (index == 5)
        {
            TableElementButton.Action toggle = structureInfo.naturalGenerationInfo != null ? new TableElementButton.Action("delete", "Delete") : new TableElementButton.Action("enable", "Enable");
            TableElementButton elementEditTransformers = new TableElementButton("editNaturalGeneration", "Natural Generation",
                    new TableElementButton.Action("edit", "Edit", structureInfo.naturalGenerationInfo != null), toggle);
            elementEditTransformers.addListener(this);
            return elementEditTransformers;
        }
        else if (index == 6)
        {
            TableElementButton.Action toggle = structureInfo.mazeGenerationInfo != null ? new TableElementButton.Action("delete", "Delete") : new TableElementButton.Action("enable", "Enable");
            TableElementButton elementEditTransformers = new TableElementButton("editMazeGeneration", "Maze Generation",
                    new TableElementButton.Action("edit", "Edit", structureInfo.mazeGenerationInfo != null), toggle);
            elementEditTransformers.addListener(this);
            return elementEditTransformers;
        }
//        else if (index == 7)
//        {
//            TableElementButton.Action toggle = structureInfo.vanillaStructureSpawnInfo != null ? new TableElementButton.Action("delete", "Delete") : new TableElementButton.Action("enable", "Enable");
//            TableElementButton elementEditTransformers = new TableElementButton("editVanillaStructureGeneration", "Vanilla Structure",
//                    new TableElementButton.Action("edit", "Edit", structureInfo.vanillaStructureSpawnInfo != null), toggle);
//            elementEditTransformers.addListener(this);
//            return elementEditTransformers;
//        }

        return null;
    }

    @Override
    public void actionPerformed(TableElementButton tableElementButton, String actionID)
    {
        if ("editTransformers".equals(tableElementButton.getID()) && "edit".equals(actionID))
        {
            GuiTable editTransformersProperties = new GuiTable(tableDelegate, new TableDataSourceBlockTransformerList(structureInfo.blockTransformers, tableDelegate, navigator));
            navigator.pushTable(editTransformersProperties);
        }
        else if ("editNaturalGeneration".equals(tableElementButton.getID()))
        {
            switch (actionID)
            {
                case "edit":
                    GuiTable editNaturalGeneration = new GuiTable(tableDelegate, new TableDataSourceNaturalGenerationInfo(navigator, tableDelegate, structureInfo));
                    navigator.pushTable(editNaturalGeneration);
                    break;
                case "delete":
                    structureInfo.naturalGenerationInfo = null;
                    tableDelegate.reloadData();
                    break;
                case "enable":
                    structureInfo.naturalGenerationInfo = new NaturalGenerationInfo("decoration", new GenerationYSelector(GenerationYSelector.SelectionMode.SURFACE, 0, 0));
                    tableDelegate.reloadData();
                    break;
            }
        }
        else if ("editMazeGeneration".equals(tableElementButton.getID()))
        {
            switch (actionID)
            {
                case "edit":
                    GuiTable editMazeGeneration = new GuiTable(tableDelegate, new TableDataSourceMazeGenerationInfo(navigator, tableDelegate, structureInfo));
                    navigator.pushTable(editMazeGeneration);
                    break;
                case "delete":
                    structureInfo.mazeGenerationInfo = null;
                    tableDelegate.reloadData();
                    break;
                case "enable":
                    SavedMazeComponent mazeComponent = new SavedMazeComponent(100);
                    mazeComponent.rooms.add(new Selection.Area(true, new int[3], new int[3]));
                    mazeComponent.setExitPaths(Arrays.asList(new MazePath(new MazeRoom(0, 0, 0), 0, true), new MazePath(new MazeRoom(0, 0, 0), 0, false),
                            new MazePath(new MazeRoom(0, 0, 0), 2, true), new MazePath(new MazeRoom(0, 0, 0), 2, false)));
                    structureInfo.mazeGenerationInfo = new MazeGenerationInfo("", mazeComponent);
                    tableDelegate.reloadData();
                    break;
            }
        }
        else if ("editVanillaStructureGeneration".equals(tableElementButton.getID()))
        {
            switch (actionID)
            {
                case "edit":
                    GuiTable editMazeGeneration = new GuiTable(tableDelegate, new TableDataSourceVanillaStructureGenerationInfo(navigator, tableDelegate, structureInfo));
                    navigator.pushTable(editMazeGeneration);
                    break;
                case "delete":
                    structureInfo.vanillaStructureSpawnInfo = null;
                    tableDelegate.reloadData();
                    break;
                case "enable":
                    structureInfo.vanillaStructureSpawnInfo = VanillaStructureSpawnInfo.defaultStructureSpawnInfo();
                    tableDelegate.reloadData();
                    break;
            }
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

        return structureKey.trim().length() > 0 ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.INVALID;
    }

    private GuiValidityStateIndicator.State currentDependencyState()
    {
        return structureInfo.areDependenciesResolved() ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.SEMI_VALID;
    }
}
