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
public class TableDataSourceMetadata extends TableDataSourceSegmented implements TableElementPropertyListener
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
                TableElementString element = new TableElementString("authors", "Authors", metadata.authors);
                element.addPropertyListener(this);
                return element;
            }
            case 1:
            {
                TableElementString element = new TableElementString("weblink", "Weblink", metadata.weblink);
                element.addPropertyListener(this);
                return element;
            }
            case 2:
            {
                TableElementString element = new TableElementString("comment", "Comment", metadata.comment);
                element.addPropertyListener(this);
                return element;
            }
        }

        return null;
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        switch (element.getID())
        {
            case "authors":
                metadata.authors = (String) element.getPropertyValue();
                break;
            case "weblink":
                metadata.weblink = (String) element.getPropertyValue();
                break;
            case "comment":
                metadata.comment = (String) element.getPropertyValue();
                break;
        }
    }
}
