/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.villages;

import cpw.mods.fml.common.registry.VillagerRegistry;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.utils.WeightedObject;
import ivorius.reccomplex.worldgen.StructureHandler;
import ivorius.reccomplex.worldgen.StructureInfo;
import ivorius.reccomplex.worldgen.WorldGenStructures;
import ivorius.reccomplex.worldgen.genericStructures.gentypes.VanillaStructureSpawnInfo;
import net.minecraft.util.MathHelper;
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
    @Override
    public StructureVillagePieces.PieceWeight getVillagePieceWeight(Random random, int villageSize)
    {
        return new StructureVillagePieces.PieceWeight(GenericVillagePiece.class, 0, Integer.MAX_VALUE);
    }

    @Override
    public Class<?> getComponentClass()
    {
        return GenericVillagePiece.class;
    }

    @Override
    public Object buildComponent(StructureVillagePieces.PieceWeight villagePiece, StructureVillagePieces.Start start, List components, Random random, int x, int y, int z, int rotation, int generationDepth)
    {
        return null;
//        StructureInfo structureInfo = StructureHandler.getStructure(structureID);
//        VanillaStructureSpawnInfo spawnInfo = structureInfo.vanillaStructureSpawnInfo();
//
//        int minLimit = spawnInfo.minBaseLimit + villageSize * spawnInfo.minScaledLimit;
//        int maxLimit = spawnInfo.maxBaseLimit + villageSize * spawnInfo.maxScaledLimit;
//
//        AxisAlignedTransform2D transform = AxisAlignedTransform2D.transform(rotation, false);
//        int[] structureBB = WorldGenStructures.structureSize(structureInfo, transform);
//        BlockCoord structureShift = spawnInfo.spawnShift;
//
//        StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, structureShift.x, 0, structureShift.z, structureBB[0], structureBB[1], structureBB[2], rotation);
//        return GenericVillagePiece.canVillageGoDeeperC(structureboundingbox) && StructureComponent.findIntersecting(components, structureboundingbox) == null ? new GenericVillagePiece(start, generationDepth, structureID, rotation, structureboundingbox) : null;
    }
}
