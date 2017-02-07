/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.algebra;

import java.util.function.Supplier;

/**
 * Created by lukas on 09.01.17.
 */
public class SupplierCache<T>
{
    private Supplier<? extends T> supplier;
    private T t;

    private SupplierCache(Supplier<? extends T> supplier)
    {
        this.supplier = supplier;
    }

    public static <T> SupplierCache<T> of(Supplier<? extends T> supplier)
    {
        return new SupplierCache<>(supplier);
    }

    public static <T> SupplierCache<T> direct(T t)
    {
        return new SupplierCache<>(() -> t);
    }

    public T get()
    {
        return t != null ? t : (t = supplier.get());
    }
}
