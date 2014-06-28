/*
 * Copyright (c) 2014, Lukas Tenbrink.
 * http://lukas.axxim.net
 *
 * You are free to:
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes, unless you have a permit by the creator.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package ivorius.ivtoolkit.blocks;

import ivorius.ivtoolkit.raytracing.IvRaytraceableAxisAlignedBox;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3Pool;
import net.minecraft.world.World;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IvMultiBlockHelper implements Iterable<int[]>
{
    private Iterator<int[]> iterator;
    private List<int[]> childLocations;
    private IvTileEntityMultiBlock parentTileEntity = null;

    private World world;
    private Block block;
    private int metadata;

    private int direction;
    private double[] center;
    private double[] size;

    public IvMultiBlockHelper()
    {

    }

    public boolean beginPlacing(List<int[]> positions, World world, int x, int y, int z, int blockSide, ItemStack itemStack, EntityPlayer player, Block block, int metadata, int direction)
    {
        List<int[]> validLocations = IvMultiBlockHelper.getBestPlacement(positions, world, x, y, z, blockSide, itemStack, player, block);

        if (validLocations == null)
        {
            return false;
        }

        this.world = world;
        this.block = block;
        this.metadata = metadata;
        this.parentTileEntity = null;
        this.direction = direction;

        this.center = IvMultiBlockHelper.getTileEntityCenter(validLocations);
        this.size = IvMultiBlockHelper.getTileEntitySize(validLocations);
        this.childLocations = validLocations;

        this.iterator = validLocations.iterator();

        return true;
    }

    @Override
    public Iterator<int[]> iterator()
    {
        return iterator;
    }

    public IvTileEntityMultiBlock placeBlock(int[] blockCoords)
    {
        return placeBlock(blockCoords, this.parentTileEntity == null);
    }

    private IvTileEntityMultiBlock placeBlock(int[] blockCoords, boolean parent)
    {
        world.setBlock(blockCoords[0], blockCoords[1], blockCoords[2], block, metadata, 3);
        TileEntity tileEntity = world.getTileEntity(blockCoords[0], blockCoords[1], blockCoords[2]);

        if (tileEntity instanceof IvTileEntityMultiBlock)
        {
            IvTileEntityMultiBlock tileEntityMB = (IvTileEntityMultiBlock) tileEntity;

            if (parent)
            {
                parentTileEntity = tileEntityMB;
                childLocations.remove(new int[]{parentTileEntity.xCoord, parentTileEntity.yCoord, parentTileEntity.zCoord});
                parentTileEntity.becomeParent(childLocations);
            }
            else
            {
                tileEntityMB.becomeChild(parentTileEntity);
            }

            tileEntityMB.direction = direction;
            tileEntityMB.centerCoords = new double[]{center[0] - blockCoords[0], center[1] - blockCoords[1], center[2] - blockCoords[2]};
            tileEntityMB.centerCoordsSize = size;

            return tileEntityMB;
        }

        return null;
    }

    public static double[] getTileEntityCenter(List<int[]> positions)
    {
        double[] result = getCenter(positions);

        return new double[]{result[0] + 0.5f, result[1] + 0.5f, result[2] + 0.5f};
    }

    public static double[] getTileEntitySize(List<int[]> positions)
    {
        return getSize(positions);
    }

    public static double[] getCenter(List<int[]> positions)
    {
        if (positions.size() > 0)
        {
            int[] min = getExtremeCoords(positions, true);
            int[] max = getExtremeCoords(positions, false);

            double[] result = new double[min.length];
            for (int i = 0; i < min.length; i++)
            {
                result[i] = (min[i] + max[i]) * 0.5f;
            }

            return result;
        }

        return null;
    }

    public static double[] getSize(List<int[]> positions)
    {
        if (positions.size() > 0)
        {
            int[] min = getExtremeCoords(positions, true);
            int[] max = getExtremeCoords(positions, false);

            double[] result = new double[min.length];
            for (int i = 0; i < min.length; i++)
            {
                result[i] = (float) (max[i] - min[i] + 1) * 0.5f;
            }

            return result;
        }

        return null;
    }

    public static int[] getExtremeCoords(List<int[]> positions, boolean min)
    {
        if (positions.size() > 0)
        {
            int[] selectedPos = positions.get(0).clone();

            for (int n = 1; n < positions.size(); n++)
            {
                int[] position = positions.get(n);

                for (int i = 0; i < selectedPos.length; i++)
                {
                    selectedPos[i] = min ? Math.min(position[i], selectedPos[i]) : Math.max(position[i], selectedPos[i]);
                }
            }

            return selectedPos;
        }

        return null;
    }

    public static int[] getLengths(List<int[]> positions)
    {
        int[] min = getExtremeCoords(positions, true);
        int[] max = getExtremeCoords(positions, false);

        return new int[]{max[0] - min[0], max[1] - min[1], max[2] - min[2]};
    }

    public static boolean canPlace(World world, Block block, List<int[]> positions, Entity entity, ItemStack stack)
    {
        for (int[] position : positions)
        {
            if (!world.canPlaceEntityOnSide(block, position[0], position[1], position[2], false, 0, entity, stack))
            {
                return false;
            }
        }

        return true;
    }

    public static List<List<int[]>> getValidPlacements(List<int[]> positions, World world, int x, int y, int z, int blockSide, ItemStack itemStack, EntityPlayer player, Block block)
    {
        Block var11 = world.getBlock(x, y, z);

        if (var11 == Blocks.snow_layer && (world.getBlockMetadata(x, y, z) & 7) < 1)
        {
            blockSide = 1;
        }
        else if (var11 != Blocks.vine && var11 != Blocks.tallgrass && var11 != Blocks.deadbush && !var11.isReplaceable(world, x, y, z))
        {
            if (blockSide == 0)
            {
                --y;
            }
            else if (blockSide == 1)
            {
                ++y;
            }
            else if (blockSide == 2)
            {
                --z;
            }
            else if (blockSide == 3)
            {
                ++z;
            }
            else if (blockSide == 4)
            {
                --x;
            }
            else if (blockSide == 5)
            {
                ++x;
            }
        }

        if (!player.canPlayerEdit(x, y, z, blockSide, itemStack))
        {
            return new ArrayList<>();
        }
        else if (y == world.getHeight() && block.getMaterial().isSolid())
        {
            return new ArrayList<>();
        }
        else
        {
            int[] lengths = getLengths(positions);
            int[] min = getExtremeCoords(positions, true);

            // Run from min+length (maximimum) being the placed x, y, z to minimum being the x, y, z
            ArrayList<List<int[]>> validPlacements = new ArrayList<>();
            for (int xShift = min[0] - lengths[0]; xShift <= min[0]; xShift++)
            {
                for (int yShift = min[0] - lengths[1]; yShift <= min[1]; yShift++)
                {
                    for (int zShift = min[0] - lengths[2]; zShift <= min[2]; zShift++)
                    {
                        ArrayList<int[]> validPositions = new ArrayList<>();

                        for (int[] position : positions)
                        {
                            validPositions.add(new int[]{position[0] + x + xShift, position[1] + y + yShift, position[2] + z + zShift});
                        }

                        if (canPlace(world, block, validPositions, null, itemStack))
                        {
                            validPlacements.add(validPositions);
                        }
                    }
                }
            }

            return validPlacements;
        }
    }

    public static List<int[]> getBestPlacement(List<int[]> positions, World world, int x, int y, int z, int blockSide, ItemStack itemStack, EntityPlayer player, Block block)
    {
        int[] lengths = getLengths(positions);

        List<List<int[]>> validPlacements = getValidPlacements(positions, world, x, y, z, blockSide, itemStack, player, block);

        if (validPlacements.size() > 0)
        {
            float[] center = new float[]{x - lengths[0] * 0.5f, y - lengths[1] * 0.5f, z - lengths[2] * 0.5f};
            List<int[]> preferredPositions = validPlacements.get(0);
            for (int i = 1; i < validPlacements.size(); i++)
            {
                int[] referenceBlock = validPlacements.get(i).get(0);
                int[] referenceBlockOriginal = preferredPositions.get(0);

                if (distanceSquared(referenceBlock, center) < distanceSquared(referenceBlockOriginal, center))
                {
                    preferredPositions = validPlacements.get(i);
                }
            }

            return preferredPositions;
        }

        return null;
    }

    private static float distanceSquared(int[] referenceBlock, float[] center)
    {
        float distX = referenceBlock[0] - center[0];
        float distY = referenceBlock[1] - center[1];
        float distZ = referenceBlock[2] - center[2];

        return distX * distX + distY * distY + distZ * distZ;
    }

    public static List<int[]> getRotatedPositions(List<int[]> positions, int rotation)
    {
        ArrayList<int[]> returnList = new ArrayList<>(positions.size());

        for (int[] position : positions)
        {
            if (rotation == 0)
            {
                returnList.add(new int[]{position[0], position[1], position[2]});
            }
            if (rotation == 1)
            {
                returnList.add(new int[]{position[2], position[1], position[0]});
            }
            if (rotation == 2)
            {
                returnList.add(new int[]{position[0], position[1], position[2]});
            }
            if (rotation == 3)
            {
                returnList.add(new int[]{position[2], position[1], position[0]});
            }
        }

        return returnList;
    }

    public static IvRaytraceableAxisAlignedBox getRotatedBox(Object userInfo, double x, double y, double z, double width, double height, double depth, int direction, double[] centerCoords)
    {
        IvRaytraceableAxisAlignedBox box = null;

        if (direction == 0)
        {
            box = new IvRaytraceableAxisAlignedBox(userInfo, centerCoords[0] - x - width, centerCoords[1] + y, centerCoords[2] + z, width, height, depth);
        }
        if (direction == 1)
        {
            box = new IvRaytraceableAxisAlignedBox(userInfo, centerCoords[0] - z - depth, centerCoords[1] + y, centerCoords[2] - x - width, depth, height, width);
        }
        if (direction == 2)
        {
            box = new IvRaytraceableAxisAlignedBox(userInfo, centerCoords[0] + x, centerCoords[1] + y, centerCoords[2] - z - depth, width, height, depth);
        }
        if (direction == 3)
        {
            box = new IvRaytraceableAxisAlignedBox(userInfo, centerCoords[0] + z, centerCoords[1] + y, centerCoords[2] + x, depth, height, width);
        }

        return box;
    }

    public static AxisAlignedBB getRotatedBB(double x, double y, double z, double width, double height, double depth, int direction, double[] centerCoords)
    {
        AxisAlignedBB box = null;

        if (direction == 0)
        {
            box = getBBWithLengths(centerCoords[0] + x, centerCoords[1] + y, centerCoords[2] + z, width, height, depth);
        }
        if (direction == 1)
        {
            box = getBBWithLengths(centerCoords[0] - z - depth, centerCoords[1] + y, centerCoords[2] + x, depth, height, width);
        }
        if (direction == 2)
        {
            box = getBBWithLengths(centerCoords[0] - x - width, centerCoords[1] + y, centerCoords[2] - z - depth, width, height, depth);
        }
        if (direction == 3)
        {
            box = getBBWithLengths(centerCoords[0] + z, centerCoords[1] + y, centerCoords[2] - x - width, depth, height, width);
        }

        return box;
    }

    public static Vector3f getRotatedVector(Vector3f vector3f, int rotation)
    {
        if (rotation == 0)
        {
            return new Vector3f(vector3f.x, vector3f.y, vector3f.z);
        }
        else if (rotation == 1)
        {
            return new Vector3f(-vector3f.z, vector3f.y, vector3f.x);
        }
        else if (rotation == 2)
        {
            return new Vector3f(-vector3f.x, vector3f.y, -vector3f.z);
        }
        else if (rotation == 3)
        {
            return new Vector3f(vector3f.z, vector3f.y, -vector3f.x);
        }

        return null;
    }

    public static Vec3 getRotatedVector(Vec3 vec3, int rotation)
    {
        if (rotation == 0)
        {
            return Vec3.createVectorHelper(vec3.xCoord, vec3.yCoord, vec3.zCoord);
        }
        else if (rotation == 1)
        {
            return Vec3.createVectorHelper(-vec3.zCoord, vec3.yCoord, vec3.xCoord);
        }
        else if (rotation == 2)
        {
            return Vec3.createVectorHelper(-vec3.xCoord, vec3.yCoord, -vec3.zCoord);
        }
        else if (rotation == 3)
        {
            return Vec3.createVectorHelper(vec3.zCoord, vec3.yCoord, -vec3.xCoord);
        }

        return null;
    }

    public static AxisAlignedBB getBBWithLengths(double x, double y, double z, double width, double height, double depth)
    {
        return AxisAlignedBB.getBoundingBox(x, y, z, x + width, y + height, z + depth);
    }

    public static int getRotation(Entity entity)
    {
        return MathHelper.floor_double((entity.rotationYaw * 4F) / 360F + 0.5D) & 3;
    }

    public static List<int[]> getRotatedPositions(int rotation, int width, int height, int length)
    {
        boolean affectsX = (rotation == 0) || (rotation == 2);
        return getPositions(affectsX ? width : length, height, affectsX ? length : width);
    }

    public static List<int[]> getPositions(int width, int height, int length)
    {
        ArrayList<int[]> positions = new ArrayList<>();

        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                for (int z = 0; z < length; z++)
                {
                    positions.add(new int[]{x, y, z});
                }
            }
        }

        return positions;
    }
}
