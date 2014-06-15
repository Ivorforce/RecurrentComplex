package ivorius.structuregen.worldgen;

import net.minecraft.util.WeightedRandom;

/**
 * Created by lukas on 24.05.14.
 */
public class WeightedStructureInfo extends WeightedRandom.Item
{
    StructureInfo structureInfo;

    public WeightedStructureInfo(int weight, StructureInfo structureInfo)
    {
        super(weight);

        this.structureInfo = structureInfo;
    }
}
