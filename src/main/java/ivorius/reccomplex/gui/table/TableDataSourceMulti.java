/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

import java.util.Arrays;
import java.util.List;

/**
 * Created by lukas on 22.06.14.
 */
public class TableDataSourceMulti extends TableDataSourceSegmented
{
    public TableDataSourceMulti(List<TableDataSource> sources)
    {
        for (int i = 0; i < sources.size(); i++)
            addManagedSection(i, sources.get(i));
    }

    public TableDataSourceMulti(TableDataSource... sources)
    {
        this(Arrays.asList(sources));
    }
}
