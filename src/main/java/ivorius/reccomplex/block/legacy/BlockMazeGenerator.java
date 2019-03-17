/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.block.legacy;

import ivorius.reccomplex.block.BlockLegacyScript;
import ivorius.reccomplex.world.gen.script.WorldScriptMazeGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Created by lukas on 06.06.14.
 */
public class BlockMazeGenerator extends BlockLegacyScript
{
    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    public net.minecraft.tileentity.TileEntity createTileEntity(World var1, IBlockState var2)
    {
        return new TileEntityMazeGenerator();
    }

    public static class TileEntityMazeGenerator extends TileLegacyScript<WorldScriptMazeGenerator, WorldScriptMazeGenerator.InstanceData>
    {
        @Override
        public WorldScriptMazeGenerator createScript()
        {
            return new WorldScriptMazeGenerator();
        }
    }
}
