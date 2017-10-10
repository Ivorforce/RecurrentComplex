/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.client.rendering.MazeVisualizationContext;
import ivorius.reccomplex.gui.table.TableCells;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.cell.TableCellButton;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceList;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.GenerationType;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceGenerationType extends TableDataSourceList<GenerationType, List<GenerationType>>
{
    protected MazeVisualizationContext realWorldMapper;

    public TableDataSourceGenerationType(List<GenerationType> list, MazeVisualizationContext realWorldMapper, TableDelegate tableDelegate, TableNavigator navigator)
    {
        super(list, tableDelegate, navigator);
        setUsesPresetActionForAdding(true);
        this.realWorldMapper = realWorldMapper;
    }

    @Override
    public List<TableCellButton> getAddActions()
    {
        return TableCells.addManyWithBase(StructureRegistry.GENERATION_TYPES.allIDs(), "reccomplex.generationInfo.", canEditList());
    }

    @Override
    public String getDisplayString(GenerationType generationType)
    {
        return generationType.displayString();
    }

    @Override
    public List<String> getTooltip(GenerationType generationType)
    {
        return TableCells.getTooltip("reccomplex.generationInfo.", StructureRegistry.GENERATION_TYPES.iDForType(generationType.getClass()));
    }

    @Override
    public GenerationType newEntry(String actionID)
    {
        return tryInstantiate(actionID, StructureRegistry.GENERATION_TYPES.typeForID(actionID), "Failed instantiating generation type: %s");
    }

    @Nonnull
    @Override
    public TableCell entryCell(boolean enabled, GenerationType generationType)
    {
        return TableCells.edit(enabled, navigator, tableDelegate, () -> generationType.tableDataSource(realWorldMapper, navigator, tableDelegate));
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Generation Types";
    }
}
