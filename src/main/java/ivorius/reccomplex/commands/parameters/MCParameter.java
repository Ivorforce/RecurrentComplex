/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import ivorius.ivtoolkit.math.MinecraftTransforms;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.utils.accessor.RCAccessorBiomeDictionary;
import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

/**
 * Created by lukas on 31.05.17.
 */
public class MCParameter extends Parameter
{
    public MCParameter(Parameter other)
    {
        super(other);
    }

    // Since CommandBase's version requires a sender
    public static BlockPos parseBlockPos(BlockPos blockpos, String[] args, int startIndex, boolean centerBlock) throws NumberInvalidException
    {
        return new BlockPos(CommandBase.parseDouble((double) blockpos.getX(), args[startIndex], -30000000, 30000000, centerBlock), CommandBase.parseDouble((double) blockpos.getY(), args[startIndex + 1], 0, 256, false), CommandBase.parseDouble((double) blockpos.getZ(), args[startIndex + 2], -30000000, 30000000, centerBlock));
    }

    @Override
    public MCParameter move(int idx)
    {
        return new MCParameter(super.move(idx));
    }

    public Result<BlockPos> pos(Parameter yp, Parameter zp, BlockPos ref, boolean centerBlock)
    {
        return first().missable().orElse("~").flatMap(x ->
                yp.first().missable().orElse("~").flatMap(y ->
                        zp.first().missable().orElse("~").map(z ->
                                parseBlockPos(ref, new String[]{x, y, z}, 0, centerBlock)
                        )));
    }

    public Result<BlockPos> pos(BlockPos ref, boolean centerBlock)
    {
        return pos(move(1), move(2), ref, centerBlock);
    }

    public Result<Biome> biome()
    {
        return first().map(ResourceLocation::new)
                .map(Biome.REGISTRY::getObject, t -> ServerTranslations.commandException("commands.rc.nobiome"));
    }

    public Result<BiomeDictionary.Type> biomeDictionaryType()
    {
        return first().map(RCAccessorBiomeDictionary::getTypeWeak, s -> ServerTranslations.commandException("commands.biomedict.notype", s));
    }

    public Result<WorldServer> dimension(MinecraftServer server, ICommandSender commandSender)
    {
        return first().filter(d -> !d.equals("~"), null).missable()
                .map(CommandBase::parseInt).map(server::worldServerForDimension, t -> ServerTranslations.commandException("commands.rc.nodimension"))
                .orElse((WorldServer) commandSender.getEntityWorld());
    }

    public Result<Block> block(ICommandSender commandSender)
    {
        return first().map(s -> CommandBase.getBlockByText(commandSender, s));
    }

    public Result<ICommand> command(MinecraftServer server)
    {
        return first().map(server.getCommandManager().getCommands()::get);
    }

    public Result<Entity> entity(MinecraftServer server, ICommandSender sender)
    {
        return first().map(s -> CommandBase.getEntity(server, sender, s));
    }

    public Result<Rotation> rotation()
    {
        return first().missable().map(CommandBase::parseInt)
                .map(i -> i > 40 ? i / 90 : i)
                .map(MinecraftTransforms::to);
    }
}
