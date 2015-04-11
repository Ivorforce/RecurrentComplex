/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

import javax.annotation.Nullable;

/**
 * Created by lukas on 30.05.14.
 */
public interface TableElement extends TableCell
{
    @Nullable
    String getTitle();
}
