/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.cell;

import com.google.common.primitives.Longs;

import javax.annotation.Nullable;

/**
 * Created by lukas on 02.06.14.
 */
public class TableCellStringLong extends TableCellTextField<Long>
{
    public TableCellStringLong(String id, Long value)
    {
        super(id, value);
        showsValidityState = true;
    }

    @Override
    protected String serialize(Long aLong)
    {
        return aLong.toString();
    }

    @Nullable
    @Override
    protected Long deserialize(String string)
    {
        return Longs.tryParse(string);
    }
}
