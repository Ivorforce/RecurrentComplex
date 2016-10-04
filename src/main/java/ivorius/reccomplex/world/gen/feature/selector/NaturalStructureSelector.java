/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.selector;

import com.google.gson.annotations.SerializedName;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.files.SimpleLeveledRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.matchers.BiomeMatcher;
import ivorius.reccomplex.world.gen.feature.structure.generic.matchers.DimensionMatcher;
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

    public static SimpleLeveledRegistry<Category> CATEGORY_REGISTRY = new SimpleLeveledRegistry<>("natural generation category");

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

        @SerializedName("defaultSpawnChance")
        public float defaultSpawnChance;
        @SerializedName("structureMinCap")
        public Integer structureMinCap;

        @SerializedName("selectableInGUI")
        public boolean selectableInGUI;
        @SerializedName("title")
        public String title;
        @SerializedName("tooltip")
        public final List<String> tooltip = new ArrayList<>();

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
            return spawnChance(biome, worldProvider)
                    * amountMultiplier(registeredStructures)
                    * RCConfig.structureSpawnChanceModifier;
        }

        public float amountMultiplier(int registeredStructures)
        {
            return Math.min((float) registeredStructures / (float) getActiveStructureMinCap(), 1.0f);
        }

        public float spawnChance(Biome biome, WorldProvider worldProvider)
        {
            float am = defaultSpawnChance;

            for (GenerationInfo info : generationInfos)
            {
                if (info.biomeMatcher.test(biome) && info.dimensionMatcher.test(worldProvider))
                    am = info.spawnChance;
            }
            return am;
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
