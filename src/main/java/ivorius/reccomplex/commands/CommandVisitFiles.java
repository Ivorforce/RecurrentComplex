/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.commands.parameters.*;
import ivorius.reccomplex.commands.rcparameters.RCExpect;
import ivorius.reccomplex.commands.rcparameters.RCP;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandVisitFiles extends CommandExpecting
{
    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "files";
    }

    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Override
    public Expect<?> expect()
    {
        return RCExpect.expectRC()
                .named("directory", "d").resourceDirectory();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        Parameters parameters = Parameters.of(args, expect()::declare);

        ResourceDirectory directory = parameters.get("directory").to(RCP::resourceDirectory).optional().orElse(ResourceDirectory.ACTIVE);

        OpenGlHelper.openFile(directory.toFile());
    }
}
