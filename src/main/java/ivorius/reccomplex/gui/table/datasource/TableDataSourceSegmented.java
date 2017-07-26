/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.datasource;

import com.google.common.primitives.Ints;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import ivorius.reccomplex.gui.GuiHider;
import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.gui.table.cell.TableCell;

import javax.annotation.Nonnull;
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

    public void addManagedSegment(int section, TableDataSource source)
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

    @Nonnull
    @Override
    public String title()
    {
        for (int i : managedSections.keys())
        {
            String title = managedSections.get(i).title();
            if (!title.trim().isEmpty())
                return title;
        }

        return "";
    }

    @Override
    public int numberOfCells()
    {
        int cells = 0;

        int segments = numberOfSegments();
        for (int i = 0; i < segments; i++)
            cells += sizeOfSegment(i);

        return cells;
    }

    @Override
    public TableCell cellForIndex(GuiTable table, int index)
    {
        for (int seg = 0; seg < numberOfSegments(); seg++)
        {
            if (index - sizeOfSegment(seg) < 0)
                return cellForIndexInSegment(table, index, seg);

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
        return managed != null ? managed.numberOfCells() : 0;
    }

    public TableCell cellForIndexInSegment(GuiTable table, int index, int segment)
    {
        TableDataSource managed = managedSections.get(segment);
        return managed != null ? managed.cellForIndex(table, index) : null;
    }

    @Override
    public boolean canVisualize()
    {
        return managedSections.valueCollection().stream()
                .filter(s -> s.canVisualize())
                .count() == 1;
    }

    @Override
    public GuiHider.Visualizer visualizer()
    {
        return managedSections.valueCollection().stream()
                .filter(s -> s.canVisualize())
                .findFirst().orElseThrow(InternalError::new)
                .visualizer();
    }
}
