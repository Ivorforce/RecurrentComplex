/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import com.google.common.primitives.Ints;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import ivorius.reccomplex.dimensions.DimensionDictionary;
import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.worldgen.genericStructures.BiomeGenerationInfo;
import ivorius.reccomplex.worldgen.genericStructures.DimensionGenerationInfo;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.DimensionManager;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceDimensionGen implements TableDataSource, TableElementPropertyListener
{
    private DimensionGenerationInfo generationInfo;

    private TableDelegate tableDelegate;

    public TableDataSourceDimensionGen(DimensionGenerationInfo generationInfo, TableDelegate tableDelegate)
    {
        this.generationInfo = generationInfo;
        this.tableDelegate = tableDelegate;
    }

    @Override
    public boolean has(GuiTable table, int index)
    {
        return index >= 0 && index < 3;
    }

    @Override
    public TableElement elementForIndex(GuiTable table, int index)
    {
        if (index == 0)
        {
            TableElementString element = new TableElementString("dimID", "Dimension ID", generationInfo.getDimensionID());
            element.setShowsValidityState(true);
            element.setValidityState(currentBiomeIDState());
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 1)
        {
            TableElementBoolean element = new TableElementBoolean("defaultWeight", "Use Default Weight", generationInfo.hasDefaultWeight());
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 2)
        {
            TableElementInteger element = new TableElementInteger("weight", "Weight", generationInfo.getActiveGenerationWeight(), 0, 500);
            element.setEnabled(!generationInfo.hasDefaultWeight());
            element.addPropertyListener(this);
            return element;
        }

        return null;
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        if ("dimID".equals(element.getID()))
        {
            generationInfo.setDimensionID((String) element.getPropertyValue());
            ((TableElementString) element).setValidityState(currentBiomeIDState());
        }
        else if ("defaultWeight".equals(element.getID()))
        {
            boolean useDefault = (boolean) element.getPropertyValue();
            generationInfo.setGenerationWeight(useDefault ? null : generationInfo.getActiveGenerationWeight());
            tableDelegate.reloadData();
        }
        else if ("weight".equals(element.getID()))
        {
            generationInfo.setGenerationWeight((Integer) element.getPropertyValue());
        }
    }

    private GuiValidityStateIndicator.State currentBiomeIDState()
    {
        if (generationInfo.isTypeList())
        {
            for (String s : generationInfo.allTypes())
                if (allDimensionsOfType(s).size() == 0)
                    return GuiValidityStateIndicator.State.SEMI_VALID;

            return GuiValidityStateIndicator.State.VALID;
        }

        String dimIDString = generationInfo.getDimensionID();

        try
        {
            int dimID = Integer.valueOf(dimIDString);

            for (int eID : DimensionManager.getIDs())
                if (dimID == eID)
                    return GuiValidityStateIndicator.State.SEMI_VALID;

            return GuiValidityStateIndicator.State.VALID;
        }
        catch (NumberFormatException ignored)
        {

        }


        return GuiValidityStateIndicator.State.INVALID;
    }

    private TIntList allDimensionsOfType(String type)
    {
        TIntList intList = new TIntArrayList();
        for (int d : DimensionManager.getIDs())
        {
            if (DimensionDictionary.dimensionMatchesType(d, type))
                intList.add(d);
        }
        return intList;
    }
}
