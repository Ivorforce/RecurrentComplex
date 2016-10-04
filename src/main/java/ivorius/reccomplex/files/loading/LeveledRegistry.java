/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.files.loading;

import java.util.Set;

/**
 * Created by lukas on 29.09.16.
 */
public interface LeveledRegistry<S>
{
    S register(String id, String domain, S s, boolean active, ILevel level);

    S unregister(String id, ILevel level);

    S get(String id);

    Status status(String id);

    Set<String> ids();

    void clear(ILevel level);

    interface ILevel
    {
        int getLevel();
    }

    interface Status
    {
        String getId();

        boolean isActive();

        void setActive(boolean active);

        String getDomain();

        ILevel getLevel();
    }

    enum Level implements ILevel
    {
        INTERNAL, MODDED, CUSTOM, SERVER;

        @Override
        public int getLevel()
        {
            return ordinal();
        }

        public boolean isCustom()
        {
            return this == CUSTOM;
        }

        public boolean isServer()
        {
            return this == SERVER;
        }
    }
}
