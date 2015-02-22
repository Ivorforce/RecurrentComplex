/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editspawncommandblock;

import ivorius.reccomplex.blocks.TileEntitySpawnCommand;
import ivorius.reccomplex.gui.table.*;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceSpawnCommandBlock extends TableDataSourceList<TileEntitySpawnCommand.Entry, List<TileEntitySpawnCommand.Entry>>
{
    private TileEntitySpawnCommand tileEntity;

    public TableDataSourceSpawnCommandBlock(TileEntitySpawnCommand tileEntity, TableDelegate tableDelegate, TableNavigator navigator)
    {
        super(tileEntity.entries, tableDelegate, navigator);
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
    public String getDisplayString(TileEntitySpawnCommand.Entry entry)
    {
        return StringUtils.abbreviate(entry.command, 16) + " (" + entry.itemWeight + ")";
    }

    @Override
    public TileEntitySpawnCommand.Entry newEntry(String actionID)
    {
        return new TileEntitySpawnCommand.Entry(100, "");
    }

    @Override
    public TableDataSource editEntryDataSource(TileEntitySpawnCommand.Entry entry)
    {
        return new TableDataSourceSpawnCommandEntry(entry, tableDelegate);
    }
}
