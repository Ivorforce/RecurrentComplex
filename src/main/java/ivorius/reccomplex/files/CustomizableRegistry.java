/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.files;

/**
 * Created by lukas on 29.09.16.
 */
public interface CustomizableRegistry<S>
{
    S register(String id, String domain, S s, boolean active, boolean custom);

    S unregister(String id, boolean custom);

    void clearCustomFiles();
}
