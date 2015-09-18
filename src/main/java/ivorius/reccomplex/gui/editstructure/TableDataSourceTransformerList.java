/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.generic.transformers.Transformer;
import ivorius.reccomplex.utils.IvTranslations;
import net.minecraft.util.StatCollector;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
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
        Class<? extends Transformer> clazz = StructureRegistry.INSTANCE.getTransformerRegistry().typeForID(actionID);

        return instantiateTransformer(clazz);
    }

    @Override
    public TableDataSource editEntryDataSource(Transformer entry)
    {
        return entry.tableDataSource(navigator, tableDelegate);
    }

    @Override
    public TableCellButton.Action[] getAddActions()
    {
        Collection<String> allTypes = StructureRegistry.INSTANCE.getTransformerRegistry().allIDs();
        List<TableCellButton.Action> actions = new ArrayList<>(allTypes.size());
        for (String type : allTypes)
        {
            String baseKey = "reccomplex.transformer." + type;
            actions.add(new TableCellButton.Action(type,
                    StatCollector.translateToLocal(baseKey),
                    IvTranslations.formatLines(baseKey + ".tooltip")
            ));
        }
        return actions.toArray(new TableCellButton.Action[actions.size()]);
    }

    public Transformer instantiateTransformer(Class<? extends Transformer> clazz)
    {
        Transformer transformer = null;

        try
        {
            transformer = clazz.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            RecurrentComplex.logger.error(e);
        }

        return transformer;
    }
}
