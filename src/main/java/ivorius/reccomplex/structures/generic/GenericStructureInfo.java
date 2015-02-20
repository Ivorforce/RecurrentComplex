/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic;

import com.google.gson.*;
import cpw.mods.fml.common.Loader;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.blocks.GeneratingTileEntity;
import ivorius.reccomplex.blocks.RCBlocks;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.json.NbtToJson;
import ivorius.reccomplex.structures.generic.blocktransformers.*;
import ivorius.reccomplex.structures.generic.gentypes.StructureGenerationInfo;
import ivorius.reccomplex.utils.RCAccessorEntity;
import ivorius.reccomplex.structures.MCRegistrySpecial;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.StructureInfo;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.structures.generic.gentypes.MazeGenerationInfo;
import ivorius.reccomplex.structures.generic.gentypes.NaturalGenerationInfo;
import ivorius.reccomplex.structures.generic.gentypes.VanillaStructureSpawnInfo;
import ivorius.reccomplex.worldgen.inventory.InventoryGenerationHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by lukas on 24.05.14.
 */
public class GenericStructureInfo implements StructureInfo, Cloneable
{
    public static final int LATEST_VERSION = 3;
    public static final int MAX_GENERATING_LAYERS = 30;

    public NBTTagCompound worldDataCompound;

    public boolean rotatable;
    public boolean mirrorable;

    public final List<StructureGenerationInfo> generationInfos = new ArrayList<>();

    public final List<BlockTransformer> blockTransformers = new ArrayList<>();

    public final List<String> dependencies = new ArrayList<>();

    public JsonObject customData;

    public static GenericStructureInfo createDefaultStructure()
    {
        GenericStructureInfo genericStructureInfo = new GenericStructureInfo();
        genericStructureInfo.rotatable = false;
        genericStructureInfo.mirrorable = false;

        genericStructureInfo.blockTransformers.add(new BlockTransformerNaturalAir(RCBlocks.negativeSpace, 1));
        genericStructureInfo.blockTransformers.add(new BlockTransformerNegativeSpace(RCBlocks.negativeSpace, 0));
        genericStructureInfo.blockTransformers.add(new BlockTransformerNatural(RCBlocks.naturalFloor, 0));
        genericStructureInfo.blockTransformers.add(new BlockTransformerReplace(RCBlocks.naturalFloor, 1, Blocks.air, new byte[]{0}));

        genericStructureInfo.generationInfos.add(new NaturalGenerationInfo());

        return genericStructureInfo;
    }

    private static boolean isBiomeAllTypes(BiomeGenBase biomeGenBase, List<BiomeDictionary.Type> types)
    {
        for (BiomeDictionary.Type type : types)
        {
            if (!BiomeDictionary.isBiomeOfType(biomeGenBase, type))
                return false;
        }

        return true;
    }

    @Override
    public int[] structureBoundingBox()
    {
        if (worldDataCompound == null)
            return new int[]{0, 0, 0};

        NBTTagCompound compound = worldDataCompound.getCompoundTag("blockCollection");
        return new int[]{compound.getInteger("width"), compound.getInteger("height"), compound.getInteger("length")};
    }

    @Override
    public boolean isRotatable()
    {
        return rotatable;
    }

    @Override
    public boolean isMirrorable()
    {
        return mirrorable;
    }


    @Override
    public void generate(StructureSpawnContext context)
    {
        World world = context.world;
        Random random = context.random;

        IvWorldData worldData = new IvWorldData(worldDataCompound, world, MCRegistrySpecial.INSTANCE);

        IvBlockCollection blockCollection = worldData.blockCollection;
        int[] areaSize = new int[]{blockCollection.width, blockCollection.height, blockCollection.length};
        BlockCoord origin = context.lowerCoord();

        List<GeneratingTileEntity> generatingTileEntities = new ArrayList<>();
        Map<BlockCoord, TileEntity> tileEntities = new HashMap<>();
        for (TileEntity tileEntity : worldData.tileEntities)
        {
            tileEntities.put(new BlockCoord(tileEntity), tileEntity);
        }

        if (!context.generateAsSource)
        {
            for (BlockTransformer transformer : blockTransformers)
            {
                if (transformer.generatesInPhase(BlockTransformer.Phase.BEFORE))
                    transformer.transform(BlockTransformer.Phase.BEFORE, context, worldData, blockTransformers);
            }
        }

        for (int pass = 0; pass < 2; pass++)
        {
            for (BlockCoord sourceCoord : blockCollection)
            {
                Block block = blockCollection.getBlock(sourceCoord);
                int meta = blockCollection.getMetadata(sourceCoord);

                BlockCoord worldPos = context.transform.apply(sourceCoord, areaSize).add(origin);

                if (pass == getPass(block, meta) && (context.generateAsSource || transformer(block, meta) == null))
                {
                    if (context.setBlock(worldPos, block, meta))
                    {
                        TileEntity tileEntity = tileEntities.get(sourceCoord);
                        if (tileEntity != null)
                        {
                            world.setBlockMetadataWithNotify(worldPos.x, worldPos.y, worldPos.z, meta, 2); // TODO Figure out why some blocks (chests, furnace) need this

                            IvWorldData.setTileEntityPosForGeneration(tileEntity, worldPos);
                            world.setTileEntity(worldPos.x, worldPos.y, worldPos.z, tileEntity);
                            tileEntity.updateContainingBlockInfo();

                            if (!context.generateAsSource)
                            {
                                if (tileEntity instanceof IInventory)
                                {
                                    IInventory inventory = (IInventory) tileEntity;
                                    InventoryGenerationHandler.generateAllTags(inventory, random);
                                }

                                if (tileEntity instanceof GeneratingTileEntity)
                                {
                                    generatingTileEntities.add((GeneratingTileEntity) tileEntity);
                                }
                            }
                        }
                        context.transform.rotateBlock(world, worldPos, block);
                    }
                }
            }
        }

        if (!context.generateAsSource)
        {
            for (BlockTransformer transformer : blockTransformers)
            {
                if (transformer.generatesInPhase(BlockTransformer.Phase.AFTER))
                    transformer.transform(BlockTransformer.Phase.AFTER, context, worldData, blockTransformers);
            }
        }

        List<Entity> entities = worldData.entities;
        for (Entity entity : entities)
        {
            RCAccessorEntity.setEntityUniqueID(entity, UUID.randomUUID());

            IvWorldData.transformEntityPosForGeneration(entity, context.transform, areaSize);
            IvWorldData.moveEntityForGeneration(entity, origin);

            if (context.boundingBox.isVecInside(MathHelper.floor_double(entity.posX), MathHelper.floor_double(entity.posY), MathHelper.floor_double(entity.posZ)))
                world.spawnEntityInWorld(entity);
        }

        if (context.generationLayer < MAX_GENERATING_LAYERS)
        {
            for (GeneratingTileEntity generatingTileEntity : generatingTileEntities)
            {
                generatingTileEntity.generate(world, random, context.transform, context.generationLayer + 1);
            }
        }
        else
        {
            RecurrentComplex.logger.warn("Structure generated with over " + MAX_GENERATING_LAYERS + " layers; most likely infinite loop!");
        }
    }

    @Override
    public <I> List<I> generationInfos(Class<I> clazz)
    {
        List<I> list = new ArrayList<>();
        for (StructureGenerationInfo info : generationInfos)
        {
            if (clazz.isAssignableFrom(info.getClass()))
                list.add((I) info);
        }

        return list;
    }

    private int getPass(Block block, int metadata)
    {
        return (block.isNormalCube() || block.getMaterial() == Material.air) ? 0 : 1;
    }

    private BlockTransformer transformer(Block block, int metadata)
    {
        for (BlockTransformer transformer : blockTransformers)
        {
            if (transformer.skipGeneration(block, metadata))
            {
                return transformer;
            }
        }

        return null;
    }

    @Override
    public GenericStructureInfo copyAsGenericStructureInfo()
    {
        return (GenericStructureInfo) clone();
    }

    @Override
    public boolean areDependenciesResolved()
    {
        for (String mod : dependencies)
        {
            if (!Loader.isModLoaded(mod))
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public Object clone()
    {
        GenericStructureInfo genericStructureInfo = StructureRegistry.createStructureFromJSON(StructureRegistry.createJSONFromStructure(this));
        genericStructureInfo.worldDataCompound = (NBTTagCompound) worldDataCompound.copy();

        return genericStructureInfo;
    }

    public static class Serializer implements JsonDeserializer<GenericStructureInfo>, JsonSerializer<GenericStructureInfo>
    {
        public GenericStructureInfo deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonobject = JsonUtils.getJsonElementAsJsonObject(jsonElement, "status");
            GenericStructureInfo structureInfo = new GenericStructureInfo();

            Integer version;
            if (jsonobject.has("version"))
            {
                version = JsonUtils.getJsonObjectIntegerFieldValue(jsonobject, "version");
            }
            else
            {
                version = LATEST_VERSION;
                RecurrentComplex.logger.warn("Structure JSON missing 'version', using latest (" + LATEST_VERSION + ")");
            }

            if (jsonobject.has("generationInfos"))
                Collections.addAll(structureInfo.generationInfos, context.<StructureGenerationInfo[]>deserialize(jsonobject.get("generationInfos"), StructureGenerationInfo[].class));

            if (version == 1)
                structureInfo.generationInfos.add(NaturalGenerationInfo.deserializeFromVersion1(jsonobject, context));

            {
                // Legacy version 2
                if (jsonobject.has("naturalGenerationInfo"))
                    structureInfo.generationInfos.add(NaturalGenerationInfo.getGson().fromJson(jsonobject.get("naturalGenerationInfo"), NaturalGenerationInfo.class));

                if (jsonobject.has("mazeGenerationInfo"))
                    structureInfo.generationInfos.add(MazeGenerationInfo.getGson().fromJson(jsonobject.get("mazeGenerationInfo"), MazeGenerationInfo.class));
            }

            if (jsonobject.has("blockTransformers"))
                Collections.addAll(structureInfo.blockTransformers, context.<BlockTransformer[]>deserialize(jsonobject.get("blockTransformers"), BlockTransformer[].class));

            structureInfo.rotatable = JsonUtils.getJsonObjectBooleanFieldValueOrDefault(jsonobject, "rotatable", false);
            structureInfo.mirrorable = JsonUtils.getJsonObjectBooleanFieldValueOrDefault(jsonobject, "mirrorable", false);

            if (jsonobject.has("dependencies"))
                Collections.addAll(structureInfo.dependencies, context.<String[]>deserialize(jsonobject.get("dependencies"), String[].class));

            if (jsonobject.has("worldData"))
            {
                structureInfo.worldDataCompound = context.deserialize(jsonobject.get("worldData"), NBTTagCompound.class);
            }
            else if (jsonobject.has("worldDataBase64"))
            {
                structureInfo.worldDataCompound = NbtToJson.getNBTFromBase64(JsonUtils.getJsonObjectStringFieldValue(jsonobject, "worldDataBase64"));
            }
            else
            {
                // And else it is taken out for packet size
            }

            structureInfo.customData = JsonUtils.getJsonObjectFieldOrDefault(jsonobject, "customData", new JsonObject());

            return structureInfo;
        }

        public JsonElement serialize(GenericStructureInfo structureInfo, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonobject = new JsonObject();

            jsonobject.addProperty("version", LATEST_VERSION);

            if (structureInfo.generationInfos.size() > 0)
                jsonobject.add("generationInfos", context.serialize(structureInfo.generationInfos));

            if (structureInfo.blockTransformers.size() > 0)
                jsonobject.add("blockTransformers", context.serialize(structureInfo.blockTransformers));

            jsonobject.addProperty("rotatable", structureInfo.rotatable);
            jsonobject.addProperty("mirrorable", structureInfo.mirrorable);

            if (structureInfo.dependencies.size() > 0)
                jsonobject.add("dependencies", context.serialize(structureInfo.dependencies));

            if (!RecurrentComplex.USE_ZIP_FOR_STRUCTURE_FILES && structureInfo.worldDataCompound != null)
            {
                if (RecurrentComplex.USE_JSON_FOR_NBT)
                {
                    jsonobject.add("worldData", context.serialize(structureInfo.worldDataCompound));
                }
                else
                {
                    jsonobject.addProperty("worldDataBase64", NbtToJson.getBase64FromNBT(structureInfo.worldDataCompound));
                }
            }

            jsonobject.add("customData", structureInfo.customData);

            return jsonobject;
        }
    }
}
