/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.transformers;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.structure.generic.transformers.TransformerNegativeSpace;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBTNegativeSpace extends TableDataSourceSegmented
{
    private TransformerNegativeSpace transformer;

    public TableDataSourceBTNegativeSpace(TransformerNegativeSpace transformer, TableNavigator navigator, TableDelegate delegate)
    {
        this.transformer = transformer;

        addManagedSegment(0, new TableDataSourceTransformer(transformer, delegate, navigator));
        addManagedSegment(1, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.gui.sources"), transformer.sourceMatcher, null));
        addManagedSegment(2, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.gui.destinations"), transformer.destMatcher, null));
    }

    public TransformerNegativeSpace getTransformer()
    {
        return transformer;
    }

    public void setTransformer(TransformerNegativeSpace transformer)
    {
        this.transformer = transformer;
    }
}
