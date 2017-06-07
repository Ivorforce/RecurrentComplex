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

import java.util.function.BinaryOperator;

/**
 * Created by lukas on 31.05.17.
 */
public class MCParameter<P extends MCParameter<P>> extends ParameterString<P>
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
    public P copy(Parameter<String, ?> p)
    {
        //noinspection unchecked
        return (P) new MCParameter(p);
    }

    public Parameter<BlockPos, ?> pos(Parameter<String, ?> yp, Parameter<String, ?> zp, BlockPos ref, boolean centerBlock)
    {
        return orElse("~").flatMap(x ->
                yp.orElse("~").flatMap(y ->
                        zp.orElse("~").map(z ->
                                parseBlockPos(ref, new String[]{x, y, z}, 0, centerBlock)
                        )));
    }

    public Parameter<BlockPos, ?> pos(BlockPos ref, boolean centerBlock)
    {
        return pos(this.move(1), this.move(2), ref, centerBlock);
    }

    public Parameter<Biome, ?> biome()
    {
        return map(ResourceLocation::new)
                .map(Biome.REGISTRY::getObject, t -> ServerTranslations.commandException("commands.rc.nobiome"));
    }

    public Parameter<BiomeDictionary.Type, ?> biomeDictionaryType()
    {
        return map(RCAccessorBiomeDictionary::getTypeWeak, s -> ServerTranslations.commandException("commands.biomedict.notype", s));
    }

    public Parameter<WorldServer, ?> dimension(MinecraftServer server, ICommandSender commandSender)
    {
        return filter(d -> !d.equals("~"), null)
                .map(CommandBase::parseInt).map(server::worldServerForDimension, t -> ServerTranslations.commandException("commands.rc.nodimension"))
                .orElse((WorldServer) commandSender.getEntityWorld());
    }

    public Parameter<Block, ?> block(ICommandSender commandSender)
    {
        return map(s -> CommandBase.getBlockByText(commandSender, s));
    }

    public Parameter<ICommand, ?> command(MinecraftServer server)
    {
        return map(server.getCommandManager().getCommands()::get);
    }

    public Parameter<Entity, ?> entity(MinecraftServer server, ICommandSender sender)
    {
        return map(s -> CommandBase.getEntity(server, sender, s));
    }

    public Parameter<Rotation, ?> rotation()
    {
        return map(CommandBase::parseInt)
                .map(i -> i > 40 ? i / 90 : i)
                .map(MinecraftTransforms::to);
    }
}
