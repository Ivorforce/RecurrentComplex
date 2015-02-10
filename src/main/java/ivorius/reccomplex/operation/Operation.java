/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.operation;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * Created by lukas on 10.02.15.
 */
public interface Operation
{
    int PREVIEW_TYPE_NONE = 0;
    int PREVIEW_TYPE_BOUNDING_BOX = 1;

    void perform(World world);

    void writeToNBT(NBTTagCompound compound);

    void readFromNBT(NBTTagCompound compound);

    void renderPreview(int previewType, World world, int ticks, float partialTicks);
}
