package ivorius.structuregen.worldgen.genericStructures;

import com.google.gson.annotations.SerializedName;
import ivorius.structuregen.ivtoolkit.IvGsonHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public class GenerationYSelector
{
    public static enum SelectionMode
    {
        @SerializedName("bedrock")
        BEDROCK,
        @SerializedName("surface")
        SURFACE,
        @SerializedName("sealevel")
        SEALEVEL,
        @SerializedName("underwater")
        UNDERWATER,
        @SerializedName("top")
        TOP;

        public String serializedName()
        {
            return IvGsonHelper.serializedName(this);
        }

        public static SelectionMode selectionMode(String serializedName)
        {
            return IvGsonHelper.enumForName(serializedName, values());
        }
    }

    public SelectionMode selectionMode;

    public int minY;
    public int maxY;

    public GenerationYSelector(SelectionMode selectionMode, int minY, int maxY)
    {
        this.selectionMode = selectionMode;
        this.minY = minY;
        this.maxY = maxY;
    }

    public int generationY(World world, Random random, int x, int z)
    {
        int y = minY + random.nextInt(maxY - minY + 1);

        switch (selectionMode)
        {
            case BEDROCK:
                return y;
            case SURFACE:
                int curY = world.getTopSolidOrLiquidBlock(x, z);

                while (curY > 0)
                {
                    Block block = world.getBlock(x, curY, z);
                    if (!(block.isFoliage(world, x, curY, z) || block.getMaterial() == Material.leaves || block.getMaterial() == Material.plants || block.getMaterial() == Material.wood))
                    {
                        break;
                    }

                    curY--;
                }
                while (curY < world.getHeight())
                {
                    if (!(world.getBlock(x, curY, z) instanceof BlockLiquid))
                    {
                        break;
                    }

                    curY++;
                }
                return curY + y;
            case SEALEVEL:
                return 63 + y;
            case UNDERWATER:
                int curYWater = world.getTopSolidOrLiquidBlock(x, z) + y;

                while (curYWater > 0)
                {
                    Block block = world.getBlock(x, curYWater, z);
                    if (!(block instanceof BlockLiquid || block.getMaterial() == Material.ice))
                    {
                        curYWater ++;
                        break;
                    }

                    curYWater--;
                }
                return curYWater + y;
            case TOP:
                return world.getHeight() + y;
        }

        throw new RuntimeException("Unrecognized selection mode " + selectionMode);
    }
}
