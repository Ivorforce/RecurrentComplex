/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.blockTransformers;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.random.BlurredValueField;
import ivorius.reccomplex.worldgen.StructureSpawnContext;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class BlockTransformerRuins implements BlockTransformer
{
    public float minDecay;
    public float maxDecay;
    public float decayChaos;

    public BlockTransformerRuins(float minDecay, float maxDecay, float decayChaos)
    {
        this.minDecay = minDecay;
        this.maxDecay = maxDecay;
        this.decayChaos = decayChaos;
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

        float decayChaos = context.random.nextFloat() * this.decayChaos;
        if (this.maxDecay - this.minDecay > decayChaos)
            decayChaos = this.maxDecay - this.minDecay;

        float center = context.random.nextFloat() * (this.maxDecay - this.minDecay) + this.minDecay;
        int[] size = context.boundingBoxSize();

        BlurredValueField field = new BlurredValueField(size[0], size[2]);
        for (int i = 0; i < size[0] * size[2] / 25; i++)
            field.addValue(center + (context.random.nextFloat() - context.random.nextFloat()) * decayChaos * 2.0f, context.random);

        BlockArea topArea = new BlockArea(new BlockCoord(0, blockCollection.height, 0), new BlockCoord(blockCollection.width, blockCollection.height, blockCollection.length));

        for (int pass = 1; pass >= 0; pass--)
        {
            for (BlockCoord surfaceSourceCoord : topArea)
            {
                float decay = field.getValue(surfaceSourceCoord.x, surfaceSourceCoord.z);
                int removedBlocks = MathHelper.floor_float(decay * blockCollection.height + 0.5f);

                for (int ySource = 0; ySource < removedBlocks && ySource < size[1]; ySource++)
                {
                    BlockCoord sourceCoord = new BlockCoord(surfaceSourceCoord.x, blockCollection.height - 1 - ySource, surfaceSourceCoord.z);

                    Block block = blockCollection.getBlock(sourceCoord);
                    int meta = blockCollection.getMetadata(sourceCoord);

                    if (getPass(block, meta) == pass)
                    {
                        boolean skip = false;
                        for (BlockTransformer transformer : transformerList)
                        {
                            if (transformer.skipGeneration(block, meta))
                            {
                                skip = true;
                                break;
                            }
                        }

                        if (!skip)
                            setBlockToAirClean(context.world, context.transform.apply(sourceCoord, size).add(context.lowerCoord()));
                    }
                }
            }
        }
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

    @Override
    public String displayString()
    {
        return "Ruins";
    }

    @Override
    public boolean generatesInPhase(Phase phase)
    {
        return phase == Phase.AFTER;
    }
}
