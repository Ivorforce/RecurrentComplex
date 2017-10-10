/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.transformers;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.files.RCFileSaver;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.editstructure.TableDataSourceTransformerList;
import ivorius.reccomplex.gui.editstructure.preset.TableDataSourcePresettedObject;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.structure.generic.transformers.TransformerMulti;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by lukas on 05.06.14.
 */

@SideOnly(Side.CLIENT)
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

        addManagedSegment(0, new TableDataSourceTransformer(transformer, delegate, navigator));

        addManagedSegment(1, new TableDataSourcePresettedObject<>(transformer.getData(), RCFileSaver.TRANSFORMER_PRESET, delegate, navigator)
                .withApplyPresetAction(() -> addPresetSections(transformer, navigator, delegate)).withCurrentOnTop(true));

        addPresetSections(transformer, navigator, delegate);
    }

    public void addPresetSections(final TransformerMulti transformer, final TableNavigator navigator, final TableDelegate delegate)
    {
        addManagedSegment(2, new TableDataSourceTransformerList(transformer.getTransformers(), delegate, navigator)
        {
            @Override
            public boolean canEditList()
            {
                return transformer.getData().isCustom();
            }
        });

        addManagedSegment(3, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.transformer.multi.condition"), transformer.getEnvironmentMatcher(), null)
                .enabled(() -> transformer.getData().isCustom()));
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
