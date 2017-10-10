/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.cell.TableCellString;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.structure.generic.Metadata;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 22.02.15.
 */
public class TableDataSourceMetadata extends TableDataSourceSegmented
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

    @Nonnull
    @Override
    public String title()
    {
        return "Metadata";
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
    public TableCell cellForIndexInSegment(GuiTable table, int index, int segment)
    {
        switch (index)
        {
            case 0:
            {
                TableCellString cell = new TableCellString("authors", metadata.authors);
                cell.addListener(val -> metadata.authors = val);
                return new TitledCell("Authors", cell);
            }
            case 1:
            {
                TableCellString cell = new TableCellString("weblink", metadata.weblink);
                cell.addListener(val -> metadata.weblink = val);
                return new TitledCell("Weblink", cell);
            }
            case 2:
            {
                TableCellString cell = new TableCellString("comment", metadata.comment);
                cell.addListener(val -> metadata.comment = val);
                return new TitledCell("Comment", cell);
            }
        }

        return null;
    }
}
