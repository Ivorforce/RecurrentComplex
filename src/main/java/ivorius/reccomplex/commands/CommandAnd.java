/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.world.MockWorld;
import ivorius.mcopts.commands.CommandExpecting;
import ivorius.mcopts.commands.parameters.Parameter;
import ivorius.mcopts.commands.parameters.Parameters;
import ivorius.mcopts.commands.parameters.expect.Expect;
import ivorius.mcopts.commands.parameters.expect.MCE;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.commands.parameters.RCP;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.Arrays;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandAnd extends CommandExpecting implements CommandVirtual
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "&";
    }

    @Override
    public void expect(Expect expect)
    {
        expect
                .split(this::expectSubcommand).required().repeat()
        ;
    }

    public void expectSubcommand(Expect expect)
    {
        expect
                .stopInterpreting()
                .then(MCE::command)
                .then(MCE::commandArguments, p -> p.get(0)).repeat()
        ;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        Parameters parameters = Parameters.of(args, expect()::declare);

        for (String param : parameters.get(0).varargsList().require())
            server.commandManager.executeCommand(commandSender, param);
    }

    @Override
    public void execute(MockWorld world, ICommandSender sender, String[] args) throws CommandException
    {
        Parameters parameters = Parameters.of(args, expect()::declare);

        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        for (String param : parameters.get(0).varargsList().require())
        {
            String[] commandArgs = param.split(" ");
            CommandVirtual command = Parameter.makeUp("command", 0, commandArgs[0]).to(RCP::virtualCommand, server).require();

            command.execute(world, sender, Arrays.stream(commandArgs).skip(1).toArray(String[]::new));
        }
    }
}
