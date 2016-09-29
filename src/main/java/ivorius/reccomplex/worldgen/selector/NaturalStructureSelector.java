/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.worldgen.selector;

import com.google.gson.annotations.SerializedName;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.files.SimpleFileRegistry;
import ivorius.reccomplex.structures.generic.matchers.BiomeMatcher;
import ivorius.reccomplex.structures.generic.matchers.DimensionMatcher;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukas on 23.09.16.
 */
public class NaturalStructureSelector
{
    public static final int STRUCTURE_MIN_CAP_DEFAULT = 20;

    public static SimpleFileRegistry<Category> CATEGORY_REGISTRY = new SimpleFileRegistry<>("natural generation category");

    public interface Category extends MixingStructureSelector.Category
    {
        boolean selectableInGUI();

        String title();

        List<String> tooltip();
    }

    public static class SimpleCategory implements Category
    {
        @SerializedName("generationInfos")
        public final List<GenerationInfo> generationInfos = new ArrayList<>();
        @SerializedName("tooltip")
        public final List<String> tooltip = new ArrayList<>();
        @SerializedName("defaultSpawnChance")
        public float defaultSpawnChance;
        @SerializedName("selectableInGUI")
        public boolean selectableInGUI;
        @SerializedName("structureMinCap")
        public Integer structureMinCap;
        @SerializedName("title")
        public String title;

        public SimpleCategory(float defaultSpawnChance, List<GenerationInfo> generationInfos, boolean selectableInGUI, Integer structureMinCap)
        {
            this.defaultSpawnChance = defaultSpawnChance;
            this.generationInfos.addAll(generationInfos);
            this.selectableInGUI = selectableInGUI;
            this.structureMinCap = structureMinCap;
        }

        public SimpleCategory(float defaultSpawnChance, List<GenerationInfo> generationInfos, boolean selectableInGUI)
        {
            this(defaultSpawnChance, generationInfos, selectableInGUI, null);
        }

        @Override
        public float structureSpawnChance(Biome biome, WorldProvider worldProvider, int registeredStructures)
        {
            float amountMultiplier = Math.min((float) registeredStructures / (float) getActiveStructureMinCap(), 1.0f);

            for (GenerationInfo info : generationInfos)
            {
                if (info.biomeMatcher.test(biome) && info.dimensionMatcher.test(worldProvider))
                    return info.spawnChance * amountMultiplier;
            }

            return defaultSpawnChance * amountMultiplier * RCConfig.structureSpawnChanceModifier;
        }

        public Integer getActiveStructureMinCap()
        {
            return structureMinCap != null ? structureMinCap : STRUCTURE_MIN_CAP_DEFAULT;
        }

        @Override
        public boolean selectableInGUI()
        {
            return selectableInGUI;
        }

        @Override
        public String title()
        {
            return title;
        }

        @Override
        public List<String> tooltip()
        {
            return tooltip;
        }
    }

    public static class GenerationInfo
    {
        @SerializedName("spawnChance")
        public float spawnChance;
        @SerializedName("biomeMatcher")
        public BiomeMatcher biomeMatcher;
        @SerializedName("dimensionMatcher")
        public DimensionMatcher dimensionMatcher;

        public GenerationInfo(float spawnChance, BiomeMatcher biomeMatcher, DimensionMatcher dimensionMatcher)
        {
            this.spawnChance = spawnChance;
            this.biomeMatcher = biomeMatcher;
            this.dimensionMatcher = dimensionMatcher;
        }
    }
}
