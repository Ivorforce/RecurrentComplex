/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editspawncommandblock;

import ivorius.reccomplex.blocks.TileEntitySpawnCommand;
import ivorius.reccomplex.blocks.TileEntityWithGUI;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.scripts.world.WorldScriptCommand;
import net.minecraft.tileentity.TileEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceSpawnCommandBlock extends TableDataSourceList<WorldScriptCommand.Entry, List<WorldScriptCommand.Entry>>
{
    private TileEntitySpawnCommand tileEntity;

    public TableDataSourceSpawnCommandBlock(TileEntitySpawnCommand tileEntity, TableDelegate tableDelegate, TableNavigator navigator)
    {
        super(tileEntity.script.entries, tableDelegate, navigator);
        this.tileEntity = tileEntity;
        setEarlierTitle("Up");
        setLaterTitle("Down");
    }

    public TileEntitySpawnCommand getTileEntity()
    {
        return tileEntity;
    }

    public void setTileEntity(TileEntitySpawnCommand tileEntity)
    {
        this.tileEntity = tileEntity;
    }

    @Override
    public String getDisplayString(WorldScriptCommand.Entry entry)
    {
        return StringUtils.abbreviate(entry.command, 20);
    }

    @Override
    public WorldScriptCommand.Entry newEntry(String actionID)
    {
        return new WorldScriptCommand.Entry(1.0, "");
    }

    @Override
    public TableDataSource editEntryDataSource(WorldScriptCommand.Entry entry)
    {
        return new TableDataSourceSpawnCommandEntry(entry, tableDelegate);
    }
}
