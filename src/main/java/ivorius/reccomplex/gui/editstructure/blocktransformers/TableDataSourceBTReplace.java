/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.blocktransformers;

import ivorius.reccomplex.gui.editstructure.TableDataSourceBiomeGenList;
import ivorius.reccomplex.gui.editstructure.TableDataSourceDimensionGen;
import ivorius.reccomplex.gui.editstructure.TableDataSourceWeightedBlockStateList;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.blocktransformers.BlockTransformerReplace;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBTReplace extends TableDataSourceSegmented implements TableElementPropertyListener, TableElementActionListener
{
    private BlockTransformerReplace blockTransformer;

    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    private TableElementTitle parsed;

    public TableDataSourceBTReplace(BlockTransformerReplace blockTransformer, TableNavigator navigator, TableDelegate tableDelegate)
    {
        this.blockTransformer = blockTransformer;
        this.navigator = navigator;
        this.tableDelegate = tableDelegate;
    }

    public BlockTransformerReplace getBlockTransformer()
    {
        return blockTransformer;
    }

    public void setBlockTransformer(BlockTransformerReplace blockTransformer)
    {
        this.blockTransformer = blockTransformer;
    }

    @Override
    public int numberOfSegments()
    {
        return 2;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return segment == 0 ? 2 : 1;
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            if (index == 0)
            {
                TableElementString element = new TableElementString("source", "Sources", blockTransformer.sourceMatcher.getExpression());
                element.addPropertyListener(this);
                return element;
            }
            else if (index == 1)
                return parsed = new TableElementTitle("parsed", "", StringUtils.abbreviate(TableDataSourceDimensionGen.parsedString(blockTransformer.sourceMatcher), 60));
        }
        else if (segment == 1)
        {
            TableElementButton element = new TableElementButton("dest", "Destinations", new TableElementButton.Action("edit", "Edit"));
            element.addListener(this);
            return element;
        }

        return null;
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        if ("source".equals(element.getID()))
        {
            blockTransformer.sourceMatcher.setExpression((String) element.getPropertyValue());
            if (parsed != null)
                parsed.setDisplayString(StringUtils.abbreviate(TableDataSourceDimensionGen.parsedString(blockTransformer.sourceMatcher), 60));
        }
    }

    @Override
    public void actionPerformed(TableElement element, String action)
    {
        if ("dest".equals(element.getID()))
        {
            GuiTable table = new GuiTable(tableDelegate, new TableDataSourceWeightedBlockStateList(blockTransformer.destination, tableDelegate, navigator));
            navigator.pushTable(table);
        }
    }
}
