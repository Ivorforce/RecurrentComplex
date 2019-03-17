/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.tweak;

import gnu.trove.map.TObjectFloatMap;
import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.cell.TableCellFloatNullable;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.utils.scale.Scales;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class TableDataSourceTweakStructuresList extends TableDataSourceSegmented
{
    protected TObjectFloatMap<String> tweaks;

    protected List<String> editingIDs = new ArrayList<>();
    protected TableDelegate delegate;

    public TableDataSourceTweakStructuresList(TableDelegate delegate, TObjectFloatMap<String> tweaks)
    {
        this.tweaks = tweaks;
        this.delegate = delegate;

        _search(null, false);
    }

    public void search(@Nullable String search)
    {
        _search(search, true);
    }

    protected void _search(@Nullable String search, boolean reload)
    {
        editingIDs.clear();
        editingIDs.addAll(StructureRegistry.INSTANCE.ids());

        if (search != null) {
            editingIDs.removeIf(id -> !id.contains(search));
        }

        editingIDs.sort(String::compareTo);

        if (reload) {
            delegate.reloadData();
        }
    }

    @Override
    public TableCell cellForIndexInSegment(GuiTable table, int index, int segment)
    {
        String id = editingIDs.get(index);

        TableCellFloatNullable tweakCell = new TableCellFloatNullable(null, getTweak(id), 1, 0, 10, "D", "T");
        tweakCell.setScale(Scales.pow(5));
        tweakCell.addListener(value -> tweaks.put(id, value));

        return new TitledCell(id, tweakCell);
    }

    public Float getTweak(String id)
    {
        return tweaks.containsKey(id)
                ? tweaks.get(id)
                : null;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return editingIDs.size();
    }

    @Override
    public int numberOfSegments()
    {
        return 1;
    }
}
