/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui;

import ivorius.reccomplex.gui.table.TableElementEnum;
import ivorius.reccomplex.utils.DirectionNames;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Created by lukas on 05.04.15.
 */
public class TableDirections
{
    public static TableElementEnum.Option<ForgeDirection>[] getDirectionOptions(ForgeDirection[] directions)
    {
        TableElementEnum.Option<ForgeDirection>[] options = new TableElementEnum.Option[directions.length];
        for (int i = 0; i < options.length; i++)
            options[i] = new TableElementEnum.Option<>(directions[i], DirectionNames.of(directions[i]));
        return options;
    }

    public static TableElementEnum.Option<ForgeDirection>[] getDirectionOptions(ForgeDirection[] directions, String nullTitle)
    {
        TableElementEnum.Option<ForgeDirection>[] options = new TableElementEnum.Option[directions.length];
        for (int i = 0; i < options.length; i++)
            options[i] = new TableElementEnum.Option<>(directions[i], DirectionNames.of(directions[i], nullTitle));
        return options;
    }
}
