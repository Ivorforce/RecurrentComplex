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
    void perform(World world);

    void writeToNBT(NBTTagCompound compound);

    void readFromNBT(NBTTagCompound compound);

    void renderPreview(PreviewType previewType, World world, int ticks, float partialTicks);

    public static enum PreviewType
    {
        NONE(0, "none"),
        BOUNDING_BOX(1, "bounds"),
        SHAPE(0, "shape"),
        ;

        public final int id;
        public final String key;

        PreviewType(int id, String key)
        {
            this.id = id;
            this.key = key;
        }

        public static String[] keys()
        {
            PreviewType[] values = values();
            String[] keys = new String[values.length];
            for (int i = 0; i < values.length; i++)
                keys[i] = values[i].key;
            return keys;
        }

        public static PreviewType findOrDefault(String key, PreviewType defaultType)
        {
            PreviewType type = find(key);
            return type != null ? type : defaultType;
        }

        public static PreviewType find(String key)
        {
            for (PreviewType type : values())
                if (type.key.equals(key))
                    return type;

            return null;
        }
    }
}
