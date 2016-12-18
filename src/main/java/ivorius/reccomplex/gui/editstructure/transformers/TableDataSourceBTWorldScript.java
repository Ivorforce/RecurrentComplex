/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.transformers;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.worldscripts.multi.TableDataSourceWorldScriptMulti;
import ivorius.reccomplex.world.gen.feature.structure.generic.transformers.TransformerWorldScript;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBTWorldScript extends TableDataSourceSegmented
{
    private TransformerWorldScript transformer;

    private TableNavigator navigator;
    private TableDelegate delegate;

    public TableDataSourceBTWorldScript(TransformerWorldScript transformer, TableNavigator navigator, TableDelegate delegate)
    {
        this.transformer = transformer;
        this.navigator = navigator;
        this.delegate = delegate;

        addManagedSegment(0, new TableDataSourceTransformer(transformer, delegate, navigator));
        addManagedSegment(1, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.gui.sources"), IvTranslations.getLines("reccomplex.transformer.block.source.tooltip"), transformer.sourceMatcher, null));
        addManagedSegment(2, new TableDataSourceWorldScriptMulti(transformer.script, delegate, navigator));
    }

    public TransformerWorldScript getTransformer()
    {
        return transformer;
    }

    public void setTransformer(TransformerWorldScript transformer)
    {
        this.transformer = transformer;
    }
}
