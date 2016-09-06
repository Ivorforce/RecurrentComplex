/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.transformers;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.transformers.TransformerNegativeSpace;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBTNegativeSpace extends TableDataSourceSegmented
{
    private TransformerNegativeSpace transformer;

    public TableDataSourceBTNegativeSpace(TransformerNegativeSpace transformer, TableNavigator navigator, TableDelegate delegate)
    {
        this.transformer = transformer;

        addManagedSection(0, new TableDataSourceTransformer(transformer, delegate, navigator));
        addManagedSection(1, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.gui.sources"), transformer.sourceMatcher));
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
