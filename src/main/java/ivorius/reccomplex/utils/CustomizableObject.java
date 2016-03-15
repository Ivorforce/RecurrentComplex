/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

/**
 * Created by lukas on 15.03.16.
 */
public class CustomizableObject<V>
{
    public V solid;
    public V custom;

    public CustomizableObject(V solid, V custom)
    {
        this.solid = solid;
        this.custom = custom;
    }

    public V get(boolean custom)
    {
        return custom ? this.custom : this.solid;
    }
}
