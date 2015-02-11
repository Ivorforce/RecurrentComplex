/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.blocktransformers;

import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.structures.StructureSpawnContext;
import net.minecraft.block.Block;

import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class BlockTransformerNegativeSpace implements BlockTransformer
{
    public Block sourceBlock;
    public int sourceMetadata;

    public BlockTransformerNegativeSpace(Block sourceBlock, int sourceMetadata)
    {
        this.sourceBlock = sourceBlock;
        this.sourceMetadata = sourceMetadata;
    }

    @Override
    public boolean skipGeneration(Block block, int metadata)
    {
        return block == sourceBlock && (metadata < 0 || metadata == sourceMetadata);
    }

    @Override
    public void transform(Phase phase, StructureSpawnContext context, IvWorldData worldData, List<BlockTransformer> transformerList)
    {

    }

    @Override
    public String displayString()
    {
        return "Space: " + sourceBlock.getLocalizedName();
    }

    @Override
    public boolean generatesInPhase(Phase phase)
    {
        return false;
    }
}
