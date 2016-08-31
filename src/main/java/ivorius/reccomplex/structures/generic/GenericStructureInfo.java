/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic;

import com.google.gson.*;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.tools.*;
import ivorius.ivtoolkit.transform.Mover;
import ivorius.ivtoolkit.transform.PosTransformer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityList;
import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.blocks.GeneratingTileEntity;
import ivorius.reccomplex.blocks.RCBlocks;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.json.NbtToJson;
import ivorius.reccomplex.structures.*;
import ivorius.reccomplex.structures.generic.gentypes.MazeGenerationInfo;
import ivorius.reccomplex.structures.generic.gentypes.NaturalGenerationInfo;
import ivorius.reccomplex.structures.generic.gentypes.StructureGenerationInfo;
import ivorius.reccomplex.structures.generic.matchers.BlockMatcher;
import ivorius.reccomplex.structures.generic.matchers.DependencyMatcher;
import ivorius.reccomplex.structures.generic.transformers.*;
import ivorius.reccomplex.utils.*;
import ivorius.reccomplex.worldgen.inventory.InventoryGenerationHandler;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lukas on 24.05.14.
 */
public class GenericStructureInfo implements StructureInfo<GenericStructureInfo.InstanceData>, Cloneable
{
    public static final int LATEST_VERSION = 3;
    public static final int MAX_GENERATING_LAYERS = 30;

    public final List<StructureGenerationInfo> generationInfos = new ArrayList<>();
    public final List<Transformer> transformers = new ArrayList<>();
    public final DependencyMatcher dependencies = new DependencyMatcher("");

    public NBTTagCompound worldDataCompound;

    public boolean rotatable;
    public boolean mirrorable;

    public Metadata metadata = new Metadata();

    public JsonObject customData;

    public static GenericStructureInfo createDefaultStructure()
    {
        GenericStructureInfo genericStructureInfo = new GenericStructureInfo();
        genericStructureInfo.rotatable = true;
        genericStructureInfo.mirrorable = true;

        genericStructureInfo.transformers.add(new TransformerNaturalAir(Transformer.randomID(TransformerNaturalAir.class), BlockMatcher.of(RecurrentComplex.specialRegistry, RCBlocks.genericSpace, 1), TransformerNaturalAir.DEFAULT_NATURAL_EXPANSION_DISTANCE, TransformerNaturalAir.DEFAULT_NATURAL_EXPANSION_RANDOMIZATION));
        genericStructureInfo.transformers.add(new TransformerNegativeSpace(Transformer.randomID(TransformerNegativeSpace.class), BlockMatcher.of(RecurrentComplex.specialRegistry, RCBlocks.genericSpace, 0)));
        genericStructureInfo.transformers.add(new TransformerNatural(Transformer.randomID(TransformerNatural.class), BlockMatcher.of(RecurrentComplex.specialRegistry, RCBlocks.genericSolid, 0), TransformerNatural.DEFAULT_NATURAL_EXPANSION_DISTANCE, TransformerNatural.DEFAULT_NATURAL_EXPANSION_RANDOMIZATION));
        genericStructureInfo.transformers.add(new TransformerReplace(Transformer.randomID(TransformerReplace.class), BlockMatcher.of(RecurrentComplex.specialRegistry, RCBlocks.genericSolid, 1)).replaceWith(new WeightedBlockState(null, Blocks.AIR.getDefaultState(), "")));

        genericStructureInfo.generationInfos.add(new NaturalGenerationInfo());

        return genericStructureInfo;
    }

    private static boolean isBiomeAllTypes(Biome Biome, List<BiomeDictionary.Type> types)
    {
        for (BiomeDictionary.Type type : types)
        {
            if (!BiomeDictionary.isBiomeOfType(Biome, type))
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
    public void generate(final StructureSpawnContext context, InstanceData instanceData)
    {
        WorldServer world = context.world;
        Random random = context.random;
        IvWorldData worldData = constructWorldData();

        // The world initializes the block event array after it generates the world - in the constructor
        // This hackily sets the field to a temporary value. Yay.
        RCAccessorWorldServer.ensureBlockEventArray(world); // Hax

        IvBlockCollection blockCollection = worldData.blockCollection;
        int[] areaSize = new int[]{blockCollection.width, blockCollection.height, blockCollection.length};
        BlockPos origin = context.lowerCoord();

        Map<BlockPos, TileEntity> origTileEntities = new HashMap<>();
        Map<BlockPos, NBTTagCompound> tileEntityCompounds = new HashMap<>();
        for (NBTTagCompound tileEntityCompound : worldData.tileEntities)
        {
            BlockPos key = new BlockPos(tileEntityCompound.getInteger("x"), tileEntityCompound.getInteger("y"), tileEntityCompound.getInteger("z"));

            TileEntity origTileEntity = RecurrentComplex.specialRegistry.loadTileEntity(world, tileEntityCompound);
            Mover.setTileEntityPos(origTileEntity, context.transform.apply(key, areaSize).add(origin));

            origTileEntities.put(key, origTileEntity);
            tileEntityCompounds.put(key, tileEntityCompound);
        }

        List<Pair<Transformer, NBTStorable>> transformers = new ArrayList<>();
        for (int index = 0; index < this.transformers.size(); index++)
        {
            Transformer transformer = this.transformers.get(index);
            transformers.add(Pair.of(transformer, instanceData.findTransformerData(transformer, index)));
        }

        if (!context.generateAsSource)
        {
            for (Pair<Transformer, NBTStorable> pair : transformers)
            {
                Transformer transformer = pair.getLeft();
                NBTStorable transformerData = pair.getRight();
                if (transformer.generatesInPhase(transformerData, Transformer.Phase.BEFORE))
                    transformer.transform(transformerData, Transformer.Phase.BEFORE, context, worldData, transformers);
            }
        }

        for (int pass = 0; pass < 2; pass++)
        {
            for (BlockPos sourceCoord : blockCollection.area())
            {
                IBlockState state = PosTransformer.transformBlockState(blockCollection.getBlockState(sourceCoord), context.transform);

                BlockPos worldPos = context.transform.apply(sourceCoord, areaSize).add(origin);
                if (context.includes(worldPos) && RecurrentComplex.specialRegistry.isSafe(state.getBlock())
                        && pass == getPass(state) && (context.generateAsSource || !skips(transformers, state)))
                {
                    TileEntity origTileEntity = origTileEntities.get(sourceCoord);

                    if (context.generateAsSource || !(origTileEntity instanceof GeneratingTileEntity) || ((GeneratingTileEntity) origTileEntity).shouldPlaceInWorld(context, instanceData.tileEntities.get(sourceCoord)))
                    {
                        if (context.setBlock(worldPos, state, 2) && world.getBlockState(worldPos).getBlock() == state.getBlock())
                        {
                            NBTTagCompound tileEntityCompound = tileEntityCompounds.get(sourceCoord);

                            if (tileEntityCompound != null)
                            {
                                TileEntity tileEntity = world.getTileEntity(worldPos);

                                if (tileEntity != null)
                                {
                                    tileEntity.readFromNBT(tileEntityCompound);
                                    Mover.setTileEntityPos(tileEntity, worldPos);

                                    if (!context.generateAsSource && tileEntity instanceof IInventory)
                                    {
                                        IInventory inventory = (IInventory) tileEntity;
                                        InventoryGenerationHandler.generateAllTags(context.world, inventory, RecurrentComplex.specialRegistry.itemHidingMode(), random);
                                    }
                                }
                            }

                            PosTransformer.transformBlock(world, worldPos, context.transform);
                        }
                    }
                    else
                        context.setBlock(worldPos, Blocks.AIR.getDefaultState(), 2); // Replace with air
                }
            }
        }

        if (!context.generateAsSource)
        {
            for (Pair<Transformer, NBTStorable> pair : transformers)
            {
                Transformer transformer = pair.getLeft();
                NBTStorable transformerData = pair.getRight();
                if (transformer.generatesInPhase(transformerData, Transformer.Phase.AFTER))
                    transformer.transform(transformerData, Transformer.Phase.AFTER, context, worldData, transformers);
            }
        }

        for (NBTTagCompound entityCompound : worldData.entities)
        {
            double[] entityPos = getEntityPos(entityCompound);
            double[] transformedEntityPos = context.transform.apply(entityPos, areaSize);
            if (context.includes(transformedEntityPos[0] + origin.getX(), transformedEntityPos[1] + origin.getY(), transformedEntityPos[2] + origin.getZ()))
            {
                Entity entity = EntityList.createEntityFromNBT(entityCompound, world);

                if (entity != null)
                {
                    PosTransformer.transformEntityPos(entity, context.transform, areaSize);
                    Mover.moveEntity(entity, origin);

                    RCAccessorEntity.setEntityUniqueID(entity, UUID.randomUUID());
                    world.spawnEntityInWorld(entity);
                }
                else
                {
                    RecurrentComplex.logger.error("Error loading entity with ID '" + entityCompound.getString("id") + "'");
                }
            }
        }

        if (!context.generateAsSource && context.generationLayer < MAX_GENERATING_LAYERS)
        {
            origTileEntities.entrySet().stream().filter(entry -> entry.getValue() instanceof GeneratingTileEntity).forEach(entry -> ((GeneratingTileEntity) entry.getValue()).generate(context, instanceData.tileEntities.get(entry.getKey())));
        }
        else
        {
            RecurrentComplex.logger.warn("Structure generated with over " + MAX_GENERATING_LAYERS + " layers; most likely infinite loop!");
        }
    }

    private static double[] getEntityPos(NBTTagCompound compound)
    {
        NBTTagList pos = compound.getTagList("Pos", Constants.NBT.TAG_DOUBLE);
        return new double[]{pos.getDoubleAt(0), pos.getDoubleAt(1), pos.getDoubleAt(2)};
    }

    @Override
    public InstanceData prepareInstanceData(StructurePrepareContext context)
    {
        InstanceData instanceData = new InstanceData();

        if (!context.generateAsSource)
        {
            IvWorldData worldData = constructWorldData();
            IvBlockCollection blockCollection = worldData.blockCollection;

            int[] areaSize = new int[]{blockCollection.width, blockCollection.height, blockCollection.length};
            BlockPos origin = context.lowerCoord();

            transformers.forEach(transformer -> instanceData.transformers.put(transformer.id(), transformer.prepareInstanceData(context)));

            worldData.tileEntities.forEach(teCompound ->
            {
                TileEntity tileEntity = RecurrentComplex.specialRegistry.loadTileEntity(TileEntities.getAnyWorld(), teCompound);
                if (tileEntity instanceof GeneratingTileEntity)
                {
                    BlockPos key = tileEntity.getPos();
                    Mover.setTileEntityPos(tileEntity, context.transform.apply(key, areaSize).add(origin));
                    instanceData.tileEntities.put(key, (NBTStorable) ((GeneratingTileEntity) tileEntity).prepareInstanceData(context));
                }
            });
        }

        return instanceData;
    }

    @Override
    public InstanceData loadInstanceData(StructureLoadContext context, final NBTBase nbt)
    {
        InstanceData instanceData = new InstanceData();
        instanceData.readFromNBT(context, nbt, transformers, constructWorldData());
        return instanceData;
    }

    private boolean skips(List<Pair<Transformer, NBTStorable>> transformers, final IBlockState state)
    {
        return transformers.stream().anyMatch(input -> input.getLeft().skipGeneration(input.getRight(), state));
    }

    public IvWorldData constructWorldData()
    {
        return new IvWorldData(worldDataCompound, RecurrentComplex.specialRegistry.itemHidingMode());
    }

    @Override
    public <I extends StructureGenerationInfo> List<I> generationInfos(Class<I> clazz)
    {
        return generationInfos.stream().filter(info -> clazz.isAssignableFrom(info.getClass())).map(info -> (I) info).collect(Collectors.toList());
    }

    @Override
    public StructureGenerationInfo generationInfo(String id)
    {
        for (StructureGenerationInfo info : generationInfos)
        {
            if (Objects.equals(info.id(), id))
                return info;
        }

        return null;
    }

    private int getPass(IBlockState state)
    {
        return (state.isNormalCube() || state.getMaterial() == Material.AIR) ? 0 : 1;
    }

    @Override
    public GenericStructureInfo copyAsGenericStructureInfo()
    {
        return copy();
    }

    @Override
    public boolean areDependenciesResolved()
    {
        return dependencies.apply();
    }

    @Override
    public String toString()
    {
        String s = StructureRegistry.INSTANCE.structureID(this);
        return s != null ? s : "Generic Structure";
    }

    public GenericStructureInfo copy()
    {
        GenericStructureInfo genericStructureInfo = StructureRegistry.INSTANCE.createStructureFromJSON(StructureRegistry.INSTANCE.createJSONFromStructure(this));
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

            if (jsonobject.has("transformers"))
                Collections.addAll(structureInfo.transformers, context.<Transformer[]>deserialize(jsonobject.get("transformers"), Transformer[].class));
            if (jsonobject.has("blockTransformers")) // Legacy
                Collections.addAll(structureInfo.transformers, context.<Transformer[]>deserialize(jsonobject.get("blockTransformers"), Transformer[].class));

            structureInfo.rotatable = JsonUtils.getJsonObjectBooleanFieldValueOrDefault(jsonobject, "rotatable", false);
            structureInfo.mirrorable = JsonUtils.getJsonObjectBooleanFieldValueOrDefault(jsonobject, "mirrorable", false);

            if (jsonobject.has("dependencyExpression"))
                structureInfo.dependencies.setExpression(JsonUtils.getJsonObjectStringFieldValue(jsonobject, "dependencyExpression"));
            else if (jsonobject.has("dependencies")) // Legacy
                structureInfo.dependencies.setExpression(DependencyMatcher.ofMods(context.<String[]>deserialize(jsonobject.get("dependencies"), String[].class)));

            if (jsonobject.has("worldData"))
                structureInfo.worldDataCompound = context.deserialize(jsonobject.get("worldData"), NBTTagCompound.class);
            else if (jsonobject.has("worldDataBase64"))
                structureInfo.worldDataCompound = NbtToJson.getNBTFromBase64(JsonUtils.getJsonObjectStringFieldValue(jsonobject, "worldDataBase64"));
            // And else it is taken out for packet size, or stored in the zip

            if (jsonobject.has("metadata")) // Else, use default
                structureInfo.metadata = context.deserialize(jsonobject.get("metadata"), Metadata.class);

            structureInfo.customData = JsonUtils.getJsonObjectFieldOrDefault(jsonobject, "customData", new JsonObject());

            return structureInfo;
        }

        public JsonElement serialize(GenericStructureInfo structureInfo, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonobject = new JsonObject();

            jsonobject.addProperty("version", LATEST_VERSION);

            jsonobject.add("generationInfos", context.serialize(structureInfo.generationInfos));
            jsonobject.add("transformers", context.serialize(structureInfo.transformers));

            jsonobject.addProperty("rotatable", structureInfo.rotatable);
            jsonobject.addProperty("mirrorable", structureInfo.mirrorable);

            jsonobject.add("dependencyExpression", context.serialize(structureInfo.dependencies.getExpression()));

            if (!RecurrentComplex.USE_ZIP_FOR_STRUCTURE_FILES && structureInfo.worldDataCompound != null)
            {
                if (RecurrentComplex.USE_JSON_FOR_NBT)
                    jsonobject.add("worldData", context.serialize(structureInfo.worldDataCompound));
                else
                    jsonobject.addProperty("worldDataBase64", NbtToJson.getBase64FromNBT(structureInfo.worldDataCompound));
            }

            jsonobject.add("metadata", context.serialize(structureInfo.metadata));
            jsonobject.add("customData", structureInfo.customData);

            return jsonobject;
        }
    }

    public static class InstanceData implements NBTStorable
    {
        public static final String KEY_TRANSFORMERS = "transformers";
        public static final String KEY_TILE_ENTITIES = "tileEntities";

        public final Map<String, NBTStorable> transformers = new HashMap<>();
        public final TIntObjectMap<NBTStorable> transformerIndices = new TIntObjectHashMap<>(); // Legacy
        public final Map<BlockPos, NBTStorable> tileEntities = new HashMap<>();

        protected static NBTBase getTileEntityTag(NBTTagCompound tileEntityCompound, BlockPos coord)
        {
            return tileEntityCompound.getTag(getTileEntityKey(coord));
        }

        private static String getTileEntityKey(BlockPos coord)
        {
            return String.format("%d,%d,%d", coord.getX(), coord.getY(), coord.getZ());
        }

        public void readFromNBT(StructureLoadContext context, NBTBase nbt, List<Transformer> transformers, IvWorldData worldData)
        {
            IvBlockCollection blockCollection = worldData.blockCollection;
            NBTTagCompound compound = nbt instanceof NBTTagCompound ? (NBTTagCompound) nbt : new NBTTagCompound();

            List<NBTTagCompound> transformerCompounds = NBTTagLists.compoundsFrom(compound, KEY_TRANSFORMERS);
            for (int i = 0; i < transformerCompounds.size(); i++)
            {
                NBTTagCompound transformerCompound = transformerCompounds.get(i);
                String transformerID = transformerCompound.getString("id");

                Transformer transformer = findTransformer(transformers, i, transformerID); // Legacy - if no ID was saved, use the one in line if any.

                if (transformer != null)
                {
                    NBTStorable instanceData = transformer.loadInstanceData(context, transformerCompound.getTag("data"));
                    this.transformers.put(transformerID, instanceData);
                    transformerIndices.put(i, instanceData);
                }
            }

            int[] areaSize = new int[]{blockCollection.width, blockCollection.height, blockCollection.length};
            BlockPos origin = context.lowerCoord();

            NBTTagCompound tileEntityCompound = compound.getCompoundTag(InstanceData.KEY_TILE_ENTITIES);
            worldData.tileEntities.stream().filter(tileEntity -> tileEntity instanceof GeneratingTileEntity).forEach(teCompound -> {
                TileEntity tileEntity = RecurrentComplex.specialRegistry.loadTileEntity(TileEntities.getAnyWorld(), teCompound);
                BlockPos key = tileEntity.getPos();
                Mover.setTileEntityPos(tileEntity, context.transform.apply(key, areaSize).add(origin));
                tileEntities.put(key, (NBTStorable) ((GeneratingTileEntity) tileEntity).loadInstanceData(context, getTileEntityTag(tileEntityCompound, key)));
            });
        }

        protected Transformer findTransformer(List<Transformer> transformers, int i, String transformerID)
        {
            return transformers.stream().filter(tr -> tr.id().equals(transformerID)).findAny()
                    .orElse(transformers.size() > i ? transformers.get(i) : null);
        }

        protected NBTStorable findTransformerData(Transformer transformer, int index)
        {
            return transformers.entrySet().stream().filter(pair -> transformer.id().equals(pair.getKey()))
                    .map(Map.Entry::getValue)
                    .findAny().orElse(transformers.size() > index ? this.transformerIndices.get(index) : null);
        }

        @Override
        public NBTBase writeToNBT()
        {
            NBTTagCompound compound = new NBTTagCompound();

            NBTTagList transformerDatas = new NBTTagList();
            transformers.forEach((id, transformerData) -> {
                NBTTagCompound transformerCompound = new NBTTagCompound();
                transformerCompound.setTag("data", transformerData.writeToNBT());
                transformerCompound.setString("id", id);
                transformerDatas.appendTag(transformerCompound);
            });
            compound.setTag(KEY_TRANSFORMERS, transformerDatas);

            NBTTagCompound tileEntityCompound = new NBTTagCompound();
            for (Map.Entry<BlockPos, NBTStorable> entry : tileEntities.entrySet())
                tileEntityCompound.setTag(getTileEntityKey(entry.getKey()), entry.getValue().writeToNBT());
            compound.setTag(KEY_TILE_ENTITIES, tileEntityCompound);

            return compound;
        }
    }
}
