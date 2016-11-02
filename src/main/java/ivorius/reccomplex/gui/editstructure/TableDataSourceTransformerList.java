/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.gui.editstructure.preset.TableDataSourcePresettedList;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCellButton;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceList;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.transformers.Transformer;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
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
        return StringUtils.abbreviate(transformer.getDisplayString(), 24);
    }

    @Override
    public Transformer newEntry(String actionID)
    {
        Class<? extends Transformer> clazz = StructureRegistry.TRANSFORMERS.typeForID(actionID);
        return tryInstantiate(actionID, clazz, "Failed instantiating transformer: %s");
    }

    @Override
    public TableDataSource editEntryDataSource(Transformer entry)
    {
        return entry.tableDataSource(navigator, tableDelegate);
    }

    @Override
    public List<TableCellButton> getAddActions()
    {
        return TableDataSourcePresettedList.addActions(StructureRegistry.TRANSFORMERS.allIDs(), "reccomplex.transformer.", canEditList());
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Transformers";
    }
}
