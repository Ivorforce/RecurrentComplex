/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

/**
 * Visits objects of type T.
 *
 * @param <T> The visited type.
 */
public interface Visitor<T>
{
    /**
     * Visits the object.
     * @param t The object.
     * @return True if the visitor should continue, otherwise false.
     */
    boolean visit(T t);
}
