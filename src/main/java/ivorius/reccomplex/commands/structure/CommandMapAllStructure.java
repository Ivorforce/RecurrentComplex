/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.structure;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.commands.CommandVirtual;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.commands.RCTextStyle;
import ivorius.reccomplex.commands.parameters.*;
import ivorius.reccomplex.files.loading.LeveledRegistry;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.utils.RawResourceLocation;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.utils.expression.ResourceExpression;
import ivorius.reccomplex.utils.optional.IvOptional;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandMapAllStructure extends CommandExpecting
{
    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "mapall";
    }

    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    @Override
    public Expect<?> expect()
    {
        return RCExpect.expectRC()
                .virtualCommand()
                .commandArguments(Parameters::get)
                .named("exp").structure()
                .named("directory", "d").resourceDirectory();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args, expect()::declare);

        ResourceExpression expression = new ResourceExpression(StructureRegistry.INSTANCE::has);
        IvOptional.ifAbsent(parameters.get("exp").expression(expression).optional(), () -> expression.setExpression(""));

        ResourceDirectory directory = parameters.get("directory").resourceDirectory().optional().orElse(ResourceDirectory.ACTIVE);
        CommandVirtual virtual = parameters.get().virtualCommand(server).require();
        String[] virtualArgs = parameters.get(1).varargs();

        int saved = 0, failed = 0, skipped = 0;
        for (String id : StructureRegistry.INSTANCE.ids())
        {
            if (!expression.test(new RawResourceLocation(StructureRegistry.INSTANCE.status(id).getDomain(), id)))
                continue;

            switch (CommandMapStructure.map(id, directory, commandSender, virtual, virtualArgs, false))
            {
                case SKIPPED:
                    skipped++;
                    break;
                case SUCCESS:
                    saved++;
                    break;
                default:
                    failed++;
                    break;
            }
        }

        commandSender.sendMessage(ServerTranslations.format("commands.rcmapall.result", saved, RCTextStyle.path(directory), failed, skipped));

        RCCommands.tryReload(RecurrentComplex.loader, LeveledRegistry.Level.CUSTOM);
        RCCommands.tryReload(RecurrentComplex.loader, LeveledRegistry.Level.SERVER);
    }

}
