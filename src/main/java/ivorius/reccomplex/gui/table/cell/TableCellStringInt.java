/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.cell;

import com.google.common.primitives.Ints;

import javax.annotation.Nullable;

/**
 * Created by lukas on 02.06.14.
 */
public class TableCellStringInt extends TableCellTextField<Integer>
{
    public TableCellStringInt(String id, Integer value)
    {
        super(id, value);
        showsValidityState = true;
    }

    @Override
    protected String serialize(Integer integer)
    {
        return integer.toString();
    }

    @Nullable
    @Override
    protected Integer deserialize(String string)
    {
        return Ints.tryParse(string);
    }
}
