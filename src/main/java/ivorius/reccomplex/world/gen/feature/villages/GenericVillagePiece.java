/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.villages;

import ivorius.ivtoolkit.blocks.Directions;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.world.chunk.gen.StructureBoundingBoxes;
import ivorius.reccomplex.nbt.NBTStorable;
import ivorius.reccomplex.utils.RCAxisAlignedTransform;
import ivorius.reccomplex.world.gen.feature.StructureGenerator;
import ivorius.reccomplex.world.gen.feature.WorldStructureGenerationData;
import ivorius.reccomplex.world.gen.feature.structure.Environment;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.GenerationType;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.VanillaGeneration;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraft.world.gen.structure.template.TemplateManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.Random;

/**
 * Created by lukas on 18.01.15.
 */
public class GenericVillagePiece extends StructureVillagePieces.Village
{
    public String structureID;
    public String generationID;
    public AxisAlignedTransform2D transform;

    public long seed;

    public boolean startedGeneration;
    public NBTBase instanceData;

    public GenericVillagePiece()
    {
    }

    public GenericVillagePiece(StructureVillagePieces.Start start, int generationDepth)
    {
        super(start, generationDepth);
    }

    public static AxisAlignedTransform2D getTransform(@Nonnull EnumFacing front, boolean mirrorX, @Nonnull EnumFacing toFront)
    {
        return AxisAlignedTransform2D.from(getRotations(front, mirrorX, toFront), mirrorX);
    }

    @Nullable
    public static AxisAlignedTransform2D getTransform(@Nullable EnumFacing front, boolean canMirror, boolean canRotate, EnumFacing toFront, Random random)
    {
        boolean mirror = canMirror && random.nextBoolean();

        AxisAlignedTransform2D transform = front != null
                ? getTransform(front, mirror, toFront)
                : AxisAlignedTransform2D.from((canRotate ? random.nextInt(4) : 0), mirror);

        return canRotate || transform.getRotation() == 0 ? transform : null;
    }

    public static int getRotations(EnumFacing front, boolean mirrorX, EnumFacing toFront)
    {
        Integer rotations = Directions.getHorizontalClockwiseRotations(front, toFront, mirrorX);
        return rotations == null ? 0 : rotations;
    }

    @Nullable
    public static GenericVillagePiece create(String structureID, String generationID)
    {
        return VanillaGenerationClassFactory.instance().create(structureID, generationID);
    }

    @Nullable
    public static GenericVillagePiece create(String structureID, String generationID, StructureVillagePieces.Start start, int generationDepth)
    {
        return VanillaGenerationClassFactory.instance().create(structureID, generationID, start, generationDepth);
    }

    public static boolean canVillageGoDeeperC(StructureBoundingBox box)
    {
        return canVillageGoDeeper(box);
    }

    public void setIds(String structureID, String generationID)
    {
        this.structureID = structureID;
        this.generationID = generationID;
    }

    public void setOrientation(EnumFacing front, AxisAlignedTransform2D transform, StructureBoundingBox boundingBox)
    {
        setCoordBaseMode(front);
        this.transform = transform;
        this.boundingBox = boundingBox;
    }

    @Nonnull
    private Biome biome(WorldServer world)
    {
        return startPiece != null ? startPiece.biome : Environment.getBiome(world, boundingBox);
    }

    @Nonnull
    protected Environment environment(WorldServer world, GenerationType generationType)
    {
        return new Environment(world, biome(world), structureType, generationType);
    }

    public void prepare(WorldServer world)
    {
        Structure<?> structure = StructureRegistry.INSTANCE.get(structureID);

        if (structure == null)
            return;

        GenerationType generationType = structure.generationType(generationID);

        if (!(generationType instanceof VanillaGeneration))
            return;

        VanillaGeneration vanillaGenInfo = (VanillaGeneration) generationType;

        instanceData = new StructureGenerator<>(structure).seed(seed).environment(environment(world, generationType)).transform(transform).boundingBox(boundingBox)
                .instanceData().map(NBTStorable::writeToNBT).orElse(null);
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean addComponentParts(World world, Random random, StructureBoundingBox boundingBox)
    {
        Structure<?> structure = StructureRegistry.INSTANCE.get(structureID);
        if (structure == null)
            return false;

        GenerationType generationType = structure.generationType(generationID);

        if (!(generationType instanceof VanillaGeneration))
            return false;

        VanillaGeneration vanillaGenInfo = (VanillaGeneration) generationType;

        BlockPos structureShift = transform.apply(vanillaGenInfo.spawnShift, new int[]{1, 1, 1});

        if (this.averageGroundLvl < 0) {
            this.averageGroundLvl = this.getAverageGroundLevel(world, boundingBox);

            if (this.averageGroundLvl < 0)
                return true;

            // Structure shift y was included in bounding box, but must be re-added because it is overwritten
            this.boundingBox.offset(0, this.averageGroundLvl - this.boundingBox.minY + structureShift.getY(), 0);
        }

        if (world instanceof WorldServer)
            generate((WorldServer) world, boundingBox, structure, generationType);

        return true;
    }

    protected <T extends NBTStorable> void generate(WorldServer world, StructureBoundingBox generationBB, Structure<T> structure, GenerationType generationType)
    {
        if (!startedGeneration)
            prepare(world);

        boolean firstTime = !startedGeneration;
        StructureGenerator.GenerationResult result = new StructureGenerator<>(structure).environment(environment(world, generationType))
                .seed(seed).lowerCoord(StructureBoundingBoxes.min(boundingBox)).transform(transform).generationBB(StructureBoundingBoxes.wholeHeightBoundingBox(world, generationBB))
                .generationLayer(componentType).structureID(structureID).maturity(firstTime ? StructureSpawnContext.GenerateMaturity.FIRST : StructureSpawnContext.GenerateMaturity.COMPLEMENT)
                .instanceData(this.instanceData).generate();

        if (result instanceof StructureGenerator.GenerationResult.Success.New) {
            WorldStructureGenerationData.StructureEntry sight = ((StructureGenerator.GenerationResult.Success.New) result).sight;
            
            sight.setPreventComplementation(true);
        }

        startedGeneration = true;
    }

    @Override
    protected void writeStructureToNBT(NBTTagCompound compound)
    {
        super.writeStructureToNBT(compound);

        compound.setString("RcSId", structureID);
        compound.setString("RcGtId", generationID);

        RCAxisAlignedTransform.write(compound, transform, "RcRotation", "RcMirror");

        compound.setLong("seed", seed);
        compound.setBoolean("RcStartGen", startedGeneration);
        if (instanceData != null)
            compound.setTag("RcInstDat", instanceData);
    }

    @Override
    protected void readStructureFromNBT(NBTTagCompound compound, TemplateManager manager)
    {
        super.readStructureFromNBT(compound, manager);

        structureID = compound.getString("RcSId");
        generationID = compound.getString("RcGtId");

        seed = compound.hasKey("seed") ? compound.getLong("seed")
                : new Random().nextLong(); // Legacy

        transform = RCAxisAlignedTransform.read(compound, "RcRotation", "RcMirror");

        startedGeneration = compound.getBoolean("RcStartGen");
        instanceData = compound.hasKey("RcInstDat") ? compound.getTag("RcInstDat") : null;
    }
}
