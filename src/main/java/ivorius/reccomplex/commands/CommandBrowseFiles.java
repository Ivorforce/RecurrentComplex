/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.io.File;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandBrowseFiles extends CommandBase
{
    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "files";
    }

    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    @Override
    public String getUsage(ICommandSender commandSender)
    {
        return ServerTranslations.usage("commands.rcfiles.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args)
    {
        // TODO Client-Side open
        OpenGlHelper.openFile(new File(ResourceDirectory.getCustomDirectory(), ResourceDirectory.RESOURCES_FILE_NAME));
    }
}
