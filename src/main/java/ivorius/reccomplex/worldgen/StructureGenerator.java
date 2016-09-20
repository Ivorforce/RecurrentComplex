/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.worldgen;

import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.events.RCEventBus;
import ivorius.reccomplex.events.StructureGenerationEvent;
import ivorius.reccomplex.events.StructureGenerationEventLite;
import ivorius.reccomplex.structures.*;
import ivorius.reccomplex.structures.generic.gentypes.StructureGenerationInfo;
import ivorius.reccomplex.structures.generic.placement.StructurePlaceContext;
import ivorius.reccomplex.utils.BlockSurfacePos;
import ivorius.reccomplex.utils.NBTStorable;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

/**
 * Created by lukas on 19.01.15.
 */
public class StructureGenerator<S extends NBTStorable>
{
    public static final int MIN_DIST_TO_LIMIT = 1;

    @Nullable
    private WorldServer world;
    @Nullable
    private StructureInfo<S> structureInfo;
    @Nullable
    private String structureID;

    @Nullable
    private BlockPos lowerCoord;
    @Nullable
    private BlockSurfacePos surfacePos;
    @Nullable
    private Placer placer;
    private boolean fromCenter;

    @Nullable
    private Environment environment;
    @Nullable
    private Random random;

    @Nullable
    private AxisAlignedTransform2D transform;
    @Nullable
    private StructureBoundingBox boundingBox;
    @Nullable
    private StructureBoundingBox generationBB;

    private String generationInfoID;
    private StructureGenerationInfo generationInfo;
    private int generationLayer = 0;

    private boolean generateAsSource = false;
    private StructureSpawnContext.GenerateMaturity generateMaturity = StructureSpawnContext.GenerateMaturity.FIRST;

    @Nullable
    private S instanceData;
    @Nullable
    private NBTBase instanceDataNBT;

    private boolean allowOverlaps = false;
    private boolean memorize = true;

    public StructureGenerator(StructureInfo<S> structureInfo)
    {
        structure(structureInfo);
    }

    public StructureGenerator()
    {

    }

    @Nonnull
    public static Placer worldHeightPlacer()
    {
        return (context, blockCollection) -> context.environment.world.getHeight(new BlockPos(context.boundingBox.getCenter())).getY();
    }

    public static String name(String structureName)
    {
        return structureName != null ? structureName : "Unknown";
    }

    public static int[] coordInts(StructureBoundingBox bb)
    {
        return new int[]{bb.minX, bb.minY, bb.minZ};
    }

    public static int[] sizeInts(StructureBoundingBox bb)
    {
        return new int[]{bb.getXSize(), bb.getYSize(), bb.getZSize()};
    }

    @Nonnull
    public Optional<StructureSpawnContext> generate()
    {
        Optional<StructureSpawnContext> spawnO = spawn();
        Optional<S> instanceDataO = instanceData();

        if (!spawnO.isPresent() || !instanceDataO.isPresent())
            return Optional.empty();

        StructureSpawnContext spawn = spawnO.get();
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        S instanceData = instanceDataO.get();

        StructureInfo<S> structureInfo = structure();
        String structureID = structureID();
        boolean firstTime = spawn.isFirstTime();

        WorldServer world = spawn.environment.world;

        int[] sizeInts = sizeInts(spawn.boundingBox);
        int[] coordInts = coordInts(spawn.boundingBox);

        if (maturity() != StructureSpawnContext.GenerateMaturity.SUGGEST || (
                spawn.boundingBox.minY >= MIN_DIST_TO_LIMIT && spawn.boundingBox.maxY <= world.getHeight() - 1 - MIN_DIST_TO_LIMIT
                        && (!RCConfig.avoidOverlappingGeneration || allowOverlaps || StructureGenerationData.get(world).getEntriesAt(spawn.boundingBox).size() == 0)
                        && !RCEventBus.INSTANCE.post(new StructureGenerationEvent.Suggest(structureInfo, spawn))
                        && !MinecraftForge.EVENT_BUS.post(new StructureGenerationEventLite.Suggest(world, structureID, coordInts, sizeInts, spawn.generationLayer))
        ))
        {
            if (firstTime)
            {
                RCEventBus.INSTANCE.post(new StructureGenerationEvent.Pre(structureInfo, spawn));
                MinecraftForge.EVENT_BUS.post(new StructureGenerationEventLite.Pre(world, structureID, coordInts, sizeInts, spawn.generationLayer));
            }

            boolean success = structureInfo.generate(spawn, instanceData);

            if (firstTime && success)
            {
                RecurrentComplex.logger.trace(String.format("Generated structure '%s' in %s", name(structureID), spawn.boundingBox));

                RCEventBus.INSTANCE.post(new StructureGenerationEvent.Post(structureInfo, spawn));
                MinecraftForge.EVENT_BUS.post(new StructureGenerationEventLite.Post(world, structureID, coordInts, sizeInts, spawn.generationLayer));

                if (structureID != null && memorize)
                    StructureGenerationData.get(world).addCompleteEntry(structureID, generationInfo != null ? generationInfo.id() : null, spawn.lowerCoord(), spawn.transform);
            }

            return success ? spawnO : Optional.empty();
        }
        else
            RecurrentComplex.logger.trace(String.format("Canceled structure '%s' generation in %s", structureID, spawn.boundingBox));

        return Optional.empty();
    }

    public StructureGenerator<S> asChild(StructureSpawnContext context)
    {
        return environment(context.environment).random(context.random).transform(context.transform)
                .generationBB(context.generationBB).generationLayer(context.generationLayer + 1)
                .asSource(context.generateAsSource).maturity(context.isFirstTime() ? StructureSpawnContext.GenerateMaturity.FIRST : StructureSpawnContext.GenerateMaturity.COMPLEMENT);
    }

    public StructureGenerator<S> world(@Nonnull WorldServer world)
    {
        this.world = world;
        return this;
    }

    @Nonnull
    public WorldServer world()
    {
        WorldServer world = this.world != null ? this.world : environment != null ? environment.world : null;
        if (world == null) throw new IllegalStateException("No world!");
        return world;
    }

    @Nonnull
    public Random random()
    {
        return this.random != null ? this.random : world().rand;
    }

    public StructureGenerator<S> structure(@Nonnull StructureInfo<S> structureInfo)
    {
        this.structureInfo = structureInfo;
        return this;
    }

    public StructureGenerator<S> structureID(@Nonnull String structureID)
    {
        this.structureID = structureID;
        return this;
    }

    @Nonnull
    public StructureInfo<S> structure()
    {
        StructureInfo<S> structureInfo = this.structureInfo != null ? this.structureInfo : structureID != null ? StructureRegistry.INSTANCE.getStructure(structureID) : null;
        if (structureInfo == null) throw new IllegalStateException();
        return structureInfo;
    }

    @Nullable
    public String structureID()
    {
        return this.structureID != null ? this.structureID : this.structureInfo != null ? StructureRegistry.INSTANCE.structureID(structureInfo) : null;
    }

    public StructureGenerator<S> lowerCoord(@Nonnull BlockPos lowerCoord)
    {
        this.lowerCoord = lowerCoord;
        return this;
    }

    @Nonnull
    public Optional<BlockPos> lowerCoord()
    {
        if (lowerCoord != null)
            return Optional.of(lowerCoord);
        else
            return boundingBox().map(bb -> new BlockPos(bb.minX, bb.minY, bb.minZ));
    }

    public StructureGenerator<S> randomPosition(@Nonnull BlockSurfacePos surfacePos, @Nullable Placer placer)
    {
        this.surfacePos = surfacePos;
        this.placer = placer != null ? placer : worldHeightPlacer();
        return this;
    }

    public StructureGenerator<S> fromCenter(boolean fromCenter)
    {
        this.fromCenter = fromCenter;
        return this;
    }

    public StructureGenerator<S> environment(@Nonnull Environment environment)
    {
        this.environment = environment;
        return this;
    }

    @Nonnull
    protected Environment environment()
    {
        return environment != null ? environment : Environment.inNature(world(), surfaceBoundingBox(),
                generationInfo != null ? generationInfo : generationInfoID != null ? structure().generationInfo(generationInfoID) : null);
    }

    public StructureGenerator<S> random(@Nonnull Random random)
    {
        this.random = random;
        return this;
    }

    public StructureGenerator<S> transform(@Nonnull AxisAlignedTransform2D transform)
    {
        this.transform = transform;
        return this;
    }

    @Nonnull
    public AxisAlignedTransform2D transform()
    {
        if (this.transform != null)
            return this.transform;
        else
        {
            StructureInfo<S> structureInfo = structure();
            Random random = random();

            return AxisAlignedTransform2D.from(structureInfo.isRotatable() ? random.nextInt(4) : 0, structureInfo.isMirrorable() && random.nextBoolean());
        }
    }

    public StructureGenerator<S> boundingBox(@Nonnull StructureBoundingBox boundingBox)
    {
        this.boundingBox = boundingBox;
        return this;
    }

    public Optional<StructureBoundingBox> boundingBox()
    {
        return boundingBox(true);
    }

    @Nonnull
    public StructureBoundingBox surfaceBoundingBox()
    {
        //noinspection OptionalGetWithoutIsPresent
        return boundingBox(false).get();
    }

    @Nonnull
    protected Optional<StructureBoundingBox> boundingBox(boolean placed)
    {
        StructureBoundingBox boundingBox = this.boundingBox != null ? this.boundingBox : null;

        if (boundingBox == null)
        {
            int[] size = structureSize();

            if (this.lowerCoord != null)
                boundingBox = StructureInfos.structureBoundingBox(fromCenter ? lowerCoord.subtract(new Vec3i(size[0] / 2, 0, size[2] / 2)) : lowerCoord, structureSize());
            else if (surfacePos != null && placer != null)
            {
                boundingBox = StructureInfos.structureBoundingBox((fromCenter ? surfacePos.subtract(size[0] / 2, size[2] / 2) : surfacePos).blockPos(0), structureSize());

                if (placed)
                {
                    int y = placer.selectY(place(), structure().blockCollection());
                    if (y < 0) return Optional.empty();
                    boundingBox.minY += y;
                    boundingBox.maxY += y;
                }
            }
            else
                throw new IllegalStateException("No place!");
        }

        if (placed)
            this.boundingBox = boundingBox;

        return Optional.of(boundingBox);
    }

    public int[] structureSize()
    {
        return StructureInfos.structureSize(structure(), transform());
    }

    @Nullable
    public StructureBoundingBox generationBB()
    {
        return generationBB;
    }

    public StructureGenerator<S> generationBB(@Nullable StructureBoundingBox generationBB)
    {
        this.generationBB = generationBB;
        return this;
    }

    public StructureGenerator<S> generationInfo(@Nullable StructureGenerationInfo generationInfo)
    {
        this.generationInfo = generationInfo;
        return this;
    }

    public StructureGenerator<S> generationInfo(@Nullable String generationInfo)
    {
        this.generationInfoID = generationInfo;
        return this;
    }

    public StructureGenerator<S> generationLayer(int generationLayer)
    {
        this.generationLayer = generationLayer;
        return this;
    }

    public StructureGenerator<S> asSource(boolean generateAsSource)
    {
        this.generateAsSource = generateAsSource;
        return this;
    }

    public StructureGenerator<S> maturity(StructureSpawnContext.GenerateMaturity generateMaturity)
    {
        this.generateMaturity = generateMaturity;
        return this;
    }

    public StructureSpawnContext.GenerateMaturity maturity()
    {
        return generateMaturity;
    }

    public StructureGenerator<S> instanceData(S s)
    {
        instanceData = s;
        return this;
    }

    public StructureGenerator<S> instanceData(NBTBase nbt)
    {
        this.instanceDataNBT = nbt;
        return this;
    }

    @Nonnull
    public Optional<S> instanceData()
    {
        return this.instanceData != null ? Optional.of(this.instanceData)
                : this.instanceDataNBT != null ? load().map(load -> structure().loadInstanceData(load, this.instanceDataNBT))
                : prepare().map(prepare -> structure().prepareInstanceData(prepare));
    }

    public StructureGenerator<S> memorize(boolean memorize)
    {
        this.memorize = memorize;
        return this;
    }

    public StructureGenerator<S> allowOverlaps(boolean allowOverlaps)
    {
        this.allowOverlaps = allowOverlaps;
        return this;
    }

    @Nonnull
    public StructurePlaceContext place()
    {
        return new StructurePlaceContext(random(), environment(), transform(), surfaceBoundingBox());
    }

    @Nonnull
    public Optional<StructurePrepareContext> prepare()
    {
        return boundingBox().map(bb -> new StructurePrepareContext(random(), environment(), transform(), bb, generateAsSource));
    }

    @Nonnull
    public Optional<StructureLoadContext> load()
    {
        return boundingBox().map(bb -> new StructureLoadContext(transform(), bb, generateAsSource));
    }

    @Nonnull
    public Optional<StructureSpawnContext> spawn()
    {
        return boundingBox().map(bb -> new StructureSpawnContext(environment(), random(), transform(), bb, generationBB, generationLayer, generateAsSource, generateMaturity));
    }

}
