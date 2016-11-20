/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.random;

import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.ivtoolkit.tools.NBTCompoundObject;
import ivorius.ivtoolkit.tools.NBTCompoundObjects;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 12.10.14.
 */
public class BlurredValueField extends ivorius.ivtoolkit.random.BlurredValueField
{
    public BlurredValueField()
    {
    }

    public BlurredValueField(int... size)
    {
        super(size);
    }
}
