/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.blocktransformers;

import com.google.gson.*;
import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.gui.editstructure.blocktransformers.TableDataSourceBTRuins;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.random.BlurredValueField;
import ivorius.reccomplex.structures.StructureSpawnContext;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public class BlockTransformerRuins implements BlockTransformer
{
    public static final ForgeDirection[] HORIZONTAL_DIRECTIONS = new ForgeDirection[]{ForgeDirection.NORTH, ForgeDirection.EAST, ForgeDirection.SOUTH, ForgeDirection.WEST};

    public float minDecay;
    public float maxDecay;
    public float decayChaos;

    public float blockErosion;
    public float vineGrowth;

    public BlockTransformerRuins()
    {
        this(0.0f, 0.9f, 0.3f, 0.3f, 0.1f);
    }

    public BlockTransformerRuins(float minDecay, float maxDecay, float decayChaos, float blockErosion, float vineGrowth)
    {
        this.minDecay = minDecay;
        this.maxDecay = maxDecay;
        this.decayChaos = decayChaos;
        this.blockErosion = blockErosion;
        this.vineGrowth = vineGrowth;
    }

    private static boolean skipBlock(Collection<BlockTransformer> transformers, Block block, int meta)
    {
        for (BlockTransformer transformer : transformers)
            if (transformer.skipGeneration(block, meta))
                return true;
        return false;
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
    public boolean skipGeneration(Block block, int metadata)
    {
        return false;
    }

    @Override
    public void transform(Phase phase, StructureSpawnContext context, IvWorldData worldData, List<BlockTransformer> transformerList)
    {
        IvBlockCollection blockCollection = worldData.blockCollection;

        BlockArea topdownArea = new BlockArea(new BlockCoord(0, blockCollection.height, 0), new BlockCoord(blockCollection.width, blockCollection.height, blockCollection.length));
        int[] size = context.boundingBoxSize();

        if (minDecay > 0.0f || maxDecay > 0.0f)
        {
            float decayChaos = context.random.nextFloat() * this.decayChaos;
            if (this.maxDecay - this.minDecay > decayChaos)
                decayChaos = this.maxDecay - this.minDecay;

            float center = context.random.nextFloat() * (this.maxDecay - this.minDecay) + this.minDecay;

            BlurredValueField field = new BlurredValueField(size[0], size[2]);
            for (int i = 0; i < size[0] * size[2] / 25; i++)
                field.addValue(center + (context.random.nextFloat() - context.random.nextFloat()) * decayChaos * 2.0f, context.random);

            for (int pass = 1; pass >= 0; pass--)
            {
                for (BlockCoord surfaceSourceCoord : topdownArea)
                {
                    float decay = field.getValue(surfaceSourceCoord.x, surfaceSourceCoord.z);
                    int removedBlocks = MathHelper.floor_float(decay * blockCollection.height + 0.5f);

                    for (int ySource = 0; ySource < removedBlocks && ySource < size[1]; ySource++)
                    {
                        BlockCoord sourceCoord = new BlockCoord(surfaceSourceCoord.x, blockCollection.height - 1 - ySource, surfaceSourceCoord.z);

                        Block block = blockCollection.getBlock(sourceCoord);
                        int meta = blockCollection.getMetadata(sourceCoord);

                        if (getPass(block, meta) == pass && !skipBlock(transformerList, block, meta))
                            setBlockToAirClean(context.world, context.transform.apply(sourceCoord, size).add(context.lowerCoord()));
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
                Block block = worldCoord.getBlock(context.world);
                int meta = worldCoord.getMetadata(context.world);

                if (!skipBlock(transformerList, block, meta))
                    decayBlock(context.world, context.random, block, meta, worldCoord);
            }
        }
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
                ForgeDirection[] directions = HORIZONTAL_DIRECTIONS.clone();
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
        return StatCollector.translateToLocal("reccomplex.blockTransformer.ruins");
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceBTRuins(this);
    }

    @Override
    public boolean generatesInPhase(Phase phase)
    {
        return phase == Phase.AFTER;
    }

    public static class Serializer implements JsonDeserializer<BlockTransformerRuins>, JsonSerializer<BlockTransformerRuins>
    {
        private MCRegistry registry;

        public Serializer(MCRegistry registry)
        {
            this.registry = registry;
        }

        @Override
        public BlockTransformerRuins deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonobject = JsonUtils.getJsonElementAsJsonObject(jsonElement, "transformerRuins");

            float minDecay = JsonUtils.getJsonObjectFloatFieldValueOrDefault(jsonobject, "minDecay", 0.0f);
            float maxDecay = JsonUtils.getJsonObjectFloatFieldValueOrDefault(jsonobject, "maxDecay", 0.9f);
            float decayChaos = JsonUtils.getJsonObjectFloatFieldValueOrDefault(jsonobject, "decayChaos", 0.3f);
            float blockErosion = JsonUtils.getJsonObjectFloatFieldValueOrDefault(jsonobject, "blockErosion", 0.0f);
            float vineGrowth = JsonUtils.getJsonObjectFloatFieldValueOrDefault(jsonobject, "vineGrowth", 0.0f);
            float gravity = JsonUtils.getJsonObjectFloatFieldValueOrDefault(jsonobject, "gravity", 0.0f);

            return new BlockTransformerRuins(minDecay, maxDecay, decayChaos, blockErosion, vineGrowth);
        }

        @Override
        public JsonElement serialize(BlockTransformerRuins transformer, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonobject = new JsonObject();

            jsonobject.addProperty("minDecay", transformer.minDecay);
            jsonobject.addProperty("maxDecay", transformer.maxDecay);
            jsonobject.addProperty("decayChaos", transformer.decayChaos);
            jsonobject.addProperty("blockErosion", transformer.blockErosion);
            jsonobject.addProperty("vineGrowth", transformer.vineGrowth);

            return jsonobject;
        }
    }
}
