/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.ivtoolkit.maze.classic.MazeRoom;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.cell.TableCellButton;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceList;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.GenerationType;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceGenerationType extends TableDataSourceList<GenerationType, List<GenerationType>>
{
    protected Function<MazeRoom, BlockPos> realWorldMapper;

    public TableDataSourceGenerationType(List<GenerationType> list, Function<MazeRoom, BlockPos> realWorldMapper, TableDelegate tableDelegate, TableNavigator navigator)
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
