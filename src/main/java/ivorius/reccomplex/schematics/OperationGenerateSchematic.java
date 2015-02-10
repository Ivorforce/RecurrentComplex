/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.schematics;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.reccomplex.client.rendering.AreaRenderer;
import ivorius.reccomplex.client.rendering.SelectionRenderer;
import ivorius.reccomplex.operation.Operation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

/**
 * Created by lukas on 10.02.15.
 */
public class OperationGenerateSchematic implements Operation
{
    public SchematicFile file;

    public BlockCoord lowerCoord;

    public OperationGenerateSchematic()
    {
    }

    public OperationGenerateSchematic(SchematicFile file, BlockCoord lowerCoord)
    {
        this.file = file;
        this.lowerCoord = lowerCoord;
    }

    @Override
    public void perform(World world)
    {
        file.generate(world, lowerCoord.x, lowerCoord.y, lowerCoord.z);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        NBTTagCompound fileCompound = new NBTTagCompound();
        file.writeToNBT(fileCompound);
        compound.setTag("schematic", fileCompound);

        BlockCoord.writeCoordToNBT("lowerCoord", lowerCoord, compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        try
        {
            file = new SchematicFile(compound.getCompoundTag("schematic"));
        }
        catch (SchematicFile.UnsupportedSchematicFormatException e)
        {
            e.printStackTrace();
            file = new SchematicFile((short) 0, (short) 0, (short) 0);
        }

        lowerCoord = BlockCoord.readCoordFromNBT("lowerCoord", compound);
    }

    @Override
    public void renderPreview(int previewType, World world, int ticks, float partialTicks)
    {
        if (previewType == PREVIEW_TYPE_BOUNDING_BOX)
            OperationGenerateStructure.maybeRenderBoundingBox(lowerCoord, new int[]{file.width, file.length, file.height}, ticks, partialTicks);
    }
}
