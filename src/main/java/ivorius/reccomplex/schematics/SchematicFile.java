/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.schematics;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.tools.IvWorldData;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
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
    public final List<NBTTagCompound> entityCompounds = new ArrayList<>();
    public final List<NBTTagCompound> tileEntityCompounds = new ArrayList<>();
    public short width, height, length;
    public Short weOriginX, weOriginY, weOriginZ;
    public Block[] blocks;
    public byte[] metadatas;

    public SchematicFile()
    {
    }

    public SchematicFile(short width, short height, short length)
    {
        this.width = width;
        this.height = height;
        this.length = length;
        this.blocks = new Block[width * height * length];
        this.metadatas = new byte[width * height * length];
    }

    public SchematicFile(NBTTagCompound tagCompound) throws UnsupportedSchematicFormatException
    {
        String materials = tagCompound.getString("Materials");
        if (!(materials.equals("Alpha")))
            throw new UnsupportedSchematicFormatException(materials);

        width = tagCompound.getShort("Width");
        height = tagCompound.getShort("Height");
        length = tagCompound.getShort("Length");

        if (tagCompound.hasKey("WEOriginX", Constants.NBT.TAG_SHORT))
            weOriginX = tagCompound.getShort("WEOriginX");
        if (tagCompound.hasKey("WEOriginY", Constants.NBT.TAG_SHORT))
            weOriginY = tagCompound.getShort("WEOriginY");
        if (tagCompound.hasKey("WEOriginZ", Constants.NBT.TAG_SHORT))
            weOriginZ = tagCompound.getShort("WEOriginZ");

        metadatas = tagCompound.getByteArray("Data");
        byte[] blockIDs = tagCompound.getByteArray("Blocks");
        byte[] addBlocks = tagCompound.getByteArray("AddBlocks");

        this.blocks = new Block[blockIDs.length];
        for (int i = 0; i < blockIDs.length; i++)
        {
            int blockID = blockIDs[i] & 0xff;

            if (addBlocks.length >= (blockIDs.length + 1) / 2)
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

    public int getBlockIndex(int x, int y, int z)
    {
        return x + (y * length + z) * width;
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
                int index = getBlockIndex(srcCoord.x, srcCoord.y, srcCoord.z);
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

    public void writeToNBT(NBTTagCompound tagCompound)
    {
        tagCompound.setString("Materials", "Alpha");

        tagCompound.setShort("Width", width);
        tagCompound.setShort("Height", height);
        tagCompound.setShort("Length", length);

        if (weOriginX != null)
            tagCompound.setShort("WEOriginX", weOriginX);
        if (weOriginY != null)
            tagCompound.setShort("WEOriginY", weOriginY);
        if (weOriginZ != null)
            tagCompound.setShort("WEOriginZ", weOriginZ);

        tagCompound.setByteArray("Data", metadatas);

        byte[] blockIDs = new byte[blocks.length];
        byte[] addBlocks = new byte[(blocks.length + 1) / 2];
        for (int i = 0; i < blocks.length; i++)
        {
            int blockID = getBlockID(blocks[i]);

            blockIDs[i] = (byte) (blockID & 0xff);
            boolean lowerNybble = (i & 1) == 0;
            addBlocks[i >> 1] |= lowerNybble ? (byte) ((blockID >> 8) & 0x0F) : (byte) ((blockID >> 4) & 0xF0);
        }
        tagCompound.setByteArray("Blocks", blockIDs);
        tagCompound.setByteArray("AddBlocks", addBlocks);

        NBTTagList entities = new NBTTagList();
        for (NBTTagCompound entityCompound : entityCompounds)
            entities.appendTag(entityCompound);
        tagCompound.setTag("Entities", entities);

        NBTTagList tileEntitites = new NBTTagList();
        for (NBTTagCompound tileEntityCompound : tileEntityCompounds)
            entities.appendTag(tileEntityCompound);
        tagCompound.setTag("TileEntities", tileEntitites);
    }

    private int getBlockID(Block block)
    {
        return Block.blockRegistry.getIDForObject(block);
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
