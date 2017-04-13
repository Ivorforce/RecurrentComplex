/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.utils.algebra.ExpressionCache;
import ivorius.reccomplex.utils.expression.BlockMatcher;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandContaining extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "containing";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.rccontaining.usage");
    }

    public static long containedBlocks(Structure structure, BlockMatcher matcher)
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
                    RCTextStyle::structure
            );
        }
        else
            throw ServerTranslations.commandException("commands.rccontaining.usage");
    }
}
