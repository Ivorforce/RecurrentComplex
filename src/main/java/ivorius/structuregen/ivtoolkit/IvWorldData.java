/*
 * Copyright (c) 2014, Lukas Tenbrink.
 * http://lukas.axxim.net
 *
 * You are free to:
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes, unless you have a permit by the creator.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package ivorius.structuregen.ivtoolkit;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by lukas on 24.05.14.
 */
public class IvWorldData
{
    public IvBlockCollection blockCollection;
    public List<TileEntity> tileEntities;
    public List<Entity> entities;

    public IvWorldData(IvBlockCollection blockCollection, List<TileEntity> tileEntities, List<Entity> entities)
    {
        this.blockCollection = blockCollection;
        this.tileEntities = tileEntities;
        this.entities = entities;
    }

    public IvWorldData(World world, int x, int y, int z, int width, int height, int length, boolean captureEntities)
    {
        blockCollection = new IvBlockCollection(width, height, length);

        tileEntities = new ArrayList<>();
        for (int xP = 0; xP < width; xP++)
        {
            for (int yP = 0; yP < height; yP++)
            {
                for (int zP = 0; zP < length; zP++)
                {
                    int wX = x + xP;
                    int wY = y + yP;
                    int wZ = z + zP;

                    blockCollection.setBlock(xP, yP, zP, world.getBlock(wX, wY, wZ));
                    blockCollection.setMetadata(xP, yP, zP, (byte) world.getBlockMetadata(wX, wY, wZ));

                    TileEntity tileEntity = world.getTileEntity(wX, wY, wZ);
                    if (tileEntity != null)
                    {
                        tileEntities.add(tileEntity);
                    }
                }
            }
        }

        if (captureEntities)
        {
            entities = world.getEntitiesWithinAABBExcludingEntity(null, AxisAlignedBB.getAABBPool().getAABB(x, y, z, x + width, y + height, z + length));
            Iterator<Entity> entityIterator = entities.iterator();
            while (entityIterator.hasNext())
            {
                Entity entity = entityIterator.next();

                if (entity instanceof EntityPlayer)
                {
                    entityIterator.remove();
                }
            }
        }
        else
        {
            entities = Collections.emptyList();
        }
    }

    public IvWorldData(NBTTagCompound compound, World world)
    {
        blockCollection = new IvBlockCollection(compound.getCompoundTag("blockCollection"));

        NBTTagList teList = compound.getTagList("tileEntities", Constants.NBT.TAG_COMPOUND);
        tileEntities = new ArrayList<>(teList.tagCount());
        for (int i = 0; i < teList.tagCount(); i++)
        {
            TileEntity tileEntity = TileEntity.createAndLoadEntity(teList.getCompoundTagAt(i));

            tileEntities.add(tileEntity);
        }

        NBTTagList entityList = compound.getTagList("entities", Constants.NBT.TAG_COMPOUND);
        entities = new ArrayList<>(entityList.tagCount());
        for (int i = 0; i < entityList.tagCount(); i++)
        {
            Entity entity = EntityList.createEntityFromNBT(entityList.getCompoundTagAt(i), world);

            entities.add(entity);
        }
    }

    public NBTTagCompound createTagCompound(int referenceX, int referenceY, int referenceZ)
    {
        NBTTagCompound compound = new NBTTagCompound();

        compound.setTag("blockCollection", blockCollection.createTagCompound());

        NBTTagList teList = new NBTTagList();
        for (TileEntity tileEntity : tileEntities)
        {
            NBTTagCompound teCompound = new NBTTagCompound();
            tileEntity.xCoord -= referenceX;
            tileEntity.yCoord -= referenceY;
            tileEntity.zCoord -= referenceZ;
            tileEntity.writeToNBT(teCompound);
            tileEntity.xCoord += referenceX;
            tileEntity.yCoord += referenceY;
            tileEntity.zCoord += referenceZ;

            teList.appendTag(teCompound);
        }
        compound.setTag("tileEntities", teList);

        NBTTagList entityList = new NBTTagList();
        for (Entity entity : entities)
        {
            NBTTagCompound teCompound = new NBTTagCompound();
            entity.posX -= referenceX;
            entity.posY -= referenceY;
            entity.posZ -= referenceZ;
            entity.writeToNBTOptional(teCompound);
            entity.posX += referenceX;
            entity.posY += referenceY;
            entity.posZ += referenceZ;

            entityList.appendTag(teCompound);
        }
        compound.setTag("entities", entityList);

        return compound;
    }
}
