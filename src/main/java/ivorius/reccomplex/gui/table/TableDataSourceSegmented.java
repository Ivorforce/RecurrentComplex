/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

import com.google.common.primitives.Ints;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;

import java.util.Arrays;
import java.util.List;

/**
 * Created by lukas on 22.06.14.
 */
public class TableDataSourceSegmented implements TableDataSource
{
    protected final TIntObjectMap<TableDataSource> managedSections = new TIntObjectHashMap<>();

    public TableDataSourceSegmented()
    {
    }

    public TableDataSourceSegmented(List<TableDataSource> segments)
    {
        for (int i = 0; i < segments.size(); i++)
            managedSections.put(i, segments.get(i));
    }

    public TableDataSourceSegmented(TableDataSource... segments)
    {
        this(Arrays.asList(segments));
    }

    public void addManagedSection(int section, TableDataSource source)
    {
        managedSections.put(section, source);
    }

    public void removeManagedSection(int section)
    {
        managedSections.remove(section);
    }

    public TIntSet managedSections()
    {
        return managedSections.keySet();
    }

    @Override
    public int numberOfElements()
    {
        int elements = 0;

        int segments = numberOfSegments();
        for (int i = 0; i < segments; i++)
            elements += sizeOfSegment(i);

        return elements;
    }

    @Override
    public TableElement elementForIndex(GuiTable table, int index)
    {
        for (int seg = 0; seg < numberOfSegments(); seg++)
        {
            if (index - sizeOfSegment(seg) < 0)
                return elementForIndexInSegment(table, index, seg);

            index -= sizeOfSegment(seg);
        }

        return null;
    }

    public int numberOfSegments()
    {
        return managedSections.isEmpty() ? 0 : Ints.max(managedSections.keys()) + 1;
    }

    public int sizeOfSegment(int segment)
    {
        TableDataSource managed = managedSections.get(segment);
        return managed != null ? managed.numberOfElements() : 0;
    }

    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        TableDataSource managed = managedSections.get(segment);
        return managed != null ? managed.elementForIndex(table, index) : null;
    }
}
