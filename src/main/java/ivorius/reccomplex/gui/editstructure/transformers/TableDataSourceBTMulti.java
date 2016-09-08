/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.transformers;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.editstructure.TableDataSourceTransformerList;
import ivorius.reccomplex.gui.table.TableDataSourceSegmented;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.structures.generic.transformers.TransformerMulti;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBTMulti extends TableDataSourceSegmented
{
    private TransformerMulti transformer;

    private TableNavigator navigator;
    private TableDelegate delegate;

    public TableDataSourceBTMulti(TransformerMulti transformer, TableNavigator navigator, TableDelegate delegate)
    {
        this.transformer = transformer;
        this.navigator = navigator;
        this.delegate = delegate;

        addManagedSection(0, new TableDataSourceTransformer(transformer, delegate, navigator));
        addManagedSection(1, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.transformer.multi.condition"), transformer.getEnvironmentMatcher(), null));
        addManagedSection(2, new TableDataSourceTransformerList(transformer.getTransformers(), delegate, navigator));
    }

    public TransformerMulti getTransformer()
    {
        return transformer;
    }

    public void setTransformer(TransformerMulti transformer)
    {
        this.transformer = transformer;
    }
}
