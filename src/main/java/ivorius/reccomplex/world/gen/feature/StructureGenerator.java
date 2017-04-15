/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature;

import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.events.RCEventBus;
import ivorius.reccomplex.events.StructureGenerationEvent;
import ivorius.reccomplex.events.StructureGenerationEventLite;
import ivorius.ivtoolkit.world.chunk.gen.StructureBoundingBoxes;
import ivorius.reccomplex.world.gen.feature.structure.*;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureLoadContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructurePrepareContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.GenerationType;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.StructurePlaceContext;
import ivorius.ivtoolkit.blocks.BlockSurfacePos;
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
import java.util.function.Predicate;

/**
 * Created by lukas on 19.01.15.
 */
public class StructureGenerator<S extends NBTStorable>
{
    public static final int MIN_DIST_TO_LIMIT = 1;

    @Nullable
    private WorldServer world;
    @Nullable
    private Structure<S> structure;
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
    @Nullable
    private Predicate<Vec3i> generationPredicate;

    private String generationInfoID;
    private GenerationType generationType;
    private int generationLayer = 0;

    private boolean generateAsSource = false;
    private StructureSpawnContext.GenerateMaturity generateMaturity = StructureSpawnContext.GenerateMaturity.FIRST;

    @Nullable
    private S instanceData;
    @Nullable
    private NBTBase instanceDataNBT;

    private boolean allowOverlaps = false;
    private boolean memorize = true;

    public StructureGenerator(Structure<S> structure)
    {
        structure(structure);
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

        Structure<S> structure = structure();
        String structureID = structureID();
        boolean firstTime = spawn.generateMaturity.isFirstTime();

        WorldServer world = spawn.environment.world;

        if (maturity() != StructureSpawnContext.GenerateMaturity.SUGGEST || (
                spawn.boundingBox.minY >= MIN_DIST_TO_LIMIT && spawn.boundingBox.maxY <= world.getHeight() - 1 - MIN_DIST_TO_LIMIT
                        && (!RCConfig.avoidOverlappingGeneration || allowOverlaps || WorldStructureGenerationData.get(world).entriesAt(spawn.boundingBox).noneMatch(WorldStructureGenerationData.Entry::blocking))
                        && !RCEventBus.INSTANCE.post(new StructureGenerationEvent.Suggest(structure, spawn))
                        && (structureID == null || !MinecraftForge.EVENT_BUS.post(new StructureGenerationEventLite.Suggest(world, structureID, spawn.boundingBox, spawn.generationLayer, firstTime)))
        ))
        {
            if (firstTime)
            {
                RCEventBus.INSTANCE.post(new StructureGenerationEvent.Pre(structure, spawn));
                if (structureID != null)
                    MinecraftForge.EVENT_BUS.post(new StructureGenerationEventLite.Pre(world, structureID, spawn.boundingBox, spawn.generationLayer, firstTime));
            }

            boolean success = structure.generate(spawn, instanceData, RCConfig.getUniversalTransformer());

            if (firstTime && success)
            {
                RecurrentComplex.logger.trace(String.format("Generated structure '%s' in %s", name(structureID), spawn.boundingBox));

                RCEventBus.INSTANCE.post(new StructureGenerationEvent.Post(structure, spawn));
                if (structureID != null)
                    MinecraftForge.EVENT_BUS.post(new StructureGenerationEventLite.Post(world, structureID, spawn.boundingBox, spawn.generationLayer, firstTime));

                if (structureID != null && memorize)
                {
                    String generationInfoID = generationType != null ? generationType.id() : null;

                    WorldStructureGenerationData.StructureEntry structureEntry = WorldStructureGenerationData.StructureEntry.complete(structureID, generationInfoID, spawn.boundingBox, spawn.transform);
                    structureEntry.blocking = structure.isBlocking();
                    WorldStructureGenerationData.get(world).addEntry(structureEntry);
                }
            }

            return success ? spawnO : Optional.empty();
        }
        else
            RecurrentComplex.logger.trace(String.format("Canceled structure '%s' generation in %s", structureID, spawn.boundingBox));

        return Optional.empty();
    }

    public StructureGenerator<S> asChild(StructureSpawnContext context)
    {
        return environment(context.environment.copy()).random(context.random).transform(context.transform)
                .generationBB(context.generationBB).generationPredicate(context.generationPredicate).generationLayer(context.generationLayer + 1)
                .asSource(context.generateAsSource).maturity(context.generateMaturity.isFirstTime() ? StructureSpawnContext.GenerateMaturity.FIRST : StructureSpawnContext.GenerateMaturity.COMPLEMENT);
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

    public StructureGenerator<S> structure(@Nonnull Structure<S> structure)
    {
        this.structure = structure;
        return this;
    }

    public StructureGenerator<S> structureID(@Nonnull String structureID)
    {
        this.structureID = structureID;
        return this;
    }

    @Nonnull
    public Structure<S> structure()
    {
        //noinspection unchecked
        Structure<S> structure = this.structure != null ? this.structure : structureID != null ? (Structure<S>) StructureRegistry.INSTANCE.get(structureID) : null;
        if (structure == null) throw new IllegalStateException();
        return structure;
    }

    @Nullable
    public String structureID()
    {
        return this.structureID != null ? this.structureID : this.structure != null ? StructureRegistry.INSTANCE.id(structure) : null;
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
            return boundingBox().map(StructureBoundingBoxes::min);
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
    public Environment environment()
    {
        return environment != null ? environment : Environment.inNature(world(), surfaceBoundingBox(),
                generationType != null ? generationType : generationInfoID != null ? structure().generationInfo(generationInfoID) : null);
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
            Structure<S> structure = structure();
            Random random = random();

            AxisAlignedTransform2D transform = AxisAlignedTransform2D.from(structure.isRotatable() ? random.nextInt(4) : 0, structure.isMirrorable() && random.nextBoolean());
            return this.transform = transform;
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
                boundingBox = Structures.structureBoundingBox(fromCenter ? lowerCoord.subtract(new Vec3i(size[0] / 2, 0, size[2] / 2)) : lowerCoord, size);
            else if (surfacePos != null && placer != null)
            {
                boundingBox = Structures.structureBoundingBox((fromCenter ? surfacePos.subtract(size[0] / 2, size[2] / 2) : surfacePos).blockPos(0), size);

                if (placed)
                {
                    int y = placer.place(place(), structure().blockCollection());
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
        return Structures.structureSize(structure(), transform());
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

    @Nullable
    public Predicate<Vec3i> generationPredicate()
    {
        return generationPredicate;
    }

    public StructureGenerator<S> generationPredicate(@Nullable Predicate<Vec3i> generationPredicate)
    {
        this.generationPredicate = generationPredicate;
        return this;
    }

    public StructureGenerator<S> generationInfo(@Nullable GenerationType generationType)
    {
        this.generationType = generationType;
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
                : this.instanceDataNBT != null ? load().map(load -> structure().loadInstanceData(load, this.instanceDataNBT, RCConfig.getUniversalTransformer()))
                : prepare().map(prepare -> structure().prepareInstanceData(prepare, RCConfig.getUniversalTransformer()));
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
        return boundingBox().map(bb -> new StructureSpawnContext(environment(), random(), transform(), bb, generationBB, generationPredicate, generationLayer, generateAsSource, generateMaturity));
    }

}
