/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.structure;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.mcopts.commands.CommandExpecting;
import ivorius.reccomplex.mcopts.commands.parameters.*;
import ivorius.reccomplex.mcopts.commands.parameters.expect.Expect;
import ivorius.reccomplex.commands.parameters.expect.RCE;
import ivorius.reccomplex.commands.parameters.RCP;
import ivorius.reccomplex.network.PacketEditStructureHandler;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandEditStructure extends CommandExpecting
{
    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "edit";
    }

    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    @Override
    public Expect expect()
    {
        return Parameters.expect().then(RCE::structure)
                .named("from").then(RCE::structure);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        EntityPlayerMP entityPlayerMP = getCommandSenderAsPlayer(commandSender);
        Parameters parameters = Parameters.of(args, expect()::declare);

        String id = parameters.get(0).require();
        GenericStructure base = parameters.get(0).to(RCP::genericStructure).require();
        GenericStructure from = parameters.get("from").to(RCP::genericStructure).optional().orElse(base);

        if (base != from)
        {
            from = from.copyAsGenericStructure();
            from.worldDataCompound = base.worldDataCompound.copy();
        }

        PacketEditStructureHandler.openEditStructure(from, id, entityPlayerMP);
    }
}
