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
import ivorius.reccomplex.world.gen.feature.structure.generic.WeightedBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by lukas on 17.01.17.
 */

@SideOnly(Side.CLIENT)
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

        resetSortedKeys();
    }

    protected void resetSortedKeys()
    {
        sortedKeys.clear();
        sortedKeys.addAll(this.nbt.getKeySet());
        sortedKeys.sort(String::compareToIgnoreCase);
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
        return 4;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return segment == 2
                ? nbt.getKeySet().size()
                : 1;
    }

    @Override
    public TableCell cellForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            TableCellButton load = new TableCellButton(null, "toString", "->", Collections.singletonList("Convert to String (slightly lossy)"), true);

            TableCellButton perform = new TableCellButton(null, "fromString", "O", Collections.singletonList("Load from String"), false);

            TableCellString cell = new TableCellString("tileEntityInfo", "");
            cell.addListener(val -> perform.setEnabled(WeightedBlockState.tryParse(val) != null));
            cell.setMaxStringLength(32500); // FromGuiCommandBlock

            load.addAction(() -> {
                cell.setPropertyValue(nbt.toString());
                perform.setEnabled(true);
            });

            perform.addAction(() ->
            {
                NBTTagCompound other = WeightedBlockState.tryParse(cell.getPropertyValue());
                if (other != null)
                {
                    nbt.getKeySet().clear();
                    other.getKeySet().forEach(s -> nbt.setTag(s, other.getTag(s)));

                    resetSortedKeys();

                    delegate.reloadData();
                }
            });

            TableCellMulti multi = new TableCellMulti(load, cell, perform);
            multi.setSize(0, 0.1f);
            multi.setSize(2, 0.1f);
            return new TitledCell("As String", multi);
        }
        else if (segment == 1 || segment == 3)
        {
            return new TableCellPresetAction(null,
                    IntStream.range(0, 12)
                            .mapToObj((id) -> TableDataSourceNBT.addButton(id, n ->
                            {
                                nbt.setTag("", n);
                                sortedKeys.removeIf(s -> s.equals(""));
                                sortedKeys.add(segment == 1 ? 0 : sortedKeys.size(), "");
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
            setKeyCell.addAction(() ->
            {
                sortedKeys.set(index, nextKey[0]);
                sortedKeys.subList(index + 1, sortedKeys.size()).remove(nextKey[0]); // If there was a previous entry
                sortedKeys.subList(0, index).remove(nextKey[0]);
                nbt.removeTag(key);
                nbt.setTag(nextKey[0], cellNBT);
                delegate.reloadData();
            });

            keyCell.addListener(value ->
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
