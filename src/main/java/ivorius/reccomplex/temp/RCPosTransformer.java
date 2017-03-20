/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.temp;

import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.math.MinecraftTransforms;
import ivorius.ivtoolkit.transform.AreaTransformable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by lukas on 21.02.17.
 */
public class RCPosTransformer
{
    public static void transformAdditionalData(TileEntity tileEntity, AxisAlignedTransform2D transform, int[] size)
    {
        if (tileEntity instanceof AreaTransformable)
            ((AreaTransformable) tileEntity).transform(transform.getRotation(), transform.isMirrorX(), size);
        else
        {
            Pair<Rotation, Mirror> mct = MinecraftTransforms.to(transform);
            tileEntity.func_189668_a(mct.getRight());
            tileEntity.func_189667_a(mct.getLeft());
        }
    }
}
