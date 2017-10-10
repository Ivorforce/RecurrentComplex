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
public class TableCellString extends TableCellTextField<String>
{
    public TableCellString(String id, String value)
    {
        super(id, value);
    }

    @Override
    protected String serialize(String s)
    {
        return s;
    }

    @Nullable
    @Override
    protected String deserialize(String string)
    {
        return string;
    }
}
