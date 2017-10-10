/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.cell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by lukas on 02.06.14.
 */
public abstract class TableCellPropertyDefault<P> extends TableCellDefault implements TableCellProperty<P>
{
    protected P property;

    private List<Consumer<P>> listeners = new ArrayList<>();

    public TableCellPropertyDefault(String id, P value)
    {
        super(id);
        setPropertyValue(value);
    }

    public Consumer<P> addPropertyConsumer(Consumer<P> consumer)
    {
        listeners.add(consumer);
        return consumer;
    }

    public void removePropertyListener(Consumer<P> listener)
    {
        listeners.remove(listener);
    }

    public List<Consumer<P>> getListeners()
    {
        return Collections.unmodifiableList(listeners);
    }

    protected void alertListenersOfChange()
    {
        for (Consumer<P> listener : listeners)
        {
            //noinspection unchecked
            listener.accept(getPropertyValue());
        }
    }

    @Override
    public P getPropertyValue()
    {
        return property;
    }

    @Override
    public void setPropertyValue(P value)
    {
        this.property = value;
    }
}
