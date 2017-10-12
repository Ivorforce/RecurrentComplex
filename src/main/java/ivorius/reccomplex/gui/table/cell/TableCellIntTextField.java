/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.cell;

import com.google.common.primitives.Ints;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.function.IntPredicate;

/**
 * Created by lukas on 02.06.14.
 */

@SideOnly(Side.CLIENT)
public class TableCellIntTextField extends TableCellTextField<Integer>
{
    @Nullable
    protected IntPredicate predicate;

    public TableCellIntTextField(String id, int value)
    {
        super(id, value);
        showsValidityState = true;
    }

    public TableCellIntTextField(String id, int value, IntPredicate predicate)
    {
        super(id, value);
        this.predicate = predicate;
    }

    public IntPredicate getPredicate()
    {
        return predicate;
    }

    public void setPredicate(IntPredicate predicate)
    {
        this.predicate = predicate;
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
        Integer i = Ints.tryParse(string);
        return i == null || (predicate != null && !predicate.test(i)) ? null : i;
    }
}
