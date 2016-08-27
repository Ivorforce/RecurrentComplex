/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.transformers;

import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.editstructure.TableDataSourceWeightedBlockStateList;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.transformers.TransformerReplaceAll;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBTReplaceAll extends TableDataSourceSegmented
{
    private TransformerReplaceAll transformer;

    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    public TableDataSourceBTReplaceAll(TransformerReplaceAll transformer, TableNavigator navigator, TableDelegate tableDelegate)
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

    public TransformerReplaceAll getTransformer()
    {
        return transformer;
    }

    public void setTransformer(TransformerReplaceAll transformer)
    {
        this.transformer = transformer;
    }
}
