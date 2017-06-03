/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.commands.parameters.RCParameters;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandVisitFiles extends CommandBase
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
    public String getUsage(ICommandSender commandSender)
    {
        return ServerTranslations.usage("commands.rcfiles.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args);

        ResourceDirectory directory = parameters.rc("dir").resourceDirectory().optional().orElse(ResourceDirectory.ACTIVE);

        OpenGlHelper.openFile(directory.toFile());
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        return RCExpect.expectRC()
                .named("dir").resourceDirectory()
                .get(server, sender, args, targetPos);
    }
}
