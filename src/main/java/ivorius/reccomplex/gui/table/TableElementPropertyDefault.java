/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 02.06.14.
 */
public abstract class TableElementPropertyDefault<P> extends TableElementDefault implements TableElementProperty<P>
{
    protected P property;

    private List<TableElementPropertyListener> listeners = new ArrayList<>();

    public TableElementPropertyDefault(String id, String title, P value)
    {
        super(id, title);
        setPropertyValue(value);
    }

    public void addPropertyListener(TableElementPropertyListener listener)
    {
        listeners.add(listener);
    }

    public void removePropertyListener(TableElementPropertyListener listener)
    {
        listeners.remove(listener);
    }

    public List<TableElementPropertyListener> getListeners()
    {
        return Collections.unmodifiableList(listeners);
    }

    protected void alertListenersOfChange()
    {
        for (TableElementPropertyListener listener : listeners)
        {
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
