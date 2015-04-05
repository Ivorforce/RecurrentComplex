/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.transformers;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.gson.*;
import ivorius.ivtoolkit.blocks.*;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.ivtoolkit.tools.NBTTagCompounds;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.editstructure.transformers.TableDataSourceBTRuins;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.random.BlurredValueField;
import ivorius.reccomplex.structures.StructureLoadContext;
import ivorius.reccomplex.structures.StructurePrepareContext;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.utils.BlockAreas2;
import ivorius.reccomplex.utils.NBTStorable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public class TransformerRuins implements Transformer<TransformerRuins.InstanceData>
{
    public ForgeDirection decayDirection;
    public float minDecay;
    public float maxDecay;
    public float decayChaos;

    public float blockErosion;
    public float vineGrowth;

    public TransformerRuins()
    {
        this(ForgeDirection.DOWN, 0.0f, 0.9f, 0.3f, 0.3f, 0.1f);
    }

    public TransformerRuins(ForgeDirection decayDirection, float minDecay, float maxDecay, float decayChaos, float blockErosion, float vineGrowth)
    {
        this.decayDirection = decayDirection;
        this.minDecay = minDecay;
        this.maxDecay = maxDecay;
        this.decayChaos = decayChaos;
        this.blockErosion = blockErosion;
        this.vineGrowth = vineGrowth;
    }

    private static boolean skipBlock(Collection<Pair<Transformer, NBTStorable>> transformers, final Block block, final int meta)
    {
        return Iterables.any(transformers, new Predicate<Pair<Transformer, NBTStorable>>()
        {
            @Override
            public boolean apply(Pair<Transformer, NBTStorable> input)
            {
                return input.getLeft().skipGeneration(input.getRight(), block, meta);
            }
        });
    }

    private static int getPass(Block block, int metadata)
    {
        return (block.isNormalCube() || block.getMaterial() == Material.air) ? 0 : 1;
    }

    public static void setBlockToAirClean(World world, BlockCoord blockCoord)
    {
        TileEntity tileEntity = world.getTileEntity(blockCoord.x, blockCoord.y, blockCoord.z);
        if (tileEntity instanceof IInventory)
        {
            IInventory inventory = (IInventory) tileEntity;
            for (int i = 0; i < inventory.getSizeInventory(); i++)
                inventory.setInventorySlotContents(i, null);
        }

        world.setBlockToAir(blockCoord.x, blockCoord.y, blockCoord.z);
    }

    public static void shuffleArray(Object[] ar, Random rand)
    {
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rand.nextInt(i + 1);

            Object a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    public static int vineMetadata(ForgeDirection direction)
    {
        switch (direction)
        {
            case NORTH:
                return 1;
            case SOUTH:
                return 4;
            case WEST:
                return 8;
            case EAST:
                return 2;
        }

        throw new IllegalArgumentException();
    }

    @Override
    public boolean skipGeneration(InstanceData instanceData, Block block, int metadata)
    {
        return false;
    }

    @Override
    public void transform(InstanceData instanceData, Phase phase, StructureSpawnContext context, IvWorldData worldData, List<Pair<Transformer, NBTStorable>> transformers)
    {
        IvBlockCollection blockCollection = worldData.blockCollection;

        BlockArea sourceArea = new BlockArea(new BlockCoord(0, 0, 0), new BlockCoord(blockCollection.width, blockCollection.height, blockCollection.length));
        BlockArea decaySideAray = BlockAreas.side(sourceArea, decayDirection.getOpposite());
        int decaySideLength = BlockAreas2.sideLength(sourceArea, decayDirection.getOpposite());

        int[] size = context.boundingBoxSize();

        StructureBoundingBox dropAreaBB = context.boundingBox;
        RecurrentComplex.forgeEventHandler.disabledTileDropAreas.add(dropAreaBB);

        BlurredValueField field = instanceData.blurredValueField;
        if (field != null)
        {
            ForgeDirection decayDirection = Directions.rotate(this.decayDirection, context.transform);

            for (int pass = 1; pass >= 0; pass--)
            {
                for (BlockCoord surfaceSourceCoord : decaySideAray)
                {
                    float decay = field.getValue(surfaceSourceCoord.x, surfaceSourceCoord.z);
                    int removedBlocks = MathHelper.floor_float(decay * decaySideLength + 0.5f);

                    BiomeGenBase biome = context.world.getBiomeGenForCoords(surfaceSourceCoord.x, surfaceSourceCoord.z);
                    Block topBlock = biome.topBlock != null ? biome.topBlock : Blocks.air;
                    Block fillerBlock = biome.fillerBlock != null ? biome.fillerBlock : Blocks.air;
                    Block mainBlock = context.world.provider.dimensionId == -1 ? Blocks.netherrack : (context.world.provider.dimensionId == 1 ? Blocks.end_stone : Blocks.stone);

                    for (int decayPos = 0; decayPos < removedBlocks && decayPos < decaySideLength; decayPos++)
                    {
                        BlockCoord sourceCoord = surfaceSourceCoord.add(decayDirection.offsetX * decayPos, decayDirection.offsetY * decayPos, decayDirection.offsetZ * decayPos);
                        BlockCoord worldCoord = context.transform.apply(sourceCoord, size).add(context.lowerCoord());

                        if (context.includes(worldCoord))
                        {
                            Block block = blockCollection.getBlock(sourceCoord);

                            if (block != topBlock && block != fillerBlock && block != mainBlock)
                            {
                                int meta = blockCollection.getMetadata(sourceCoord);

                                if (getPass(block, meta) == pass && !skipBlock(transformers, block, meta))
                                    setBlockToAirClean(context.world, worldCoord);
                            }
                        }
                    }
                }
            }
        }

        int[] areaSize = new int[]{blockCollection.width, blockCollection.height, blockCollection.length};
        if (blockErosion > 0.0f || vineGrowth > 0.0f)
        {
            for (BlockCoord sourceCoord : blockCollection)
            {
                BlockCoord worldCoord = context.transform.apply(sourceCoord, areaSize).add(context.lowerCoord());

                if (context.includes(worldCoord))
                {
                    Block block = worldCoord.getBlock(context.world);
                    int meta = worldCoord.getMetadata(context.world);

                    if (!skipBlock(transformers, block, meta))
                        decayBlock(context.world, context.random, block, meta, worldCoord);
                }
            }
        }

        RecurrentComplex.forgeEventHandler.disabledTileDropAreas.remove(dropAreaBB);
    }

    public void decayBlock(World world, Random random, Block block, int meta, BlockCoord coord)
    {
        Block newBlock = block;
        int newMeta = meta;

        if (random.nextFloat() < blockErosion)
        {
            if (newBlock == Blocks.stonebrick && newMeta == 0)
                newMeta = 2;
        }

        if (random.nextFloat() < vineGrowth)
        {
            if (newBlock == Blocks.stonebrick && (newMeta == 2 || newMeta == 0))
                newMeta = 1;
            else if (newBlock == Blocks.cobblestone)
            {
                newBlock = Blocks.mossy_cobblestone;
                newMeta = 0;
            }
            else if (newBlock == Blocks.cobblestone_wall && newMeta == 0)
                newMeta = 1;
            else if (newBlock == Blocks.air)
            {
                ForgeDirection[] directions = Directions.HORIZONTAL.clone();
                shuffleArray(directions, random);

                for (ForgeDirection direction : directions)
                {
                    if (Blocks.vine.canPlaceBlockOnSide(world, coord.x, coord.y, coord.z, direction.ordinal()))
                    {
                        newBlock = Blocks.vine;
                        newMeta = vineMetadata(direction);

                        int length = 1 + random.nextInt(MathHelper.floor_float(vineGrowth * 10.0f + 3));
                        for (int y = 0; y < length; y++)
                        {
                            if (world.getBlock(coord.x, coord.y - y, coord.z) == Blocks.air)
                                world.setBlock(coord.x, coord.y - y, coord.z, Blocks.vine, newMeta, 3);
                            else
                                break;
                        }

                        break;
                    }
                }
            }
        }

        if (block != newBlock || meta != newMeta)
            world.setBlock(coord.x, coord.y, coord.z, newBlock, newMeta, 3);
    }

    @Override
    public String getDisplayString()
    {
        return StatCollector.translateToLocal("reccomplex.transformer.ruins");
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceBTRuins(this);
    }

    @Override
    public InstanceData prepareInstanceData(StructurePrepareContext context)
    {
        InstanceData instanceData = new InstanceData();

        if (minDecay > 0.0f || maxDecay > 0.0f)
        {
            int[] size = context.boundingBoxSize();

            float decayChaos = context.random.nextFloat() * this.decayChaos;
            if (this.maxDecay - this.minDecay > decayChaos)
                decayChaos = this.maxDecay - this.minDecay;

            float center = context.random.nextFloat() * (this.maxDecay - this.minDecay) + this.minDecay;

            instanceData.blurredValueField = new BlurredValueField(size[0], size[2]);
            for (int i = 0; i < size[0] * size[2] / 25; i++)
                instanceData.blurredValueField.addValue(center + (context.random.nextFloat() - context.random.nextFloat()) * decayChaos * 2.0f, context.random);
        }

        return instanceData;
    }

    @Override
    public InstanceData loadInstanceData(StructureLoadContext context, NBTBase nbt)
    {
        return new InstanceData(nbt instanceof NBTTagCompound ? (NBTTagCompound) nbt : new NBTTagCompound());
    }

    @Override
    public boolean generatesInPhase(InstanceData instanceData, Phase phase)
    {
        return phase == Phase.AFTER;
    }

    public static class InstanceData implements NBTStorable
    {
        public BlurredValueField blurredValueField;

        public InstanceData()
        {
        }

        public InstanceData(NBTTagCompound compound)
        {
            blurredValueField = NBTTagCompounds.read(compound.getCompoundTag("field"), BlurredValueField.class);
        }

        @Override
        public NBTBase writeToNBT()
        {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setTag("field", NBTTagCompounds.write(blurredValueField));
            return compound;
        }
    }

    public static class Serializer implements JsonDeserializer<TransformerRuins>, JsonSerializer<TransformerRuins>
    {
        private MCRegistry registry;

        public Serializer(MCRegistry registry)
        {
            this.registry = registry;
        }

        @Override
        public TransformerRuins deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(jsonElement, "transformerRuins");

            ForgeDirection decayDirection = Directions.deserialize(JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "decayDirection", "DOWN"));
            float minDecay = JsonUtils.getJsonObjectFloatFieldValueOrDefault(jsonObject, "minDecay", 0.0f);
            float maxDecay = JsonUtils.getJsonObjectFloatFieldValueOrDefault(jsonObject, "maxDecay", 0.9f);
            float decayChaos = JsonUtils.getJsonObjectFloatFieldValueOrDefault(jsonObject, "decayChaos", 0.3f);
            float blockErosion = JsonUtils.getJsonObjectFloatFieldValueOrDefault(jsonObject, "blockErosion", 0.0f);
            float vineGrowth = JsonUtils.getJsonObjectFloatFieldValueOrDefault(jsonObject, "vineGrowth", 0.0f);
            float gravity = JsonUtils.getJsonObjectFloatFieldValueOrDefault(jsonObject, "gravity", 0.0f);

            return new TransformerRuins(decayDirection, minDecay, maxDecay, decayChaos, blockErosion, vineGrowth);
        }

        @Override
        public JsonElement serialize(TransformerRuins transformer, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonobject = new JsonObject();

            jsonobject.addProperty("decayDirection", Directions.serialize(transformer.decayDirection));
            jsonobject.addProperty("minDecay", transformer.minDecay);
            jsonobject.addProperty("maxDecay", transformer.maxDecay);
            jsonobject.addProperty("decayChaos", transformer.decayChaos);
            jsonobject.addProperty("blockErosion", transformer.blockErosion);
            jsonobject.addProperty("vineGrowth", transformer.vineGrowth);

            return jsonobject;
        }
    }
}
