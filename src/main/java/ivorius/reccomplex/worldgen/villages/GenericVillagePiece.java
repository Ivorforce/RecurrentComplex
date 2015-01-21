/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.villages;

import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.worldgen.StructureRegistry;
import ivorius.reccomplex.worldgen.StructureInfo;
import ivorius.reccomplex.worldgen.StructureSpawnContext;
import ivorius.reccomplex.worldgen.genericStructures.gentypes.VanillaStructureSpawnInfo;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureVillagePieces;

import java.util.Random;

/**
 * Created by lukas on 18.01.15.
 */
public class GenericVillagePiece extends StructureVillagePieces.Village
{
    public String structureID;

    public GenericVillagePiece()
    {
    }

    public GenericVillagePiece(StructureVillagePieces.Start start, int generationDepth, String structureID, int rotation, StructureBoundingBox bb)
    {
        super(start, generationDepth);
        this.structureID = structureID;
        this.coordBaseMode = rotation;
        this.boundingBox = bb;
    }

    public static boolean canVillageGoDeeperC(StructureBoundingBox box)
    {
        return canVillageGoDeeper(box);
    }

    @Override
    public boolean addComponentParts(World world, Random random, StructureBoundingBox boundingBox)
    {
        StructureInfo structureInfo = StructureRegistry.getStructure(structureID);
        VanillaStructureSpawnInfo spawnInfo = structureInfo.vanillaStructureSpawnInfo();

        BlockCoord structureShift = spawnInfo.spawnShift;
        AxisAlignedTransform2D transform = AxisAlignedTransform2D.transform(coordBaseMode, false);

        if (this.field_143015_k < 0)
        {
            this.field_143015_k = this.getAverageGroundLevel(world, boundingBox);

            if (this.field_143015_k < 0)
                return true;

            this.boundingBox.offset(0, this.field_143015_k - this.boundingBox.minY + structureShift.y, 0);
        }

        structureInfo.generate(new StructureSpawnContext(world, random, boundingBox, 0, false, transform));

        return true;
    }

    protected void func_143012_a(NBTTagCompound tagCompound)
    {
        super.func_143012_a(tagCompound);
        tagCompound.setString("RcSID", structureID);
    }

    protected void func_143011_b(NBTTagCompound tagCompound)
    {
        super.func_143011_b(tagCompound);
        this.structureID = tagCompound.getString("RcSID");
    }
}
