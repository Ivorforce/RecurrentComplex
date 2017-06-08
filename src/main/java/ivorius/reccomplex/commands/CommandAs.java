/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.commands.parameters.*;
import ivorius.reccomplex.commands.parameters.expect.Expect;
import ivorius.reccomplex.commands.parameters.expect.MCE;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandAs extends CommandExpecting
{
    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "as";
    }

    public int getRequiredPermissionLevel()
    {
        return RCConfig.asCommandPermissionLevel;
    }

    @Override
    public Expect expect()
    {
        return Parameters.expect()
                .then(MCE::entity).required()
                .then(MCE::command).required()
                .then(MCE.commandArguments(p -> p.get(1))).repeat();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        Parameters parameters = Parameters.of(args, expect()::declare);

        Entity entity = parameters.get(0).to(MCP.entity(server, commandSender)).require();
        String command = buildString(args, 1);

        server.commandManager.executeCommand(entity, command);
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index)
    {
        return index == 0;
    }
}
