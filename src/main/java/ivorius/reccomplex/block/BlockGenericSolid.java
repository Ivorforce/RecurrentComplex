/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.block;

import ivorius.reccomplex.block.materials.RCMaterials;
import ivorius.reccomplex.utils.UnstableBlock;

/**
 * Created by lukas on 06.06.14.
 */
public class BlockGenericSolid extends BlockTyped implements UnstableBlock
{
    public BlockGenericSolid()
    {
        super(RCMaterials.materialGenericSolid);
    }
}
