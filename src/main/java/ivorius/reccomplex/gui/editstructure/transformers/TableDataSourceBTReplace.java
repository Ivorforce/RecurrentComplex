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
public class TableDataSourceBTReplace extends TableDataSourceSegmented
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
        addManagedSection(1, TableCellMultiBuilder.create(navigator, tableDelegate)
                .addNavigation(() -> "Edit", null,
                () -> new GuiTable(tableDelegate, new TableDataSourceWeightedBlockStateList(transformer.destination, tableDelegate, navigator))
                ).buildPreloaded("Destinations"));
    }

    public TransformerReplace getTransformer()
    {
        return transformer;
    }

    public void setTransformer(TransformerReplace transformer)
    {
        this.transformer = transformer;
    }
}
