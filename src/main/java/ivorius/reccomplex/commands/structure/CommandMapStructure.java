/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.structure;

import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.ivtoolkit.world.MockWorld;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.commands.CommandSelecting;
import ivorius.reccomplex.commands.CommandVirtual;
import ivorius.reccomplex.commands.parameters.*;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.network.PacketSaveStructureHandler;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandMapStructure extends CommandExpecting
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "map";
    }

    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    @Override
    public Expect<?> expect()
    {
        return RCExpect.expectRC()
                .structure()
                .virtualCommand()
                .commandArguments(p -> p.get(1)).repeat()
                .named("directory", "d").resourceDirectory();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args, expect()::declare);

        String id = parameters.get().first().require();
        ResourceDirectory directory = parameters.get("directory").resourceDirectory().optional().orElse(ResourceDirectory.ACTIVE);
        CommandVirtual virtual = parameters.get(1).virtualCommand(server).require();

        CommandMapAllStructure.map(id, directory, sender, virtual, parameters.get(2).varargs(), true);
    }
}
