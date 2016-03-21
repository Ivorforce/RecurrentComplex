/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen;

import com.google.common.base.Predicate;
import com.google.common.collect.*;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.structures.StructureInfo;
import ivorius.reccomplex.structures.StructureInfos;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.utils.StructureBoundingBoxes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Created by lukas on 01.03.15.
 */
public class StructureGenerationData extends WorldSavedData
{
    private static final String IDENTIFIER = RecurrentComplex.MODID + ":structuredata";

    protected final Set<ChunkCoordIntPair> checkedChunks = new HashSet<>();
    protected final Set<ChunkCoordIntPair> checkedChunksFinal = new HashSet<>();

    protected final Map<UUID, Entry> entryMap = new HashMap<>();
    protected final SetMultimap<ChunkCoordIntPair, Entry> chunkMap = HashMultimap.create();
    protected final SetMultimap<String, Entry> instanceMap = HashMultimap.create();

    public StructureGenerationData(String id)
    {
        super(id);
    }

    public StructureGenerationData()
    {
        this(IDENTIFIER);
    }

    public static StructureGenerationData get(World world)
    {
        StructureGenerationData data = (StructureGenerationData) world.loadItemData(StructureGenerationData.class, IDENTIFIER);
        if (data == null)
        {
            data = new StructureGenerationData();
            world.setItemData(data.mapName, data);
        }
        return data;
    }

    public Set<Entry> getEntriesAt(ChunkCoordIntPair coords, boolean onlyPartial)
    {
        if (onlyPartial)
            return Sets.filter(chunkMap.get(coords), input -> !input.hasBeenGenerated);
        return chunkMap.get(coords);
    }

    public Set<Entry> getEntriesAt(final BlockCoord coords)
    {
        Set<Entry> entries = getEntriesAt(new ChunkCoordIntPair(coords.x >> 4, coords.z >> 4), false);

        return Sets.filter(entries, input -> {
            StructureBoundingBox bb = input.boundingBox();
            return bb != null && bb.isVecInside(coords.x, coords.y, coords.z);
        });
    }

    public Set<Entry> getEntriesAt(final StructureBoundingBox boundingBox)
    {
        ImmutableSet.Builder<Entry> entries = ImmutableSet.builder();
        for (ChunkCoordIntPair chunkCoords : StructureBoundingBoxes.rasterize(boundingBox))
                entries.addAll(Sets.filter(getEntriesAt(chunkCoords, false), input -> {
                    StructureBoundingBox bb = input.boundingBox();
                    return bb != null && bb.intersectsWith(boundingBox);
                }));
        return entries.build();
    }

    public Set<ChunkCoordIntPair> addCompleteEntry(String structureID, BlockCoord lowerCoord, AxisAlignedTransform2D transform)
    {
        return addEntry(new Entry(UUID.randomUUID(), structureID, lowerCoord, transform, true));
    }

    public Set<ChunkCoordIntPair> addGeneratingEntry(String structureID, BlockCoord lowerCoord, AxisAlignedTransform2D transform)
    {
        return addEntry(new Entry(UUID.randomUUID(), structureID, lowerCoord, transform, false));
    }

    public Set<ChunkCoordIntPair> addEntry(Entry entry)
    {
        entryMap.put(entry.getUuid(), entry);

        Set<ChunkCoordIntPair> rasterized = entry.rasterize();
        for (ChunkCoordIntPair coords : rasterized)
            chunkMap.put(coords, entry);

        instanceMap.put(entry.getStructureID(), entry);

        markDirty();

        return Sets.intersection(checkedChunksFinal, rasterized);
    }

    public Entry getEntry(UUID id)
    {
        return entryMap.get(id);
    }

    public Set<Entry> getEntriesByID(String id)
    {
        return instanceMap.get(id);
    }

    public boolean checkChunk(ChunkCoordIntPair coords)
    {
        if (checkedChunks.contains(coords))
            return false;

        checkedChunks.add(coords);
        markDirty();
        return true;
    }

    public boolean checkChunkFinal(ChunkCoordIntPair coords)
    {
        if (checkedChunksFinal.contains(coords))
            return false;

        checkedChunksFinal.add(coords);
        markDirty();
        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        entryMap.clear();

        NBTTagList entries = compound.getTagList("entries", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < entries.tagCount(); i++)
        {
            Entry entry = new Entry();
            entry.readFromNBT(entries.getCompoundTagAt(i));
            addEntry(entry);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        NBTTagList entries = new NBTTagList();
        for (Entry entry : entryMap.values())
        {
            NBTTagCompound entryCompound = new NBTTagCompound();
            entry.writeToNBT(entryCompound);
            entries.appendTag(entryCompound);
        }
        compound.setTag("entries", entries);
    }

    public static class Entry
    {
        @Nonnull
        protected UUID uuid;

        protected String structureID;
        protected BlockCoord lowerCoord;
        protected AxisAlignedTransform2D transform;

        protected NBTTagCompound instanceData;
        protected boolean firstTime = true;

        protected boolean hasBeenGenerated;

        public Entry()
        {
        }

        public Entry(@Nonnull UUID uuid, String structureID, BlockCoord lowerCoord, AxisAlignedTransform2D transform, boolean hasBeenGenerated)
        {
            this.uuid = uuid;
            this.structureID = structureID;
            this.lowerCoord = lowerCoord;
            this.transform = transform;
            this.hasBeenGenerated = hasBeenGenerated;
         }

        @Nonnull
        public UUID getUuid()
        {
            return uuid;
        }

        public void setUuid(@Nonnull UUID uuid)
        {
            this.uuid = uuid;
        }

        public String getStructureID()
        {
            return structureID;
        }

        public BlockCoord getLowerCoord()
        {
            return lowerCoord;
        }

        public AxisAlignedTransform2D getTransform()
        {
            return transform;
        }

        public boolean isHasBeenGenerated()
        {
            return hasBeenGenerated;
        }

        public void readFromNBT(NBTTagCompound compound)
        {
            uuid = new UUID(compound.getLong("UUIDMS"), compound.getLong("UUIDLS"));

            structureID = compound.getString("structureID");

            transform = AxisAlignedTransform2D.from(compound.getInteger("rotation"), compound.getBoolean("mirrorX"));

            lowerCoord = BlockCoord.readCoordFromNBT("lowerCoord", compound);

            if (compound.hasKey("instanceData", Constants.NBT.TAG_COMPOUND))
                instanceData = compound.getCompoundTag("instanceData");
            firstTime = compound.getBoolean("firstTime");
            hasBeenGenerated = compound.getBoolean("hasBeenGenerated");
        }

        public void writeToNBT(NBTTagCompound compound)
        {
            compound.setLong("UUIDMS", uuid.getMostSignificantBits());
            compound.setLong("UUIDLS", uuid.getLeastSignificantBits());

            compound.setString("structureID", structureID);

            compound.setInteger("rotation", transform.getRotation());
            compound.setBoolean("mirrorX", transform.isMirrorX());

            BlockCoord.writeCoordToNBT("lowerCoord", lowerCoord, compound);

            if (instanceData != null)
                compound.setTag("instanceData", instanceData);
            compound.setBoolean("firstTime", firstTime);
            compound.setBoolean("hasBeenGenerated", hasBeenGenerated);
        }

        public Set<ChunkCoordIntPair> rasterize()
        {
            return StructureBoundingBoxes.rasterize(boundingBox());
        }

        public StructureBoundingBox boundingBox()
        {
            StructureInfo structureInfo = StructureRegistry.INSTANCE.getStructure(structureID);

            return structureInfo != null
                    ? StructureInfos.structureBoundingBox(lowerCoord, StructureInfos.structureSize(structureInfo, transform))
                    : null;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Entry entry = (Entry) o;

            if (!uuid.equals(entry.uuid)) return false;

            return true;
        }

        @Override
        public int hashCode()
        {
            return uuid.hashCode();
        }
    }
}
