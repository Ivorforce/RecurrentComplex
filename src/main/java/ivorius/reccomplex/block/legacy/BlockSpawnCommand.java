/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.block.legacy;

import ivorius.reccomplex.block.BlockLegacyScript;
import ivorius.reccomplex.block.GeneratingTileEntity;
import ivorius.reccomplex.nbt.NBTNone;
import ivorius.reccomplex.world.gen.script.WorldScriptCommand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Created by lukas on 06.06.14.
 */
public class BlockSpawnCommand extends BlockLegacyScript
{
    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    public net.minecraft.tileentity.TileEntity createTileEntity(World world, IBlockState state)
    {
        return new TileEntitySpawnCommand();
    }

    public static class TileEntitySpawnCommand extends TileLegacyScript<WorldScriptCommand, NBTNone> implements GeneratingTileEntity<NBTNone>
    {
        @Override
        public WorldScriptCommand createScript()
        {
            return new WorldScriptCommand();
        }
    }
}
