/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.structures.generic.matchers.BlockMatcher;
import ivorius.reccomplex.structures.generic.matchers.PositionedBlockMatcher;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSetProperty extends CommandSelectModify
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "property";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.selectProperty.usage");
    }

    @Override
    public void executeSelection(EntityPlayerMP player, StructureEntityInfo structureEntityInfo, BlockPos point1, BlockPos point2, String[] args) throws CommandException
    {
        if (args.length >= 2)
        {
            World world = player.getEntityWorld();

            PositionedBlockMatcher matcher = new PositionedBlockMatcher(RecurrentComplex.specialRegistry, args.length > 2 ? buildString(args, 2) : "");

            String propertyName = args[0];
            String propertyValue = args[1];

            for (BlockPos coord : new BlockArea(point1, point2))
            {
                PositionedBlockMatcher.Argument at = PositionedBlockMatcher.Argument.at(world, coord);
                Optional<IProperty<?>> mProperty = at.state.getProperties().keySet().stream().filter(p -> p.getName().equals(propertyName)).findFirst();

                if (matcher.test(at) && mProperty.isPresent())
                {
                    IProperty property = mProperty.get();
                    Optional<Comparable> value = property.getAllowedValues().stream()
                            .filter(v -> property.getName((Comparable) v).equals(propertyValue)).findAny();
                    if (value.isPresent())
                        world.setBlockState(coord, at.state.withProperty(property, value.get()), 3);
                }
            }
        }
        else
        {
            throw ServerTranslations.wrongUsageException("commands.selectProperty.usage");
        }
    }

    @Nonnull
    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, Block.REGISTRY.getKeys().stream().map(Block.REGISTRY::getObject)
                    .flatMap(b -> b.getDefaultState().getPropertyNames().stream().map(IProperty::getName)).collect(Collectors.toSet()));
        else if (args.length == 2)
            return getListOfStringsMatchingLastWord(args, Block.REGISTRY.getKeys().stream().map(Block.REGISTRY::getObject)
                    .map(b -> b.getDefaultState().getPropertyNames().stream().filter(p -> p.getName().equals(args[0])).findFirst().orElse(null))
                    .filter(Objects::nonNull).flatMap(p -> p.getAllowedValues().stream().map(p::getName)).collect(Collectors.toSet()));
        else if (args.length == 3)
            return getListOfStringsMatchingLastWord(args, Block.REGISTRY.getKeys());

        return Collections.emptyList();
    }
}
