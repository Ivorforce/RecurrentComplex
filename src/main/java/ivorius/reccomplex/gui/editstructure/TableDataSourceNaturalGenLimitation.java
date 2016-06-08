/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.gentypes.NaturalGenerationInfo;

import java.util.Arrays;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceNaturalGenLimitation extends TableDataSourceSegmented implements TableCellPropertyListener
{
    private NaturalGenerationInfo.SpawnLimitation limitation;

    private TableDelegate tableDelegate;

    public TableDataSourceNaturalGenLimitation(NaturalGenerationInfo.SpawnLimitation limitation, TableDelegate tableDelegate)
    {
        this.limitation = limitation;
        this.tableDelegate = tableDelegate;
    }

    @Override
    public int numberOfSegments()
    {
        return 1;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 0:
                return 2;
            case 1:
                return 0;
//                return limitation.context == NaturalGenerationInfo.SpawnLimitation.Context.X_CHUNKS ? 1 : 0;
            default:
                return super.sizeOfSegment(segment);
        }
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            switch (index)
            {
                case 0:
                {

                    TableCellEnum<NaturalGenerationInfo.SpawnLimitation.Context> cell = new TableCellEnum<>("context", limitation.context, TableCellEnum.options(Arrays.asList(NaturalGenerationInfo.SpawnLimitation.Context.values()), "reccomplex.generationInfo.natural.limitation.context.", false));
                    cell.addPropertyListener(this);
                    return new TableElementCell("Context", cell);
                }
                case 1:
                {
                    TableCellInteger cell = new TableCellInteger("max", limitation.maxCount, 1, 50);
                    cell.addPropertyListener(this);
                    return new TableElementCell("Max Occurrences", cell);
                }
            }
        }
//        else if (segment == 1)
//        {
//            TableCellInteger cell = new TableCellInteger("chunks", limitation.chunkCount, 1, 100);
//            cell.addPropertyListener(this);
//            return new TableElementCell("Chunk Range", cell);
//        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    @Override
    public void valueChanged(TableCellPropertyDefault cell)
    {
        if ("context".equals(cell.getID()))
        {
            limitation.context = (NaturalGenerationInfo.SpawnLimitation.Context) cell.getPropertyValue();
            tableDelegate.reloadData();
        }
        else if ("max".equals(cell.getID()))
        {
            limitation.maxCount = (int) cell.getPropertyValue();
        }
//        else if ("chunks".equals(cell.getID()))
//        {
//            limitation.chunkCount = (int) cell.getPropertyValue();
//        }
    }
}
