/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import ivorius.reccomplex.world.gen.feature.decoration.RCBiomeDecorator;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.VanillaDecorationGeneration;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Random;

public class MapGenStructureHook extends MapGenStructure
{
    public MapGenStructure base;
    public RCBiomeDecorator.DecorationType decorationType;

    public MapGenStructureHook(MapGenStructure base, RCBiomeDecorator.DecorationType decorationType)
    {
        this.base = base;
        this.decorationType = decorationType;
    }

    public MapGenStructureHook(MapGenStructure base)
    {
        this.base = base;
    }

    public static Long2ObjectMap<StructureStart> getStructureMap(MapGenStructure gen)
    {
        return ReflectionHelper.getPrivateValue(MapGenStructure.class, gen, "structureMap", "field_75053_d");
    }

    public static void initializeStructureData(MapGenStructure gen, World world)
    {
        Method method = ReflectionHelper.findMethod(MapGenStructure.class, "initializeStructureData", "func_143027_a", World.class);

        try
        {
            method.invoke(gen, world);
        }
        catch (IllegalAccessException | InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }

    public static void setStructureStart(MapGenStructure gen, int x, int z, StructureStart start)
    {
        Method method = ReflectionHelper.findMethod(MapGenStructure.class, "setStructureStart", "func_143026_a", Integer.TYPE, Integer.TYPE, StructureStart.class);

        try
        {
            method.invoke(gen, x, z, start);
        }
        catch (IllegalAccessException | InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }

    public static StructureStart getStructureStart(MapGenStructure gen, ChunkPos chunkPos)
    {
        return getStructureMap(gen).get(ChunkPos.asLong(chunkPos.x, chunkPos.z));
    }

    @Override
    public String getStructureName()
    {
        String name = base != null ? base.getStructureName() : null;
        return name != null ? name : "";
    }

    @Override
    public boolean generateStructure(World worldIn, Random randomIn, ChunkPos chunkCoord)
    {
        return base.generateStructure(worldIn, randomIn, chunkCoord);
    }

    @Override
    public boolean isInsideStructure(BlockPos pos)
    {
        return base.isInsideStructure(pos);
    }

    @Override
    public boolean isPositionInStructure(World worldIn, BlockPos pos)
    {
        return base.isPositionInStructure(worldIn, pos);
    }

    @Override
    @Nullable
    public BlockPos getNearestStructurePos(World worldIn, BlockPos pos, boolean findUnexplored)
    {
        return base.getNearestStructurePos(worldIn, pos, findUnexplored);
    }

    @Override
    public boolean canSpawnStructureAtCoords(int chunkX, int chunkZ)
    {
        Method method = ReflectionHelper.findMethod(MapGenStructure.class, "canSpawnStructureAtCoords", "func_75047_a", Integer.TYPE, Integer.TYPE);
        try
        {
            return (boolean) method.invoke(base, chunkX, chunkZ);
        }
        catch (IllegalAccessException | InvocationTargetException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ)
    {
        return null; // Don't need to override this but is abstract
    }

    @Override
    public void generate(World worldIn, int x, int z, ChunkPrimer primer)
    {
        this.world = worldIn;
        WorldServer server = (WorldServer) worldIn;

        this.rand.setSeed(worldIn.getSeed());
        long j = this.rand.nextLong();
        long k = this.rand.nextLong();

        initializeStructureData(base, world);
        Long2ObjectMap<StructureStart> map = getStructureMap(base);
        LongSet before = new LongOpenHashSet(map.keySet());

        base.generate(worldIn, x, z, primer);

        Sets.newHashSet(Collections2.filter(map.keySet(), Predicates.not(Predicates.in(before)))).forEach(key -> {
            StructureStart start = map.get(key);

            if (start.isSizeableStructure())
            {
                this.rand.setSeed((j * start.getChunkPosX()) ^ (k * start.getChunkPosZ()) ^ worldIn.getSeed());
                Pair<Structure<?>, VanillaDecorationGeneration> selected = RCBiomeDecorator.selectDecoration(server, rand, new BlockPos(start.getChunkPosX() * 16, 0, start.getChunkPosZ() * 16), getDecorationType(start));
                if (selected != null) // > 1 we can't handle yet...
                {
//                    int minY = start.getComponents().get(0).getBoundingBox().minY;

                    // We don't want this anymore.
                    // Don't just remove it from the list lest it get added again
                    // Instead remove all components so it doesn't generate anything
                    start.getComponents().clear();
                    setStructureStart(base, start.getChunkPosX(), start.getChunkPosZ(), start);

                    // Gen ours
                    // Do this AFTER clearing because of chained chunk gen
                    // HACKY This is important because technically we're in the planning phase and not allowed to gen
                    RCBiomeDecorator.generate(selected, server, new ChunkPos(start.getChunkPosX(), start.getChunkPosZ()), rand);
                }
            }
        });
    }

    public RCBiomeDecorator.DecorationType getDecorationType(StructureStart start)
    {
        return decorationType;
    }
}
