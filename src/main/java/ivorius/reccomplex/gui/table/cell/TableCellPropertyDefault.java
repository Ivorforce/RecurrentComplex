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

    private List<TableCellPropertyListener> listeners = new ArrayList<>();

    public TableCellPropertyDefault(String id, P value)
    {
        super(id);
        setPropertyValue(value);
    }

    public TableCellPropertyListener addPropertyConsumer(Consumer<P> consumer)
    {
        TableCellPropertyListener listener = cell -> consumer.accept(property);
        listeners.add(listener);
        return listener;
    }

    public void addPropertyListener(TableCellPropertyListener listener)
    {
        listeners.add(listener);
    }

    public void removePropertyListener(TableCellPropertyListener listener)
    {
        listeners.remove(listener);
    }

    public List<TableCellPropertyListener> getListeners()
    {
        return Collections.unmodifiableList(listeners);
    }

    protected void alertListenersOfChange()
    {
        for (TableCellPropertyListener listener : listeners)
        {
            //noinspection unchecked
            listener.valueChanged(this);
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
