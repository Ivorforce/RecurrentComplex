/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.gentypes;

import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 19.02.15.
 */
public abstract class StructureGenerationInfo
{
    @Nonnull
    public abstract String id();

    public abstract void setID(@Nonnull String id);

    public abstract String displayString();

    public abstract TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate);
}
