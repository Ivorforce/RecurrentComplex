/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.operation;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockPositions;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.rendering.grid.BlockQuadCache;
import ivorius.ivtoolkit.rendering.grid.GridQuadCache;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.client.rendering.OperationRenderer;
import ivorius.reccomplex.utils.RCAxisAlignedTransform;
import ivorius.reccomplex.utils.RCStrings;
import ivorius.reccomplex.world.gen.feature.StructureGenerator;
import ivorius.reccomplex.world.gen.feature.structure.ReadableInstanceData;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import ivorius.reccomplex.world.gen.feature.structure.generic.StructureSaveHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Created by lukas on 10.02.15.
 */
public class OperationGenerateStructure implements Operation
{
    public GenericStructure structure;

    public AxisAlignedTransform2D transform;
    public BlockPos lowerCoord;

    public boolean generateAsSource;

    public String structureID;
    public String generationInfoID;

    protected GridQuadCache cachedShapeGrid;

    protected String seed;

    protected final ReadableInstanceData<GenericStructure.InstanceData> instanceData = new ReadableInstanceData<>();

    public OperationGenerateStructure()
    {
    }

    public OperationGenerateStructure(GenericStructure structure, String generationInfoID, AxisAlignedTransform2D transform, BlockPos lowerCoord, boolean generateAsSource)
    {
        this.structure = structure;
        this.generationInfoID = generationInfoID;
        this.transform = transform;
        this.lowerCoord = lowerCoord;
        this.generateAsSource = generateAsSource;
    }

    public OperationGenerateStructure dummy(GenericStructure structure)
    {
        OperationGenerateStructure op = new OperationGenerateStructure(structure, generationInfoID, transform, lowerCoord, generateAsSource)
                .withSeed(seed)
                .withStructureID(structureID);
        op.instanceData.set(instanceData);
        return op;
    }

    public String getStructureID()
    {
        return structureID;
    }

    public void setStructureID(String structureID)
    {
        this.structureID = structureID;
    }

    public OperationGenerateStructure withStructureID(String structureIDForSaving)
    {
        this.structureID = structureIDForSaving;
        return this;
    }

    public OperationGenerateStructure withSeed(String seed)
    {
        this.seed = seed;
        return this;
    }

    public OperationGenerateStructure prepare(WorldServer world)
    {
        return prepare(generator(world).instanceData());
    }

    public OperationGenerateStructure prepare(@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<GenericStructure.InstanceData> instanceData)
    {
        this.instanceData.setInstanceData(instanceData.orElse(null));
        return this;
    }

    @Override
    public void perform(WorldServer world)
    {
        if (!instanceData.exists())
            throw new IllegalStateException();

        StructureGenerator<GenericStructure.InstanceData> generator = generator(world);
        instanceData.load(generator);

        generator.generate();
    }

    public StructureGenerator<GenericStructure.InstanceData> generator(WorldServer world)
    {
        StructureGenerator<GenericStructure.InstanceData> generator = new StructureGenerator<>(structure).world(world).generationInfo(generationInfoID)
                .seed(RCStrings.seed(seed))
                .transform(transform).lowerCoord(lowerCoord)
                .maturity(StructureSpawnContext.GenerateMaturity.FIRST).asSource(generateAsSource);

        instanceData.register(generator);

        if (structureID != null)
            generator.structureID(structureID);

        return generator;
    }

    @Override
    public boolean checkDead(ICommandSender target)
    {
        if (!instanceData.exists() || lowerCoord == null)
        {
            String reason = StructureGenerator.GenerationResult.Failure.placement.description;
            target.sendMessage(RecurrentComplex.translations.format("commands.strucGen.failure", reason));

            return true;
        }

        return false;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        compound.setString("structureInfo", StructureSaveHandler.INSTANCE.toJSON(structure));
        compound.setTag("structureData", structure.worldDataCompound);

        RCAxisAlignedTransform.write(compound, transform, "rotation", "mirrorX");

        BlockPositions.writeToNBT("lowerCoord", lowerCoord, compound);

        compound.setBoolean("generateAsSource", generateAsSource);

        if (structureID != null)
            compound.setString("structureIDForSaving", structureID);

        if (seed != null)
            compound.setString("seed", seed);

        instanceData.writeToNBT("instanceData", compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        structure = StructureSaveHandler.INSTANCE.fromJSON(compound.getString("structureInfo"),
                compound.getCompoundTag("structureData"));

        transform = RCAxisAlignedTransform.read(compound, "rotation", "mirrorX");

        lowerCoord = BlockPositions.readFromNBT("lowerCoord", compound);

        generateAsSource = compound.getBoolean("generateAsSource");

        structureID = compound.hasKey("structureIDForSaving", Constants.NBT.TAG_STRING)
                ? compound.getString("structureIDForSaving")
                : null;

        seed = compound.hasKey("seed") ? compound.getString("seed") : null;

        instanceData.readFromNBT("instanceData", compound);
    }

    public void invalidateCache()
    {
        cachedShapeGrid = null;
    }

//    float partial = 0;
//    @Override
//    public void update(World world, int ticks)
//    {
//        if (!world.isRemote)
//        {
//            float before = partial;
//            partial = Math.min(partial + 1f / 100f, 1);
//
//            if (partial > before)
//            {
//                StructureGenerator<GenericStructureInfo.InstanceData> generator = generator((WorldServer) world);
//
//                BlurredValueField field = new BlurredValueField(generator.structureSize());
//                Random random = new Random(123810283);
//                for (int i = 0; i < 10; i++)
//                    field.addValue(random.nextFloat(), random);
//                generator.generationPredicate(v ->
//                {
//                    float val = field.getValue(v.getX() - lowerCoord.getX(), v.getY() - lowerCoord.getY(), v.getZ() - lowerCoord.getZ());
//                    return val < partial && val >= before;
//                }).generate();
//            }
//        }
//    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderPreview(PreviewType previewType, World world, int ticks, float partialTicks)
    {
        if (previewType == PreviewType.SHAPE)
        {
            GlStateManager.color(0.8f, 0.75f, 1.0f);
            OperationRenderer.renderGridQuadCache(
                    cachedShapeGrid != null ? cachedShapeGrid : (cachedShapeGrid = BlockQuadCache.createQuadCache(structure.constructWorldData().blockCollection, new float[]{1, 1, 1})),
                    transform, lowerCoord, ticks, partialTicks);
        }

        if (previewType == PreviewType.BOUNDING_BOX || previewType == PreviewType.SHAPE)
            OperationRenderer.renderBoundingBox(generationArea(), ticks, partialTicks);
    }

    @Nullable
    public BlockArea generationArea()
    {
        if (structure == null)
            return null;

        int[] size = structure.size();
        return OperationRenderer.blockAreaFromSize(lowerCoord, RCAxisAlignedTransform.applySize(transform, size));
    }
}
