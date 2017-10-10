/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.cell;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * Created by lukas on 02.06.14.
 */

@SideOnly(Side.CLIENT)
public class TableCellInteger extends TableCellSlider<Integer>
{
    public TableCellInteger(String id, int value, int min, int max)
    {
        super(id, value, min, max);
    }

    @Override
    protected float serialize(Integer integer)
    {
        return integer;
    }

    @Nullable
    @Override
    protected Integer deserialize(float val)
    {
        return (int) (val + 0.5f);
    }

    @Override
    protected String displayString()
    {
        return getPropertyValue().toString();
    }
}
