/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator.reachability;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.realmsclient.gui.ChatFormatting;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.maze.SavedMazePath;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by lukas on 16.03.16.
 */
public class TableDataSourceMazeReachabilityGroups extends TableDataSourceSegmented implements TableCellActionListener
{
    private List<Set<SavedMazePath>> groups;
    private Set<SavedMazePath> expected;

    private TableDelegate tableDelegate;
    private TableNavigator tableNavigator;

    public TableDataSourceMazeReachabilityGroups(List<Set<SavedMazePath>> groups, Set<SavedMazePath> expected, TableDelegate tableDelegate, TableNavigator tableNavigator)
    {
        this.groups = groups;
        this.expected = expected;
        this.tableDelegate = tableDelegate;
        this.tableNavigator = tableNavigator;
    }

    public List<Set<SavedMazePath>> getGroups()
    {
        return groups;
    }

    public void setGroups(List<Set<SavedMazePath>> groups)
    {
        this.groups = groups;
    }

    public TableDelegate getTableDelegate()
    {
        return tableDelegate;
    }

    public void setTableDelegate(TableDelegate tableDelegate)
    {
        this.tableDelegate = tableDelegate;
    }

    public TableNavigator getTableNavigator()
    {
        return tableNavigator;
    }

    public void setTableNavigator(TableNavigator tableNavigator)
    {
        this.tableNavigator = tableNavigator;
    }

    @Override
    public int numberOfSegments()
    {
        return 2 + groups.size() * 2;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return (segment % 2 == 0) ? 1 : getVirtualGroup(segment / 2 - 1).size();
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        int group = segment / 2 - 1;

        if (segment % 2 == 0)
            return new TableElementCell(new TableCellTitle("groupTitle" + group, group < 0
                    ? IvTranslations.get("reccomplex.reachability.groups.default")
                    : IvTranslations.format("reccomplex.reachability.groups.group", "" + (group + 1))));
        else
        {
            SavedMazePath t = getVirtualGroup(group).get(index);

            TableCellButton[] entryActions = getEntryActions(group, index);
            for (TableCellButton entryAction : entryActions)
            {
                entryAction.addListener(this);
                entryAction.setId(String.format("entry%d,%d", group, index));
            }
            return new TableElementCell(getDisplayString(t), new TableCellMulti(entryActions));
        }
    }

    public TableCellButton[] getEntryActions(int group, int index)
    {
        boolean first = group < 0;
        boolean second = group == 0;
        boolean last = group == groups.size() - 1;
        List<SavedMazePath> groupL = getVirtualGroup(group);

        boolean enabled = true;
        return new TableCellButton[]{
                new TableCellButton("", second ? "default" : "earlier", IvTranslations.get("reccomplex.reachability.groups.previous"), enabled && !first),
                new TableCellButton("", last ? "new" : "later", last
                        ? IvTranslations.get("reccomplex.reachability.groups.new")
                        : IvTranslations.get("reccomplex.reachability.groups.next"),
                        enabled && (!last || groupL.size() > 1))
        };
    }

    private List<SavedMazePath> getVirtualGroup(int group)
    {
        Set<SavedMazePath> paths = group < 0 ? defaultGroup() : groups.get(group);
        List<SavedMazePath> sorted = Lists.newArrayList(paths);
        Collections.sort(sorted); // Meh

        return sorted;
    }

    private Set<SavedMazePath> defaultGroup()
    {
        Set<SavedMazePath> view = expected;
        for (Set<SavedMazePath> group : groups)
            view = Sets.difference(view, group);
        return view;
    }

    private String getDisplayString(SavedMazePath path)
    {
        return String.format("%s%s%s %s", ChatFormatting.BLUE, path.getEnumFacing(), ChatFormatting.RESET, path.getSourceRoom());
    }

    @Override
    public void actionPerformed(TableCell cell, String action)
    {
        if (cell.getID() != null && cell.getID().startsWith("entry"))
        {
            String[] split = cell.getID().substring(5).split(",");
            int groupIndex = Integer.valueOf(split[0]);
            int index = Integer.valueOf(split[1]);

            SavedMazePath element = getVirtualGroup(groupIndex).get(index);
            if (groupIndex >= 0)
                groups.get(groupIndex).remove(element);

            switch (action)
            {
                case "earlier":
                    groups.get(groupIndex - 1).add(element);
                    break;
                case "later":
                    groups.get(groupIndex + 1).add(element);
                    break;
                case "new":
                    groups.add(Sets.newHashSet(element));
                    break;
            }

            if (groupIndex >= 0 && groups.get(groupIndex).isEmpty())
                groups.remove(groupIndex);

            tableDelegate.reloadData();
        }
    }
}
