/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui;

import ivorius.reccomplex.gui.table.TableCellEnum;
import ivorius.reccomplex.utils.DirectionNames;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Created by lukas on 05.04.15.
 */
public class TableDirections
{
    public static TableCellEnum.Option<ForgeDirection>[] getDirectionOptions(ForgeDirection[] directions)
    {
        TableCellEnum.Option<ForgeDirection>[] options = new TableCellEnum.Option[directions.length];
        for (int i = 0; i < options.length; i++)
            options[i] = new TableCellEnum.Option<>(directions[i], DirectionNames.of(directions[i]));
        return options;
    }

    public static TableCellEnum.Option<ForgeDirection>[] getDirectionOptions(ForgeDirection[] directions, String nullTitle)
    {
        TableCellEnum.Option<ForgeDirection>[] options = new TableCellEnum.Option[directions.length];
        for (int i = 0; i < options.length; i++)
            options[i] = new TableCellEnum.Option<>(directions[i], DirectionNames.of(directions[i], nullTitle));
        return options;
    }
}
