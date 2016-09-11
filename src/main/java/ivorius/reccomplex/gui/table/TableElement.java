/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by lukas on 30.05.14.
 */
public interface TableElement extends TableCell
{
    @Nullable
    String getTitle();

    @Nullable
    List<String> getTitleTooltip();
}
