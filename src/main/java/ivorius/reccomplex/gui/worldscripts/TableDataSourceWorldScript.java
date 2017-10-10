/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.worldscripts;

import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.script.WorldScript;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 02.11.16.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceWorldScript extends TableDataSourceSegmented
{
    WorldScript<?> script;

    public TableDataSourceWorldScript(WorldScript<?> script)
    {
        this.script = script;
    }

    @Nonnull
    @Override
    public String title()
    {
        return script.getDisplayString();
    }
}
