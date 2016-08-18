/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.utils.PresettedList;
import ivorius.ivtoolkit.tools.IvTranslations;
import net.minecraft.util.text.translation.I18n;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lukas on 04.06.14.
 */
public abstract class TableDataSourcePresettedList<T> extends TableDataSourceList<T, List<T>> implements TableCellActionListener
{
    public PresettedList<T> presettedList;

    public TableDataSourcePresettedList(PresettedList<T> list, TableDelegate tableDelegate, TableNavigator navigator)
    {
        super(list.getList(), tableDelegate, navigator);
        this.presettedList = list;
    }

    @Override
    public boolean canEditList()
    {
        return presettedList.isCustom();
    }

    @Override
    public int numberOfSegments()
    {
        return super.numberOfSegments() + 1;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return segment == 0 ? 2 : super.sizeOfSegment(segment);
    }

    @Override
    public boolean isListSegment(int segment)
    {
        return segment == 2;
    }

    @Override
    public int getAddIndex(int segment)
    {
        return super.getAddIndex(segment - 1);
    }

    public TableCellButton.Action[] getPresetActions()
    {
        Collection<String> allTypes = presettedList.getListPresets().allTypes();
        List<TableCellButton.Action> actions = new ArrayList<>(allTypes.size());

        String baseKey = getBasePresetKey();
        actions.addAll(allTypes.stream().map(type -> new TableCellButton.Action(type,
                I18n.translateToLocal(baseKey + type),
                IvTranslations.formatLines(baseKey + type + ".tooltip")
        )).collect(Collectors.toList()));
        return actions.toArray(new TableCellButton.Action[actions.size()]);
    }

    protected abstract String getBasePresetKey();

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            if (index == 0)
            {
                TableCellPresetAction cell = new TableCellPresetAction("preset", "Apply", getPresetActions());
                cell.addListener(this);
                return new TableElementCell("Presets", cell);
            }
            else if (index == 1)
            {
                String title = !presettedList.isCustom() ? I18n.translateToLocal(getBasePresetKey() + presettedList.getPreset()) : "Custom";
                TableCellButton cell = new TableCellButton("customize", new TableCellButton.Action("customize", "Customize", !presettedList.isCustom()));
                cell.addListener(this);
                return new TableElementCell(title, cell);
            }
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    @Override
    public void actionPerformed(TableCell cell, String actionID)
    {
        if ("preset".equals(cell.getID()))
        {
            presettedList.setPreset(actionID);
            tableDelegate.reloadData();
        }
        else if (actionID.equals("customize"))
        {
            presettedList.setToCustom();
            tableDelegate.reloadData();
        }

        super.actionPerformed(cell, actionID);
    }
}
