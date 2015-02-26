/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.generic.BiomeGenerationInfo;
import ivorius.reccomplex.structures.generic.gentypes.BiomeMatcherPresets;
import ivorius.reccomplex.utils.IvTranslations;
import net.minecraft.util.StatCollector;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceBiomeGenList extends TableDataSourceList<BiomeGenerationInfo, List<BiomeGenerationInfo>>
{
    public TableDataSourceBiomeGenList(List<BiomeGenerationInfo> list, TableDelegate tableDelegate, TableNavigator navigator)
    {
        super(list, tableDelegate, navigator);
    }

    @Override
    public int numberOfSegments()
    {
        return super.numberOfSegments() + 1;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return segment == 0 ? 1 : super.sizeOfSegment(segment);
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
        Collection<String> allTypes = BiomeMatcherPresets.instance().allTypes();
        List<TableElementButton.Action> actions = new ArrayList<>(allTypes.size());
        for (String type : allTypes)
        {
            String baseKey = "reccomplex.biomePreset." + type;
            actions.add(new TableElementButton.Action(type,
                    StatCollector.translateToLocal(baseKey),
                    IvTranslations.formatLines(baseKey + ".tooltip")
            ));
        }
        return actions.toArray(new TableElementButton.Action[actions.size()]);
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            TableElementPresetAction elementPresetAction = new TableElementPresetAction("biomePreset", "Presets", "Apply", getPresetActions());
            elementPresetAction.addListener(this);
            return elementPresetAction;
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    @Override
    public String getDisplayString(BiomeGenerationInfo biomeGenerationInfo)
    {
        return StringUtils.abbreviate(biomeGenerationInfo.getDisplayString(), 16) + " (" + biomeGenerationInfo.getActiveGenerationWeight() + ")";
    }

    @Override
    public BiomeGenerationInfo newEntry(String actionID)
    {
        return new BiomeGenerationInfo("", null);
    }

    @Override
    public TableDataSource editEntryDataSource(BiomeGenerationInfo entry)
    {
        return new TableDataSourceBiomeGen(entry, tableDelegate);
    }

    @Override
    public void actionPerformed(TableElement element, String actionID)
    {
        if (element.getID().equals("biomePreset"))
        {
            list.clear();
            List<BiomeGenerationInfo> preset = BiomeMatcherPresets.instance().preset(actionID);

            if (preset != null)
            {
                list.addAll(preset);
                tableDelegate.reloadData();
            }
        }

        super.actionPerformed(element, actionID);
    }
}
