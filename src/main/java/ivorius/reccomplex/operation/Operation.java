/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.operation;

import ivorius.ivtoolkit.tools.NBTCompoundObject;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by lukas on 10.02.15.
 */
public interface Operation extends NBTCompoundObject
{
    void perform(WorldServer world);

    default void update(World world, int ticks) {}

    @SideOnly(Side.CLIENT)
    void renderPreview(PreviewType previewType, World world, int ticks, float partialTicks);

    enum PreviewType
    {
        NONE("none"),
        BOUNDING_BOX("bounds"),
        SHAPE("shape"),
        ;

        public final String key;

        PreviewType(String key)
        {
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
