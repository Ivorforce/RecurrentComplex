/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.transformers;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.TableDataSourceBlockState;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.TableDataSourceSegmented;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.structures.generic.transformers.TransformerPillar;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBTPillar extends TableDataSourceSegmented
{
    private TransformerPillar transformer;

    public TableDataSourceBTPillar(TransformerPillar transformer, TableNavigator navigator, TableDelegate delegate)
    {
        this.transformer = transformer;

        addManagedSection(0, new TableDataSourceTransformer(transformer, navigator, delegate));
        addManagedSection(1, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.gui.sources"), transformer.sourceMatcher));
        addManagedSection(2, new TableDataSourceBlockState(transformer.destState, state -> transformer.destState = state, navigator, delegate, IvTranslations.get("reccomplex.transformer.pillar.dest.block"), IvTranslations.get("reccomplex.transformer.pillar.dest.metadata")));
    }

    public TransformerPillar getTransformer()
    {
        return transformer;
    }

    public void setTransformer(TransformerPillar transformer)
    {
        this.transformer = transformer;
    }
}
