/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.transformers;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.transformers.TransformerVillageSpecific;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBTBiomeSpecific extends TableDataSourceSegmented
{
    private TransformerVillageSpecific transformer;

    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    public TableDataSourceBTBiomeSpecific(TransformerVillageSpecific transformer, TableNavigator navigator, TableDelegate delegate)
    {
        this.transformer = transformer;
        this.navigator = navigator;
        this.tableDelegate = delegate;

        addManagedSection(0, new TableDataSourceTransformer(transformer, delegate, navigator));
        addManagedSection(1, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.gui.sources"), transformer.sourceMatcher, null));
    }

    public TransformerVillageSpecific getTransformer()
    {
        return transformer;
    }

    public void setTransformer(TransformerVillageSpecific transformer)
    {
        this.transformer = transformer;
    }
}
