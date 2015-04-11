/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.transformers;

import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.transformers.TransformerNaturalAir;
import ivorius.reccomplex.utils.IvTranslations;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBTNaturalAir extends TableDataSourceSegmented
{
    private TransformerNaturalAir transformer;

    public TableDataSourceBTNaturalAir(TransformerNaturalAir transformer)
    {
        this.transformer = transformer;

        addManagedSection(0, TableDataSourceExpression.constructDefault("Sources", transformer.sourceMatcher));
    }

    public TransformerNaturalAir getTransformer()
    {
        return transformer;
    }

    public void setTransformer(TransformerNaturalAir transformer)
    {
        this.transformer = transformer;
    }
}
