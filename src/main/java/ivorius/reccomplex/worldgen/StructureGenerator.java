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
    private YSelector ySelector;
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

    private int generationLayer = 0;

    private boolean generateAsSource = false;
    private StructureSpawnContext.GenerateMaturity generateMaturity = StructureSpawnContext.GenerateMaturity.FIRST;

    @Nullable
    private S instanceData;
    @Nullable
    private NBTBase instanceDataNBT;

    private boolean memorize = true;

    public StructureGenerator(StructureInfo<S> structureInfo)
    {
        structure(structureInfo);
    }

    public StructureGenerator()
    {

    }

    @Nonnull
    public static YSelector worldHeightYSelector()
    {
        return (world1, random1, boundingBox) -> world1.getHeight(new BlockPos(boundingBox.getCenter())).getY();
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

    @Nullable
    public StructureSpawnContext generate()
    {
        StructureSpawnContext context = spawn();

        StructureInfo<S> structureInfo = structure();
        String structureID = structureID();
        boolean firstTime = context.isFirstTime();

        WorldServer world = context.environment.world;

        int[] sizeInts = sizeInts(context.boundingBox);
        int[] coordInts = coordInts(context.boundingBox);

        if (maturity() != StructureSpawnContext.GenerateMaturity.SUGGEST || (
                context.boundingBox.minY >= MIN_DIST_TO_LIMIT && context.boundingBox.maxY <= world.getHeight() - 1 - MIN_DIST_TO_LIMIT
                        && (!RCConfig.avoidOverlappingGeneration || !memorize || StructureGenerationData.get(world).getEntriesAt(context.boundingBox).size() == 0)
                        && !RCEventBus.INSTANCE.post(new StructureGenerationEvent.Suggest(structureInfo, context))
                        && !MinecraftForge.EVENT_BUS.post(new StructureGenerationEventLite.Suggest(world, structureID, coordInts, sizeInts, context.generationLayer))
        ))
        {
            if (firstTime)
            {
                RCEventBus.INSTANCE.post(new StructureGenerationEvent.Pre(structureInfo, context));
                MinecraftForge.EVENT_BUS.post(new StructureGenerationEventLite.Pre(world, structureID, coordInts, sizeInts, context.generationLayer));
            }

            boolean success = structureInfo.generate(context, instanceData());

            if (firstTime && success)
            {
                RecurrentComplex.logger.trace(String.format("Generated structure '%s' in %s", name(structureID), context.boundingBox));

                RCEventBus.INSTANCE.post(new StructureGenerationEvent.Post(structureInfo, context));
                MinecraftForge.EVENT_BUS.post(new StructureGenerationEventLite.Post(world, structureID, coordInts, sizeInts, context.generationLayer));

                if (structureID != null && memorize)
                    StructureGenerationData.get(world).addCompleteEntry(structureID, context.lowerCoord(), context.transform);
            }

            return success ? context : null;
        }
        else
            RecurrentComplex.logger.trace(String.format("Canceled structure '%s' generation in %s", structureID, context.boundingBox));

        return null;
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
        if (world == null) throw new IllegalArgumentException("No world!");
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
        if (structureInfo == null) throw new IllegalArgumentException();
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

    public StructureGenerator<S> randomPosition(@Nonnull BlockSurfacePos surfacePos, @Nullable YSelector ySelector)
    {
        this.surfacePos = surfacePos;
        this.ySelector = ySelector != null ? ySelector : worldHeightYSelector();
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
        return environment != null ? environment : Environment.inNature(world(), boundingBox());
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
    protected AxisAlignedTransform2D transform()
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

    @Nonnull
    public StructureBoundingBox boundingBox()
    {
        WorldServer world = world();
        Random random = random();

        StructureBoundingBox boundingBox = this.boundingBox != null ? this.boundingBox : null;

        if (boundingBox == null)
        {
            int[] size = structureSize();

            if (this.lowerCoord != null)
                boundingBox = StructureInfos.structureBoundingBox(fromCenter ? lowerCoord.subtract(new Vec3i(size[0] / 2, 0, size[2] / 2)) : lowerCoord, structureSize());
            else if (surfacePos != null && ySelector != null)
            {
                boundingBox = StructureInfos.structureBoundingBox((fromCenter ? surfacePos.subtract(size[0] / 2, size[2] / 2) : surfacePos).blockPos(0), structureSize());
                int y = ySelector.selectY(world, random, boundingBox);
                boundingBox.minY += y;
                boundingBox.maxY += y;
            }
            else
                throw new IllegalArgumentException("No place!");
        }
        return boundingBox;
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
    public S instanceData()
    {
        return this.instanceData != null ? this.instanceData :
                this.instanceDataNBT != null ? structure().loadInstanceData(load(), this.instanceDataNBT)
                        : structure().prepareInstanceData(prepare());
    }

    public StructureGenerator<S> memorize(boolean memorize)
    {
        this.memorize = memorize;
        return this;
    }

    @Nonnull
    public StructurePrepareContext prepare()
    {
        return new StructurePrepareContext(random(), environment(), transform(), boundingBox(), generateAsSource);
    }

    @Nonnull
    public StructureLoadContext load()
    {
        return new StructureLoadContext(transform(), boundingBox(), generateAsSource);
    }

    public StructureSpawnContext spawn()
    {
        return new StructureSpawnContext(environment(), random(), transform(), boundingBox(), generationBB, generationLayer, generateAsSource, generateMaturity);
    }

}
