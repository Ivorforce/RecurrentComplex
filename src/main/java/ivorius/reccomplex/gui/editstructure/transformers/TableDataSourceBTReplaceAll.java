/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.transformers;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.editstructure.TableDataSourceWeightedBlockStateList;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.world.gen.feature.structure.generic.transformers.TransformerReplaceAll;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBTReplaceAll extends TableDataSourceSegmented
{
    private TransformerReplaceAll transformer;

    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    public TableDataSourceBTReplaceAll(TransformerReplaceAll transformer, TableNavigator navigator, TableDelegate delegate)
    {
        this.transformer = transformer;
        this.navigator = navigator;
        this.tableDelegate = delegate;

        addManagedSegment(0, new TableDataSourceTransformer(transformer, delegate, navigator));
        addManagedSegment(1, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.gui.sources"), transformer.sourceMatcher, null));
        addManagedSegment(2, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceWeightedBlockStateList(transformer.destination, delegate, navigator)
                ).buildDataSource(IvTranslations.get("reccomplex.gui.destinations")));
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
