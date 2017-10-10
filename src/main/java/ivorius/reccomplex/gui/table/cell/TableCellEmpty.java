/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.cell;

/**
 * Created by lukas on 02.06.14.
 */

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TableCellEmpty extends TableCellDefault
{
    public TableCellEmpty(String id)
    {
        super(id);
    }
}
