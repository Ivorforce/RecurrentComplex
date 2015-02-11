/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic;

import ivorius.reccomplex.dimensions.DimensionDictionary;
import net.minecraft.world.WorldProvider;

import java.util.Arrays;
import java.util.List;

/**
 * Created by lukas on 19.09.14.
 */
public class DimensionSelector
{
    private String dimensionID;

    public DimensionSelector(String dimensionID)
    {
        this.dimensionID = dimensionID;
    }

    public String getDimensionID()
    {
        return dimensionID;
    }

    public void setDimensionID(String dimensionID)
    {
        this.dimensionID = dimensionID;
    }

    public List<String> getDimensionTypes()
    {
        if (dimensionID.startsWith("Type:"))
            return Arrays.asList(dimensionID.substring(5).split(","));

        return null;
    }

    public boolean matches(WorldProvider provider)
    {
        try
        {
            Integer dimensionIDInt = Integer.valueOf(this.dimensionID);
            return dimensionIDInt == provider.dimensionId;
        }
        catch (NumberFormatException ignored)
        {

        }

        List<String> types = getDimensionTypes();
        return types != null && DimensionDictionary.dimensionMatchesAllTypes(provider, types);
    }

    public boolean isTypeList()
    {
        return dimensionID.startsWith("Type:");
    }
}
