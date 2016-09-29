/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.files;

/**
 * Created by lukas on 29.09.16.
 */
public interface LeveledRegistry<S>
{
    S register(String id, String domain, S s, boolean active, ILevel level);

    S unregister(String id, ILevel level);

    void clear(ILevel level);

    interface ILevel
    {
        int getLevel();
    }

    enum Level implements ILevel
    {
        INTERNAL, MODDED, CUSTOM;

        @Override
        public int getLevel()
        {
            return ordinal();
        }

        public boolean isCustom()
        {
            return this == CUSTOM;
        }
    }
}
