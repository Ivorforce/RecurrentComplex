/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.utils.algebra.ExpressionCache;
import ivorius.reccomplex.utils.expression.BlockMatcher;
import ivorius.reccomplex.world.gen.feature.structure.StructureInfo;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructureInfo;
import ivorius.reccomplex.world.gen.feature.structure.generic.Metadata;
import ivorius.reccomplex.world.gen.feature.structure.generic.gentypes.GenerationInfo;
import joptsimple.internal.Strings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandContaining extends CommandBase
{
    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "containing";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.rccontaining.usage");
    }

    public static long containedBlocks(StructureInfo structure, BlockMatcher matcher)
    {
        if (structure == null)
            return 0;

        IvBlockCollection collection = structure.blockCollection();

        if (collection == null)
            return 0;

        return collection.area().stream()
                .anyMatch(p -> matcher.evaluate(collection.getBlockState(p))) ? 1 : 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length >= 1)
        {
            BlockMatcher matcher = ExpressionCache.of(new BlockMatcher(RecurrentComplex.specialRegistry), buildString(args, 0));
            RCCommands.ensureValid(matcher, 0);
            CommandSearchStructure.outputSearch(commandSender, StructureRegistry.INSTANCE.ids(),
                    name -> containedBlocks(StructureRegistry.INSTANCE.get(name), matcher),
                    CommandSearchStructure::structureTextComponent
            );
        }
        else
            throw ServerTranslations.commandException("commands.rccontaining.usage");
    }
}
