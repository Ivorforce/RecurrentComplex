/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.genericStructures;

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
import ivorius.reccomplex.utils.RCAccessHelperEntity;
import ivorius.reccomplex.worldgen.MCRegistrySpecial;
import ivorius.reccomplex.worldgen.StructureRegistry;
import ivorius.reccomplex.worldgen.StructureInfo;
import ivorius.reccomplex.worldgen.StructureSpawnContext;
import ivorius.reccomplex.worldgen.blockTransformers.*;
import ivorius.reccomplex.worldgen.genericStructures.gentypes.MazeGenerationInfo;
import ivorius.reccomplex.worldgen.genericStructures.gentypes.NaturalGenerationInfo;
import ivorius.reccomplex.worldgen.genericStructures.gentypes.VanillaStructureSpawnInfo;
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
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by lukas on 24.05.14.
 */
public class GenericStructureInfo implements StructureInfo, Cloneable
{
    public static final int LATEST_VERSION = 2;
    public static final int MAX_GENERATING_LAYERS = 30;

    public NBTTagCompound worldDataCompound;

    public boolean rotatable;
    public boolean mirrorable;

    public NaturalGenerationInfo naturalGenerationInfo;
    public MazeGenerationInfo mazeGenerationInfo;
    public VanillaStructureSpawnInfo vanillaStructureSpawnInfo;

    public List<BlockTransformer> blockTransformers = new ArrayList<>();

    public List<String> dependencies = new ArrayList<>();

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

        genericStructureInfo.naturalGenerationInfo = new NaturalGenerationInfo("decoration", new GenerationYSelector(GenerationYSelector.SelectionMode.SURFACE, 0, 0));
        genericStructureInfo.naturalGenerationInfo.biomeWeights.addAll(BiomeGenerationInfo.overworldBiomeGenerationList());

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
    public int generationY(World world, Random random, int x, int z)
    {
        return naturalGenerationInfo != null ? naturalGenerationInfo.ySelector.generationY(world, random, x, z, structureBoundingBox()) : world.getHeightValue(x, z);
    }

    @Override
    public int[] structureBoundingBox()
    {
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
                    transformer.transform(world, random, BlockTransformer.Phase.BEFORE, origin, areaSize, context.transform, worldData, blockTransformers);
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
                    transformer.transform(world, random, BlockTransformer.Phase.AFTER, origin, areaSize, context.transform, worldData, blockTransformers);
            }
        }

        List<Entity> entities = worldData.entities;
        for (Entity entity : entities)
        {
            RCAccessHelperEntity.setEntityUniqueID(entity, UUID.randomUUID());

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
    public int generationWeight(BiomeGenBase biome, int dimensionID)
    {
        return generationWeightInBiome(biome) * generationWeightInDimension(dimensionID);
    }

    protected int generationWeightInDimension(int dimensionID)
    {
        if (naturalGenerationInfo != null)
        {
            for (DimensionGenerationInfo generationInfo : naturalGenerationInfo.dimensionWeights)
            {
                if (generationInfo.matches(dimensionID))
                    return generationInfo.getActiveGenerationWeight();
            }
        }

        return 0;
    }

    protected int generationWeightInBiome(BiomeGenBase biome)
    {
        if (naturalGenerationInfo != null)
        {
            for (BiomeGenerationInfo generationInfo : naturalGenerationInfo.biomeWeights)
            {
                if (generationInfo.matches(biome))
                    return generationInfo.getActiveGenerationWeight();
            }
        }

        return 0;
    }

    @Override
    public String generationCategory()
    {
        return naturalGenerationInfo != null ? naturalGenerationInfo.generationCategory : null;
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
    public String mazeID()
    {
        return mazeGenerationInfo != null ? mazeGenerationInfo.mazeID : null;
    }

    @Override
    public SavedMazeComponent mazeComponent()
    {
        return mazeGenerationInfo != null ? mazeGenerationInfo.mazeComponent : null;
    }

    @Override
    public VanillaStructureSpawnInfo vanillaStructureSpawnInfo()
    {
        return vanillaStructureSpawnInfo;
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

            if (jsonobject.has("blockTransformers"))
            {
                JsonArray blockTransformers = JsonUtils.getJsonObjectJsonArrayField(jsonobject, "blockTransformers");

                for (JsonElement transformerElement : blockTransformers)
                {
                    BlockTransformer transformer = context.deserialize(transformerElement, BlockTransformer.class);
                    structureInfo.blockTransformers.add(transformer);
                }
            }

            if (version == 1)
                structureInfo.naturalGenerationInfo = NaturalGenerationInfo.deserializeFromVersion1(jsonobject, context);
            else
            {
                if (jsonobject.has("naturalGenerationInfo"))
                    structureInfo.naturalGenerationInfo = context.deserialize(jsonobject.get("naturalGenerationInfo"), NaturalGenerationInfo.class);
            }

            if (jsonobject.has("mazeGenerationInfo"))
                structureInfo.mazeGenerationInfo = context.deserialize(jsonobject.get("mazeGenerationInfo"), MazeGenerationInfo.class);

            if (jsonobject.has("vanillaStructureGenerationInfo"))
                structureInfo.vanillaStructureSpawnInfo = context.deserialize(jsonobject.get("vanillaStructureGenerationInfo"), VanillaStructureSpawnInfo.class);

            structureInfo.rotatable = JsonUtils.getJsonObjectBooleanFieldValueOrDefault(jsonobject, "rotatable", false);
            structureInfo.mirrorable = JsonUtils.getJsonObjectBooleanFieldValueOrDefault(jsonobject, "mirrorable", false);

            if (jsonobject.has("dependencies"))
            {
                JsonArray dependencyArray = JsonUtils.getJsonObjectJsonArrayField(jsonobject, "dependencies");
                for (JsonElement element : dependencyArray)
                {
                    structureInfo.dependencies.add(JsonUtils.getJsonElementStringValue(element, "dependency"));
                }
            }

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

            if (structureInfo.blockTransformers.size() > 0)
            {
                JsonArray blockTransformers = new JsonArray();
                for (BlockTransformer transformer : structureInfo.blockTransformers)
                {
                    blockTransformers.add(context.serialize(transformer, BlockTransformer.class));
                }
                jsonobject.add("blockTransformers", blockTransformers);
            }

            jsonobject.addProperty("rotatable", structureInfo.rotatable);
            jsonobject.addProperty("mirrorable", structureInfo.mirrorable);

            if (structureInfo.naturalGenerationInfo != null)
                jsonobject.add("naturalGenerationInfo", context.serialize(structureInfo.naturalGenerationInfo));
            if (structureInfo.mazeGenerationInfo != null)
                jsonobject.add("mazeGenerationInfo", context.serialize(structureInfo.mazeGenerationInfo));
            if (structureInfo.vanillaStructureSpawnInfo != null)
                jsonobject.add("vanillaStructureGenerationInfo", context.serialize(structureInfo.vanillaStructureSpawnInfo));

            if (structureInfo.dependencies.size() > 0)
            {
                JsonArray dependencyArray = new JsonArray();
                for (String s : structureInfo.dependencies)
                {
                    dependencyArray.add(context.serialize(s));
                }
                jsonobject.add("dependencies", dependencyArray);
            }

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
