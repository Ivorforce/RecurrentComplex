/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.transformers;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.transformers.TransformerEnsureBlocks;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBTEnsureSpace extends TableDataSourceSegmented
{
    private TransformerEnsureBlocks transformer;

    public TableDataSourceBTEnsureSpace(TransformerEnsureBlocks transformer, TableNavigator navigator, TableDelegate delegate)
    {
        this.transformer = transformer;

        addManagedSegment(0, new TableDataSourceTransformer(transformer, delegate, navigator));

        addManagedSegment(1, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.gui.sources"), transformer.sourceMatcher, null));
        addManagedSegment(2, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.transformer.ensureBlocks.dest"), transformer.destMatcher, null));
    }

    public TransformerEnsureBlocks getTransformer()
    {
        return transformer;
    }

    public void setTransformer(TransformerEnsureBlocks transformer)
    {
        this.transformer = transformer;
    }
}
