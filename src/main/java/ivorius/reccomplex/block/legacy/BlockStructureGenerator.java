/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.block.legacy;

import ivorius.reccomplex.block.BlockLegacyScript;
import ivorius.reccomplex.world.gen.script.WorldScriptStructureGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Created by lukas on 06.06.14.
 */
public class BlockStructureGenerator extends BlockLegacyScript
{
    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    public net.minecraft.tileentity.TileEntity createTileEntity(World world, IBlockState state)
    {
        return new TileEntityStructureGenerator();
    }

    public static class TileEntityStructureGenerator extends TileLegacyScript<WorldScriptStructureGenerator, WorldScriptStructureGenerator.InstanceData>
    {
        @Override
        public WorldScriptStructureGenerator createScript()
        {
            return new WorldScriptStructureGenerator();
        }
    }
}
