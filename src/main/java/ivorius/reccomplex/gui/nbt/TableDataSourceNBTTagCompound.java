/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.nbt;

import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.*;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import net.minecraft.nbt.*;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by lukas on 17.01.17.
 */
public class TableDataSourceNBTTagCompound extends TableDataSourceSegmented
{
    protected TableDelegate delegate;
    protected TableNavigator navigator;
    protected NBTTagCompound nbt;
    protected final List<String> sortedKeys = new ArrayList<>();

    public TableDataSourceNBTTagCompound(TableDelegate delegate, TableNavigator navigator, NBTTagCompound nbt)
    {
        this.delegate = delegate;
        this.navigator = navigator;
        this.nbt = nbt;

        sortedKeys.addAll(nbt.getKeySet());
    }

    public NBTTagCompound getNbt()
    {
        return nbt;
    }

    public void setNbt(NBTTagCompound nbt)
    {
        this.nbt = nbt;
    }

    @Nonnull
    @Override
    public String title()
    {
        return "NBT Compound";
    }

    @Override
    public int numberOfSegments()
    {
        return 3;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return segment == 1
                ? nbt.getKeySet().size()
                : 1;
    }

    @Override
    public TableCell cellForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0 || segment == 2)
        {

            return new TableCellPresetAction(null,
                    IntStream.range(0, 12)
                            .mapToObj((id) -> TableDataSourceNBT.addButton(id, n ->
                            {
                                nbt.setTag("", n);
                                sortedKeys.removeIf(s -> s.equals(""));
                                sortedKeys.add(segment == 0 ? 0 : sortedKeys.size(), "");
                                delegate.reloadData();
                            }))
                            .collect(Collectors.toList())
            );
        }
        else
        {
            String key = sortedKeys.get(index);
            String[] nextKey = new String[]{key};

            NBTBase cellNBT = nbt.getTag(key);
            TableCell nbtCell = TableDataSourceNBT.cell(cellNBT, delegate, navigator);

            TableCellString keyCell = new TableCellString(null, key);

            TableCellButton setKeyCell = new TableCellButton(null, "setKey", "O", Collections.singletonList("Set Key"), false);
            setKeyCell.addAction(() -> {
                sortedKeys.set(index, nextKey[0]);
                sortedKeys.subList(index + 1, sortedKeys.size()).remove(nextKey[0]); // If there was a previous entry
                sortedKeys.subList(0, index).remove(nextKey[0]);
                nbt.removeTag(key);
                nbt.setTag(nextKey[0], cellNBT);
                delegate.reloadData();
            });

            keyCell.addPropertyConsumer(value ->
            {
                nextKey[0] = value;
                setKeyCell.setEnabled(true);
            });

            TableCellButton deleteCell = new TableCellButton(null, null, TextFormatting.RED + "-");
            deleteCell.addAction(() ->
            {
                sortedKeys.remove(key);
                nbt.removeTag(key);
                delegate.reloadData();
            });

            TableCellMulti multi = new TableCellMulti(keyCell, setKeyCell, nbtCell, deleteCell);
            multi.setSize(1, 0.2f);
            multi.setSize(3, 0.2f);
            return multi;
        }
    }
}
