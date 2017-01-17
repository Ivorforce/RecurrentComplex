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
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by lukas on 17.01.17.
 */
public class TableDataSourceNBTList extends TableDataSourceSegmented
{
    protected TableDelegate delegate;
    protected TableNavigator navigator;

    protected NBTTagList nbt;

    protected String earlierTitle = TextFormatting.BOLD + "↑";
    protected String laterTitle = TextFormatting.BOLD + "↓";
    protected String deleteTitle = TextFormatting.RED + "X";
    protected String addTitle = TextFormatting.GREEN + "+";

    public TableDataSourceNBTList(TableDelegate delegate, TableNavigator navigator, NBTTagList nbt)
    {
        this.delegate = delegate;
        this.navigator = navigator;
        this.nbt = nbt;
    }

    public String getEarlierTitle()
    {
        return earlierTitle;
    }

    public void setEarlierTitle(String earlierTitle)
    {
        this.earlierTitle = earlierTitle;
    }

    public String getLaterTitle()
    {
        return laterTitle;
    }

    public void setLaterTitle(String laterTitle)
    {
        this.laterTitle = laterTitle;
    }

    public String getDeleteTitle()
    {
        return deleteTitle;
    }

    public void setDeleteTitle(String deleteTitle)
    {
        this.deleteTitle = deleteTitle;
    }

    public String getAddTitle()
    {
        return addTitle;
    }

    public void setAddTitle(String addTitle)
    {
        this.addTitle = addTitle;
    }

    @Nonnull
    @Override
    public String title()
    {
        return "NBT List";
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
                ? nbt.tagCount()
                : 1;
    }

    @Override
    public TableCell cellForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            return new TableCellPresetAction(null,
                    IntStream.range(0, 12)
                            .mapToObj((id) -> TableDataSourceNBT.typeButton(id, () ->
                            {
                                while (!nbt.hasNoTags())
                                    nbt.removeTag(nbt.tagCount() - 1);
                                ReflectionHelper.setPrivateValue(NBTTagList.class, nbt, (byte) id, "field_74746_b", "tagType");
                                delegate.reloadData();
                            }))
                            .collect(Collectors.toList())
            );
        }
        else if (segment == 1 || segment == 3)
        {
            return TableDataSourceNBT.addButton(nbt.getTagType(), n ->
            {
                if (segment == 1)
                {
                    // Hax
                    List<NBTBase> cur = new ArrayList<>(nbt.tagCount());
                    while (!nbt.hasNoTags())
                        cur.add(nbt.removeTag(nbt.tagCount() - 1));
                    nbt.appendTag(n);
                    for (NBTBase prev : cur)
                        nbt.appendTag(prev);
                }
                else
                    nbt.appendTag(n);

                delegate.reloadData();
            });
        }
        else if (segment == 2)
        {
            TableCellMulti multi = new TableCellMulti(getEntryActions(index));
            multi.setSize(0, 8);
            return new TitledCell("" + index, multi);
        }

        return super.cellForIndexInSegment(table, index, segment);
    }

    public List<TableCell> getEntryActions(int index)
    {
        boolean enabled = true;
        NBTBase cellNBT = nbt.get(index);

        TableCell entryCell = TableDataSourceNBT.cell(cellNBT, delegate, navigator);

        TableCellButton earlier = new TableCellButton("", "earlier", getEarlierTitle(), index > 0 && enabled);
        earlier.addAction(() ->
        {
            nbt.set(index, nbt.get(index - 1));
            nbt.set(index - 1, cellNBT);
            delegate.reloadData();
        });

        TableCellButton later = new TableCellButton("", "later", getLaterTitle(), index < nbt.tagCount() - 1 && enabled);
        later.addAction(() ->
        {
            nbt.set(index, nbt.get(index + 1));
            nbt.set(index + 1, cellNBT);
            delegate.reloadData();
        });

        TableCellButton delete = new TableCellButton("", "delete", getDeleteTitle(), enabled);
        delete.addAction(() ->
        {
            nbt.removeTag(index);
            delegate.reloadData();
        });

        return Arrays.asList(entryCell, earlier, later, delete);
    }
}
