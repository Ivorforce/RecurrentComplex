/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.mcopts.commands.parameters;

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

import java.util.function.Function;

/**
 * Created by lukas on 31.05.17.
 */
public class MCP
{
    // Since CommandBase's version requires a sender
    public static BlockPos parseBlockPos(BlockPos blockpos, String[] args, int startIndex, boolean centerBlock) throws NumberInvalidException
    {
        return new BlockPos(CommandBase.parseDouble((double) blockpos.getX(), args[startIndex], -30000000, 30000000, centerBlock), CommandBase.parseDouble((double) blockpos.getY(), args[startIndex + 1], 0, 256, false), CommandBase.parseDouble((double) blockpos.getZ(), args[startIndex + 2], -30000000, 30000000, centerBlock));
    }

    public static Function<Parameter<String>, Parameter<BlockPos>> pos(Parameter<String> yp, Parameter<String> zp, BlockPos ref, boolean centerBlock)
    {
        return xp -> xp.orElse("~").flatMap(x ->
                yp.orElse("~").flatMap(y ->
                        zp.orElse("~").map(z ->
                                parseBlockPos(ref, new String[]{x, y, z}, 0, centerBlock)
                        )));
    }

    public static Function<Parameter<String>, Parameter<BlockPos>> pos(BlockPos ref, boolean centerBlock)
    {
        return p -> pos(p.move(1), p.move(2), ref, centerBlock).apply(p);
    }

    public static Function<Parameters, Parameter<BlockPos>> pos(String x, String y, String z, BlockPos ref, boolean centerBlock)
    {
        return p -> p.get(x).to(pos(p.get(y), p.get(z), ref, centerBlock));
    }

    public static Parameter<Biome> biome(Parameter<String> p)
    {
        return p.map(ResourceLocation::new)
                .map(Biome.REGISTRY::getObject, t -> ServerTranslations.commandException("commands.rc.nobiome"));
    }

    public static Parameter<BiomeDictionary.Type> biomeDictionaryType(Parameter<String> p)
    {
        return p.map(RCAccessorBiomeDictionary::getTypeWeak, s -> ServerTranslations.commandException("commands.biomedict.notype", s));
    }

    public static Function<Parameter<String>, Parameter<WorldServer>> dimension(MinecraftServer server, ICommandSender sender)
    {
        return p -> p.filter(d -> !d.equals("~"), null)
                .map(CommandBase::parseInt).map(server::worldServerForDimension, t -> ServerTranslations.commandException("commands.rc.nodimension"))
                .orElse((WorldServer) sender.getEntityWorld());
    }

    public static Function<Parameter<String>, Parameter<Block>> block(ICommandSender commandSender)
    {
        return p -> p.map(s -> CommandBase.getBlockByText(commandSender, s));
    }

    public static Function<Parameter<String>, Parameter<ICommand>> command(MinecraftServer server)
    {
        return p -> p.map(server.getCommandManager().getCommands()::get);
    }

    public static Function<Parameter<String>, Parameter<Entity>> entity(MinecraftServer server, ICommandSender sender)
    {
        return p -> p.map(s -> CommandBase.getEntity(server, sender, s));
    }

    public static Parameter<Rotation> rotation(Parameter<String> p)
    {
        return p.map(CommandBase::parseInt)
                .map(i -> i > 40 ? i / 90 : i)
                .map(MinecraftTransforms::to);
    }
}
