/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.transformers;

import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.editstructure.TableDataSourceWeightedBlockStateList;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.transformers.TransformerReplace;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBTReplace extends TableDataSourceSegmented implements TableCellActionListener
{
    private TransformerReplace transformer;

    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    public TableDataSourceBTReplace(TransformerReplace transformer, TableNavigator navigator, TableDelegate tableDelegate)
    {
        this.transformer = transformer;
        this.navigator = navigator;
        this.tableDelegate = tableDelegate;

        addManagedSection(0, TableDataSourceExpression.constructDefault("Sources", transformer.sourceMatcher));
    }

    public TransformerReplace getTransformer()
    {
        return transformer;
    }

    public void setTransformer(TransformerReplace transformer)
    {
        this.transformer = transformer;
    }

    @Override
    public int numberOfSegments()
    {
        return 2;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return segment == 1 ? 1 : super.sizeOfSegment(segment);
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 1)
        {
            TableCellButton cell = new TableCellButton("dest", new TableCellButton.Action("edit", "Edit"));
            cell.addListener(this);
            return new TableElementCell("Destinations", cell);
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    @Override
    public void actionPerformed(TableCell cell, String action)
    {
        if ("dest".equals(cell.getID()))
        {
            GuiTable table = new GuiTable(tableDelegate, new TableDataSourceWeightedBlockStateList(transformer.destination, tableDelegate, navigator));
            navigator.pushTable(table);
        }
    }
}
