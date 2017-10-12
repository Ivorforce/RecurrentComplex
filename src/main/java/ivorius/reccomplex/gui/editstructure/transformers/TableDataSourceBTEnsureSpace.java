/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.transformers;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.structure.generic.transformers.TransformerEnsureBlocks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by lukas on 05.06.14.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceBTEnsureSpace extends TableDataSourceSegmented
{
    private TransformerEnsureBlocks transformer;

    public TableDataSourceBTEnsureSpace(TransformerEnsureBlocks transformer, TableNavigator navigator, TableDelegate delegate)
    {
        this.transformer = transformer;

        addSegment(0, new TableDataSourceTransformer(transformer, delegate, navigator));

        addSegment(1, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.gui.sources"), IvTranslations.getLines("reccomplex.transformer.block.source.tooltip"), transformer.sourceMatcher, null));
        addSegment(2, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.transformer.ensureBlocks.dest"), IvTranslations.getLines("reccomplex.transformer.block.dest.tooltip"), transformer.destMatcher, null));
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
