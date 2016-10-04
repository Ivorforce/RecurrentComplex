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
        public float defaultSpawnChance = 1;
        @SerializedName("structureMinCap")
        public Integer structureMinCap = null;

        @SerializedName("spawnDistanceMultiplier")
        public float spawnDistanceMultiplier = 0;
        @SerializedName("spawnDistanceMultiplierCap")
        public float spawnDistanceMultiplierCap = 1;

        @SerializedName("selectableInGUI")
        public boolean selectableInGUI = true;
        @SerializedName("title")
        public String title = "";
        @SerializedName("tooltip")
        public final List<String> tooltip = new ArrayList<>();

        @Override
        public float structureSpawnChance(Biome biome, WorldProvider worldProvider, int registeredStructures, Float distanceToSpawn)
        {
            return spawnChance(biome, worldProvider)
                    * amountMultiplier(registeredStructures)
                    * distanceMultiplier(distanceToSpawn)
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

        public float distanceMultiplier(Float distance)
        {
            return distance == null ? 1 :
                    spawnDistanceMultiplier > 1 ? Math.min(1 + distance * spawnDistanceMultiplier, Math.max(spawnDistanceMultiplierCap, 1))
                    : Math.max(1 + distance * spawnDistanceMultiplier, Math.min(spawnDistanceMultiplierCap, 1));
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
