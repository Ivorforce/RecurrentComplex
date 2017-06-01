/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.ivtoolkit.world.MockWorld;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.commands.parameters.RCParameters;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.network.PacketSaveStructureHandler;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
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
public class CommandMapStructure extends CommandBase
{
    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "map";
    }

    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    @Override
    public String getUsage(ICommandSender commandSender)
    {
        return ServerTranslations.usage("commands.rcmap.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args);

        String id = parameters.get().first().require();
        GenericStructure structure = parameters.rc().genericStructure().require();
        ResourceDirectory directory = parameters.rc("dir").resourceDirectory().optional().orElse(ResourceDirectory.ACTIVE);
        CommandVirtual virtual = parameters.rc().move(1).virtualCommand(server).require();

        IvWorldData worldData = structure.constructWorldData();
        MockWorld world = new MockWorld.WorldData(worldData);

        try
        {
            virtual.execute(world, new CommandSelecting.SelectingSender(commandSender, BlockPos.ORIGIN, worldData.blockCollection.area().getHigherCorner()),
                    parameters.get().move(2).varargs());
        }
        catch (MockWorld.VirtualWorldException ex)
        {
            throw ServerTranslations.commandException("commands.rcmap.nonvirtual.arguments");
        }

        structure.worldDataCompound = worldData.createTagCompound();
        PacketSaveStructureHandler.write(commandSender, structure, id, directory, true, true);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        RCParameters parameters = RCParameters.of(args);

        return RCExpect.startRC()
                .structure()
                .command()
                .commandArguments(parameters.get().move(1), sender).repeat()
                .named("dir").resourceDirectory()
                .get(server, sender, args, pos);
    }
}
