/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.utils.PresettedList;
import ivorius.reccomplex.utils.IvTranslations;
import net.minecraft.util.StatCollector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
public abstract class TableDataSourcePresettedList<T> extends TableDataSourceList<T, List<T>> implements TableElementActionListener
{
    public PresettedList<T> presettedList;

    public TableDataSourcePresettedList(PresettedList<T> list, TableDelegate tableDelegate, TableNavigator navigator)
    {
        super(list.list, tableDelegate, navigator);
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
        return segment == 1
                ? 0
                : segment == 3
                ? (list.size() > 0 ? list.size() : -1)
                : -1;
    }

    public TableElementButton.Action[] getPresetActions()
    {
        Collection<String> allTypes = presettedList.getListPresets().allTypes();
        List<TableElementButton.Action> actions = new ArrayList<>(allTypes.size());

        String baseKey = getBasePresetKey();
        for (String type : allTypes)
        {
            actions.add(new TableElementButton.Action(type,
                    StatCollector.translateToLocal(baseKey + type),
                    IvTranslations.formatLines(baseKey + type + ".tooltip")
            ));
        }
        return actions.toArray(new TableElementButton.Action[actions.size()]);
    }

    protected abstract String getBasePresetKey();

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            if (index == 0)
            {
                TableElementPresetAction element = new TableElementPresetAction("preset", "Presets", "Apply", getPresetActions());
                element.addListener(this);
                return element;
            }
            else if (index == 1)
            {
                String title = !presettedList.isCustom() ? StatCollector.translateToLocal(getBasePresetKey() + presettedList.getPreset()) : "Custom";
                TableElementButton element = new TableElementButton("customize", title, new TableElementButton.Action("customize", "Customize", !presettedList.isCustom()));
                element.addListener(this);
                return element;
            }
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    @Override
    public void actionPerformed(TableElement element, String actionID)
    {
        if (element.getID().equals("preset"))
        {
            presettedList.setPreset(actionID);
            tableDelegate.reloadData();
        }
        else if (actionID.equals("customize"))
        {
            presettedList.setToCustom();
            tableDelegate.reloadData();
        }

        super.actionPerformed(element, actionID);
    }
}
