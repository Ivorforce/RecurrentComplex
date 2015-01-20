/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen;

import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import java.util.Random;

/**
 * Created by lukas on 19.01.15.
 */
public class StructureSpawnContext
{
    public final World world;
    public final Random random;

    public final AxisAlignedTransform2D transform;
    public final StructureBoundingBox boundingBox;

    public final int generationLayer;

    public final boolean generateAsSource;

    public StructureSpawnContext(World world, Random random, StructureBoundingBox boundingBox, int generationLayer, boolean generateAsSource, AxisAlignedTransform2D transform)
    {
        this.world = world;
        this.random = random;
        this.transform = transform;
        this.boundingBox = boundingBox;
        this.generationLayer = generationLayer;
        this.generateAsSource = generateAsSource;
    }

    public StructureSpawnContext(World world, Random random, BlockCoord coord, AxisAlignedTransform2D transform, int generationLayer, boolean generateAsSource, StructureInfo structureInfo)
    {
        this.world = world;
        this.random = random;
        boundingBox = WorldGenStructures.structureBoundingBox(coord, WorldGenStructures.structureSize(structureInfo, transform));
        this.transform = transform;
        this.generationLayer = generationLayer;
        this.generateAsSource = generateAsSource;
    }

    public int[] boundingBoxSize()
    {
        return new int[]{boundingBox.getXSize(), boundingBox.getYSize(), boundingBox.getZSize()};
    }

    public BlockCoord lowerCoord()
    {
        return new BlockCoord(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
    }

    public boolean setBlock(BlockCoord coord, Block block, int meta)
    {
        if (boundingBox.isVecInside(coord.x, coord.y, coord.z))
        {
            world.setBlock(coord.x, coord.y, coord.z, block, meta, 2);
            return true;
        }

        return false;
    }

    public boolean setBlock(int x, int y, int z, Block block, int meta)
    {
        if (boundingBox.isVecInside(x, y, z))
        {
            world.setBlock(x, y, z, block, meta, 2);
            return true;
        }

        return false;
    }
}
