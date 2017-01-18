/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.cell;

import com.google.common.primitives.Doubles;

import javax.annotation.Nullable;

/**
 * Created by lukas on 02.06.14.
 */
public class TableCellStringDouble extends TableCellTextField<Double>
{
    public TableCellStringDouble(String id, Double value)
    {
        super(id, value);
        showsValidityState = true;
    }

    @Override
    protected String serialize(Double aDouble)
    {
        return aDouble.toString();
    }

    @Nullable
    @Override
    protected Double deserialize(String string)
    {
        return Doubles.tryParse(string);
    }
}
