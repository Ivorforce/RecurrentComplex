/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.villages;

import ivorius.reccomplex.RCConfig;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.Structures;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.gentypes.GenerationInfo;
import ivorius.reccomplex.world.gen.feature.structure.generic.gentypes.VanillaGenerationInfo;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces;

import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 18.01.15.
 */
public class GenericVillageCreationHandler implements VillagerRegistry.IVillageCreationHandler
{
    protected String structureID;
    protected String generationID;

    public GenericVillageCreationHandler(String structureID, String generationID)
    {
        this.structureID = structureID;
        this.generationID = generationID;
    }

    public static GenericVillageCreationHandler forGeneration(String structureID, String generationID)
    {
        return getPieceClass(structureID, generationID) != null
                ? new GenericVillageCreationHandler(structureID, generationID)
                : null;
    }

    public static Class<? extends GenericVillagePiece> getPieceClass(String structureID, String generationID)
    {
        return VanillaGenerationClassFactory.instance().getClass(structureID, generationID);
    }

    @Override
    public StructureVillagePieces.PieceWeight getVillagePieceWeight(Random random, int villageSize)
    {
        Structure structure = StructureRegistry.INSTANCE.hasActive(structureID) ? StructureRegistry.INSTANCE.get(structureID) : null;
        if (structure != null)
        {
            float tweakedWeight = RCConfig.tweakedSpawnRate(structureID);
            GenerationInfo generationInfo = structure.generationInfo(generationID);
            if (generationInfo instanceof VanillaGenerationInfo)
            {
                VanillaGenerationInfo vanillaGenInfo = (VanillaGenerationInfo) generationInfo;

                int spawnLimit = MathHelper.floor_double(MathHelper.getRandomDoubleInRange(random,
                        vanillaGenInfo.minBaseLimit + villageSize * vanillaGenInfo.minScaledLimit,
                        vanillaGenInfo.maxBaseLimit + villageSize * vanillaGenInfo.maxScaledLimit) + 0.5);
                return new StructureVillagePieces.PieceWeight(getComponentClass(), vanillaGenInfo.getVanillaWeight(tweakedWeight), spawnLimit);
            }
        }

        return new StructureVillagePieces.PieceWeight(getComponentClass(), 0, 0);
    }

    @Override
    public Class<? extends StructureVillagePieces.Village> getComponentClass()
    {
        return getPieceClass(structureID, generationID);
    }

    @Override
    public StructureVillagePieces.Village buildComponent(StructureVillagePieces.PieceWeight villagePiece, StructureVillagePieces.Start startPiece, List<StructureComponent> pieces, Random random, int x, int y, int z, EnumFacing front, int generationDepth)
    {
        Structure structure = StructureRegistry.INSTANCE.get(structureID);

        if (structure != null)
        {
            GenerationInfo generationInfo = structure.generationInfo(generationID);
            if (generationInfo instanceof VanillaGenerationInfo)
            {
                VanillaGenerationInfo vanillaGenInfo = (VanillaGenerationInfo) generationInfo;

                boolean mirrorX = structure.isMirrorable() && random.nextBoolean();
                AxisAlignedTransform2D transform = GenericVillagePiece.getTransform(vanillaGenInfo.front, mirrorX, front.getOpposite());

                if (vanillaGenInfo.generatesIn(startPiece.biome) && (structure.isRotatable() || transform.getRotation() == 0))
                {
                    int[] structureSize = Structures.structureSize(structure, transform);

                    StructureBoundingBox strucBB = Structures.structureBoundingBox(new BlockPos(x, y, z), structureSize);

                    if (GenericVillagePiece.canVillageGoDeeperC(strucBB) && StructureComponent.findIntersecting(pieces, strucBB) == null)
                    {
                        GenericVillagePiece genericVillagePiece = GenericVillagePiece.create(structureID, generationID, startPiece, generationDepth);

                        if (genericVillagePiece != null)
                        {
                            genericVillagePiece.setIds(structureID, generationID);
                            genericVillagePiece.setOrientation(front, mirrorX, strucBB);

                            return genericVillagePiece;
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenericVillageCreationHandler that = (GenericVillageCreationHandler) o;

        if (!structureID.equals(that.structureID)) return false;
        return generationID.equals(that.generationID);

    }

    @Override
    public int hashCode()
    {
        int result = structureID.hashCode();
        result = 31 * result + generationID.hashCode();
        return result;
    }
}
