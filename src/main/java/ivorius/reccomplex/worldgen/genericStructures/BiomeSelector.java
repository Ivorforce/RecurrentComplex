/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.genericStructures;

import ivorius.reccomplex.json.RCGsonHelper;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;

import java.util.*;

/**
 * Created by lukas on 19.09.14.
 */
public class BiomeSelector
{
    private String biomeID;

    public BiomeSelector(String biomeID)
    {
        this.biomeID = biomeID;
    }

    public static Set<BiomeGenBase> gatherAllBiomes()
    {
        Set<BiomeGenBase> set = new HashSet<>();

        for (BiomeGenBase biomeGenBase : BiomeGenBase.getBiomeGenArray())
        {
            if (biomeGenBase != null)
                set.add(biomeGenBase);
        }

        for (BiomeDictionary.Type type : BiomeDictionary.Type.values())
            Collections.addAll(set, BiomeDictionary.getBiomesForType(type));

        return set;
    }

    public static boolean isBiomeAllTypes(BiomeGenBase biomeGenBase, List<BiomeDictionary.Type> types)
    {
        for (BiomeDictionary.Type type : types)
        {
            if (!BiomeDictionary.isBiomeOfType(biomeGenBase, type))
                return false;
        }

        return true;
    }

    public String getBiomeID()
    {
        return biomeID;
    }

    public void setBiomeID(String biomeID)
    {
        this.biomeID = biomeID;
    }

    public List<BiomeDictionary.Type> getBiomeTypes()
    {
        if (biomeID.startsWith("Type:"))
        {
            String[] typeIDs = biomeID.substring(5).split(",");

            List<BiomeDictionary.Type> types = new ArrayList<>(typeIDs.length);

            for (String typeID : typeIDs)
            {
                BiomeDictionary.Type type = RCGsonHelper.enumForNameIgnoreCase(typeID, BiomeDictionary.Type.values());

                if (type == null)
                    return null;

                types.add(type);
            }

            return types;
        }

        return null;
    }

    public boolean matches(BiomeGenBase biome)
    {
        String generationBiomeID = getBiomeID();
        if (generationBiomeID.equals(biome.biomeName))
            return true;

        List<BiomeDictionary.Type> types = getBiomeTypes();
        return types != null && isBiomeAllTypes(biome, types);
    }

    public boolean isTypeList()
    {
        return getBiomeTypes() != null;
    }
}
