/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.schematics;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.blocks.GeneratingTileEntity;
import ivorius.reccomplex.worldgen.inventory.InventoryGenerationHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.*;

/**
 * Created by lukas on 29.09.14.
 */
public class SchematicFile
{
    public short width, height, length;
    public short weOriginX, weOriginY, weOriginZ;
    public Block[] blocks;
    public byte[] metadatas;
    public List<NBTTagCompound> entityCompounds = new ArrayList<>();
    public List<NBTTagCompound> tileEntityCompounds = new ArrayList<>();

    public SchematicFile(NBTTagCompound tagCompound) throws UnsupportedSchematicFormatException
    {
        String materials = tagCompound.getString("Materials");
        if (!(materials.equals("Alpha")))
            throw new UnsupportedSchematicFormatException(materials);

        width = tagCompound.getShort("Width");
        height = tagCompound.getShort("Height");
        length = tagCompound.getShort("Length");

        weOriginX = tagCompound.getShort("WEOriginX");
        weOriginY = tagCompound.getShort("WEOriginY");
        weOriginZ = tagCompound.getShort("WEOriginZ");

        metadatas = tagCompound.getByteArray("Data");
        byte[] blockIDs = tagCompound.getByteArray("Blocks");
        byte[] addBlocks = tagCompound.getByteArray("AddBlocks");

        this.blocks = new Block[blockIDs.length];
        for (int i = 0; i < blockIDs.length; i++)
        {
            int blockID = blockIDs[i] & 0xff;

            if (addBlocks.length == blockIDs.length / 2)
            {
                boolean lowerNybble = (i & 1) == 0;
                blockID |= lowerNybble ? ((addBlocks[i >> 1] & 0x0F) << 8) : ((addBlocks[i >> 1] & 0xF0) << 4);
            }

            this.blocks[i] = Block.getBlockById(blockID);
        }

        NBTTagList entities = tagCompound.getTagList("Entities", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < entities.tagCount(); i++)
            entityCompounds.add(entities.getCompoundTagAt(i));

        NBTTagList tileEntities = tagCompound.getTagList("TileEntities", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tileEntities.tagCount(); i++)
            tileEntityCompounds.add(tileEntities.getCompoundTagAt(i));
    }

    public void generate(World world, int x, int y, int z)
    {
        Map<BlockCoord, TileEntity> tileEntities = new HashMap<>();
        for (NBTTagCompound tileTagCompound : tileEntityCompounds)
        {
            TileEntity tileEntity = TileEntity.createAndLoadEntity(tileTagCompound);
            if (tileEntity != null)
                tileEntities.put(new BlockCoord(tileEntity), tileEntity);
        }

        BlockArea blockArea = BlockArea.areaFromSize(new BlockCoord(0, 0, 0), new int[]{width, height, length});
        for (int pass = 0; pass < 2; pass++)
        {
            for (BlockCoord srcCoord : blockArea)
            {
                int index = srcCoord.x + (srcCoord.y * length + srcCoord.z) * width;
                Block block = blocks[index];
                byte meta = metadatas[index];

                if (block != null && getPass(block, meta) == pass)
                {
                    BlockCoord worldPos = new BlockCoord(x + srcCoord.x, y + srcCoord.y, z + srcCoord.z);
                    world.setBlock(worldPos.x, worldPos.y, worldPos.z, block, meta, 3);

                    TileEntity tileEntity = tileEntities.get(srcCoord);
                    if (tileEntity != null)
                    {
                        world.setBlockMetadataWithNotify(worldPos.x, worldPos.y, worldPos.z, meta, 2); // TODO Figure out why some blocks (chests, furnace) need this

                        IvWorldData.setTileEntityPosForGeneration(tileEntity, worldPos);
                        world.setTileEntity(worldPos.x, worldPos.y, worldPos.z, tileEntity);
                        tileEntity.updateContainingBlockInfo();
                    }
                }
            }
        }

        for (NBTTagCompound entityCompound : entityCompounds)
        {
            Entity entity = EntityList.createEntityFromNBT(entityCompound, world);
            if (entity != null)
            {
                entity.entityUniqueID = UUID.randomUUID();

                IvWorldData.moveEntityForGeneration(entity, new BlockCoord(x, y, z));

                world.spawnEntityInWorld(entity);
            }
        }
    }

    private int getPass(Block block, int metadata)
    {
        return (block.isNormalCube() || block.getMaterial() == Material.air) ? 0 : 1;
    }

    public static class UnsupportedSchematicFormatException extends Exception
    {
        public final String format;

        public UnsupportedSchematicFormatException(String format)
        {
            this.format = format;
        }
    }
}
