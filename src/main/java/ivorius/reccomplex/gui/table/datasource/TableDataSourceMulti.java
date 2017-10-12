/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.datasource;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;

/**
 * Created by lukas on 22.06.14.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceMulti extends TableDataSourceSegmented
{
    public TableDataSourceMulti(List<TableDataSource> sources)
    {
        for (int i = 0; i < sources.size(); i++)
            addSegment(i, sources.get(i));
    }

    public TableDataSourceMulti(TableDataSource... sources)
    {
        this(Arrays.asList(sources));
    }
}
