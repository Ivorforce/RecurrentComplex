/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.selector;

import com.google.gson.annotations.SerializedName;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.files.SimpleLeveledRegistry;
import ivorius.reccomplex.utils.expression.BiomeMatcher;
import ivorius.reccomplex.utils.expression.DimensionMatcher;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 23.09.16.
 */
public class NaturalStructureSelector
{
    public static final double STRUCTURE_MIN_CAP_DEFAULT = 20;

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
        public double defaultSpawnChance;
        @SerializedName("defaultSpawnChances")
        public double[] defaultSpawnChances;
        @SerializedName("structureMinCap")
        public Double structureMinCap = null;

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
        public int structuresInBiome(Biome biome, WorldProvider worldProvider, double totalWeight, Float distanceToSpawn, Random random)
        {
            return (int) Arrays.stream(spawnChance(biome, worldProvider)).filter(chance -> random.nextDouble() <
                    chance * amountMultiplier(totalWeight)
                            * distanceMultiplier(distanceToSpawn)
                            * RCConfig.structureSpawnChanceModifier)
                    .count();
        }

        public double amountMultiplier(double totalWeight)
        {
            return Math.min(totalWeight / getActiveStructureMinCap(), 1.0f);
        }

        public double[] spawnChance(Biome biome, WorldProvider worldProvider)
        {
            double[] am = defaultSpawnChances != null ? defaultSpawnChances : new double[]{defaultSpawnChance};

            for (GenerationInfo info : generationInfos)
            {
                if (info.biomeMatcher.test(biome) && info.dimensionMatcher.test(worldProvider))
                    am = info.spawnChances != null ? info.spawnChances : new double[]{info.spawnChance};
            }
            return am;
        }

        public double distanceMultiplier(Float distance)
        {
            return distance == null ? 1 :
                    spawnDistanceMultiplier > 1 ? Math.min(1 + distance * spawnDistanceMultiplier, Math.max(spawnDistanceMultiplierCap, 1))
                            : Math.max(1 + distance * spawnDistanceMultiplier, Math.min(spawnDistanceMultiplierCap, 1));
        }

        public Double getActiveStructureMinCap()
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
        public double spawnChance;
        public double[] spawnChances;

        @SerializedName("biomeMatcher")
        public BiomeMatcher biomeMatcher;
        @SerializedName("dimensionMatcher")
        public DimensionMatcher dimensionMatcher;

        public GenerationInfo(double[] spawnChance, BiomeMatcher biomeMatcher, DimensionMatcher dimensionMatcher)
        {
            this.spawnChances = spawnChance;
            this.biomeMatcher = biomeMatcher;
            this.dimensionMatcher = dimensionMatcher;
        }
    }
}
