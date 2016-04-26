/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.gentypes;

import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;

import javax.annotation.Nonnull;
import java.util.Random;
import java.util.UUID;

/**
 * Created by lukas on 19.02.15.
 */
public abstract class StructureGenerationInfo
{
    public static String randomID(String prefix)
    {
        return prefix + "_" + randomID();
    }

    public static String randomID()
    {
        Random random = new Random();
        return String.format("%s_%s", Integer.toHexString(random.nextInt()), Integer.toHexString(random.nextInt()));
    }

    @Nonnull
    public abstract String id();

    public abstract void setID(@Nonnull String id);

    public abstract String displayString();

    public abstract TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate);
}
