/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import ivorius.ivtoolkit.world.chunk.gen.StructureBoundingBoxes;
import ivorius.reccomplex.utils.RCStructureBoundingBoxes;
import ivorius.reccomplex.utils.accessor.SafeReflector;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created by lukas on 04.07.17.
 */
public class HeightMapFreezer
{
    protected static Method relightBlock;
    protected static Method propagateSkylightOcclusion;

    protected StructureBoundingBox boundingBox;

    protected World world;
    protected ChunkPos chunkMin;
    protected int[] chunkSize;

    protected TIntObjectMap<Entry> chunks = new TIntObjectHashMap<>();

    public HeightMapFreezer(StructureBoundingBox boundingBox, World world)
    {
        // Use minY twice so we have a flat bounding box
        this.boundingBox = new StructureBoundingBox(boundingBox.minX, boundingBox.minY, boundingBox.minZ,
                boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);

        this.world = world;
        this.chunkMin = new ChunkPos(StructureBoundingBoxes.min(boundingBox));
        ChunkPos chunkMax = new ChunkPos(StructureBoundingBoxes.max(boundingBox));
        this.chunkSize = new int[]{chunkMax.x - chunkMin.x + 1, chunkMax.z - chunkMin.z + 1,};
    }

    public static HeightMapFreezer freeze(StructureBoundingBox boundingBox, World world)
    {
        HeightMapFreezer freezer = new HeightMapFreezer(boundingBox, world);
        freezer.initialize();
        return freezer;
    }

    protected void initialize()
    {
        for (ChunkPos pos : RCStructureBoundingBoxes.rasterize(boundingBox, false))
            chunks.put(index(pos), new Entry(world.getChunkFromChunkCoords(pos.x, pos.z)));

        //noinspection ConstantConditions
        RCStructureBoundingBoxes.streamMutablePositions(boundingBox).forEach(pos ->
                entry(pos).chunk.getHeightMap()[chunkSurfaceIndex(pos)] = world.getHeight() + 1);
    }

    private Entry entry(BlockPos pos)
    {
        if (pos.getX() >= boundingBox.minX && pos.getZ() >= boundingBox.minZ
                && pos.getX() <= boundingBox.maxX && pos.getZ() <= boundingBox.maxZ)
            return chunks.get(index(new ChunkPos(pos)));

        return null;
    }

    private Integer index(ChunkPos pos)
    {
        return (pos.x - chunkMin.x) * chunkSize[1] + (pos.z - chunkMin.z);
    }

    private int chunkSurfaceIndex(BlockPos pos)
    {
        int i = pos.getX() & 15;
        int k = pos.getZ() & 15;
        return (k << 4) | i;
    }

    public void setBlockState(BlockPos pos, IBlockState state, int flags)
    {
        world.setBlockState(pos, state, flags);
        markBlock(pos, state);
    }

    public void markBlock(BlockPos pos, IBlockState state)
    {
        Entry entry = entry(pos);

        if (entry != null)
        {
            int light = state.getLightOpacity(this.world, pos);
            int surfaceIndex = chunkSurfaceIndex(pos);

            if (light > 0)
            {
                if (pos.getY() >= entry.heightMap[surfaceIndex])
                    entry.heightMap[surfaceIndex] = pos.getY();
            }
        }
    }

    public void melt()
    {
        if (relightBlock == null)
            relightBlock = ReflectionHelper.findMethod(Chunk.class, "relightBlock", "func_76615_h",
                    Integer.TYPE, Integer.TYPE, Integer.TYPE);
        if (propagateSkylightOcclusion == null)
            propagateSkylightOcclusion = ReflectionHelper.findMethod(Chunk.class, "propagateSkylightOcclusion", "func_76595_e",
                    Integer.TYPE, Integer.TYPE);

        // Restore
        RCStructureBoundingBoxes.streamMutablePositions(boundingBox).forEach(pos ->
        {
            Entry entry = entry(pos);
            int surfaceIndex = chunkSurfaceIndex(pos);
            //noinspection ConstantConditions
            entry.chunk.getHeightMap()[surfaceIndex] = entry.original[surfaceIndex];
        });

        // Relight
        RCStructureBoundingBoxes.streamMutablePositions(boundingBox).forEach(pos ->
        {
            Entry entry = entry(pos);
            int surfaceIndex = chunkSurfaceIndex(pos);
            //noinspection ConstantConditions
            if (entry.heightMap[surfaceIndex] >= entry.original[surfaceIndex])
            {
                SafeReflector.invoke(entry.chunk, relightBlock, null,
                        pos.getX() & 15, entry.heightMap[surfaceIndex] + 1, pos.getZ() & 15);
                SafeReflector.invoke(entry.chunk, propagateSkylightOcclusion, null,
                        pos.getX() & 15, pos.getZ() & 15);
                if (world.provider.hasSkyLight())
                    world.checkLight(new BlockPos(pos.getX(), entry.heightMap[surfaceIndex], pos.getZ()));
            }
        });
    }

    private class Entry
    {
        public Chunk chunk;
        public int[] heightMap;
        public int[] original;

        public Entry(Chunk chunk)
        {
            this.chunk = chunk;
            this.original = chunk.getHeightMap().clone();
            this.heightMap = new int[original.length];
            Arrays.fill(heightMap, -1);
        }
    }
}
