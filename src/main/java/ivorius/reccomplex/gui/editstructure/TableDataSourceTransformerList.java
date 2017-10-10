/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.gui.table.TableCells;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.cell.TableCellButton;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceList;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.transformers.Transformer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceTransformerList extends TableDataSourceList<Transformer, List<Transformer>>
{
    public TableDataSourceTransformerList(List<Transformer> list, TableDelegate tableDelegate, TableNavigator navigator)
    {
        super(list, tableDelegate, navigator);
        setUsesPresetActionForAdding(true);
    }

    @Override
    public String getDisplayString(Transformer transformer)
    {
        return transformer.getDisplayString();
    }

    @Override
    public Transformer newEntry(String actionID)
    {
        Class<? extends Transformer> clazz = StructureRegistry.TRANSFORMERS.typeForID(actionID);
        return tryInstantiate(actionID, clazz, "Failed instantiating transformer: %s");
    }

    @Nonnull
    @Override
    public TableCell entryCell(boolean enabled, Transformer transformer)
    {
        return TableCells.edit(enabled, navigator, tableDelegate, () -> transformer.tableDataSource(navigator, tableDelegate));
    }

    @Override
    public List<TableCellButton> getAddActions()
    {
        return TableCells.addManyWithBase(StructureRegistry.TRANSFORMERS.allIDs(), "reccomplex.transformer.", canEditList());
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Transformers";
    }
}
