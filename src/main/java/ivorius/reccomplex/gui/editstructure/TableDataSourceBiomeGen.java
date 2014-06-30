/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.worldgen.genericStructures.BiomeGenerationInfo;
import net.minecraft.world.biome.BiomeGenBase;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBiomeGen implements TableDataSource, TableElementPropertyListener
{
    private BiomeGenerationInfo generationInfo;

    private TableDelegate tableDelegate;

    public TableDataSourceBiomeGen(BiomeGenerationInfo generationInfo, TableDelegate tableDelegate)
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
            TableElementString element = new TableElementString("biomeID", "Biome ID", generationInfo.getBiomeID());
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
        if ("biomeID".equals(element.getID()))
        {
            generationInfo.setBiomeID((String) element.getPropertyValue());
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
        if (generationInfo.getBiomeTypes() != null)
        {
            return GuiValidityStateIndicator.State.VALID;
        }

        String biomeID = generationInfo.getBiomeID();
        BiomeGenBase[] biomes = BiomeGenBase.getBiomeGenArray();

        for (BiomeGenBase biome : biomes)
        {
            if (biome != null && biome.biomeName.equals(biomeID))
            {
                return GuiValidityStateIndicator.State.VALID;
            }
        }

        return biomeID.trim().length() > 0 ? GuiValidityStateIndicator.State.SEMI_VALID : GuiValidityStateIndicator.State.INVALID;
    }
}
