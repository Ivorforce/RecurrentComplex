/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.Metadata;

/**
 * Created by lukas on 22.02.15.
 */
public class TableDataSourceMetadata extends TableDataSourceSegmented implements TableCellPropertyListener
{
    protected Metadata metadata;

    public TableDataSourceMetadata(Metadata metadata)
    {
        this.metadata = metadata;
    }

    public Metadata getMetadata()
    {
        return metadata;
    }

    public void setMetadata(Metadata metadata)
    {
        this.metadata = metadata;
    }

    @Override
    public int numberOfSegments()
    {
        return 1;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return 3;
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        switch (index)
        {
            case 0:
            {
                TableCellString cell = new TableCellString("authors", metadata.authors);
                cell.addPropertyListener(this);
                return new TableElementCell("Authors", cell);
            }
            case 1:
            {
                TableCellString cell = new TableCellString("weblink", metadata.weblink);
                cell.addPropertyListener(this);
                return new TableElementCell("Weblink", cell);
            }
            case 2:
            {
                TableCellString cell = new TableCellString("comment", metadata.comment);
                cell.addPropertyListener(this);
                return new TableElementCell("Comment", cell);
            }
        }

        return null;
    }

    @Override
    public void valueChanged(TableCellPropertyDefault cell)
    {
        if (cell.getID() != null)
        {
            switch (cell.getID())
            {
                case "authors":
                    metadata.authors = (String) cell.getPropertyValue();
                    break;
                case "weblink":
                    metadata.weblink = (String) cell.getPropertyValue();
                    break;
                case "comment":
                    metadata.comment = (String) cell.getPropertyValue();
                    break;
            }
        }
    }
}
