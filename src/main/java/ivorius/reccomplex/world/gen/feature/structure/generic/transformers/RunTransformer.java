/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.transformers;

/**
 * Created by lukas on 25.10.16.
 */
public class RunTransformer
{
    public final TransformerMulti transformer;
    public final TransformerMulti.InstanceData instanceData;

    public RunTransformer(TransformerMulti transformer, TransformerMulti.InstanceData instanceData)
    {
        this.transformer = transformer;
        this.instanceData = instanceData;
    }
}
