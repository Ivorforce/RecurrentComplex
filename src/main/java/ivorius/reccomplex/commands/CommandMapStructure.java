/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.network.PacketSaveStructureHandler;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.ivtoolkit.world.MockWorld;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandMapStructure extends CommandBase
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
    public String getCommandUsage(ICommandSender commandSender)
    {
        return ServerTranslations.usage("commands.rcmap.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length < 3)
            throw ServerTranslations.wrongUsageException("commands.rcmap.usage");

        String id = args[0];
        GenericStructure structure = RCCommands.getGenericStructure(id);
        ResourceDirectory directory = RCCommands.parseResourceDirectory(args[1]);

        ICommand other = server.getCommandManager().getCommands().get(args[2]);

        if (!(other instanceof CommandVirtual))
            throw ServerTranslations.commandException("commands.rcmap.nonvirtual");

        CommandVirtual virtual = (CommandVirtual) other;
        IvWorldData worldData = structure.constructWorldData();
        MockWorld world = new MockWorld.WorldData(worldData);

        try
        {
            virtual.execute(world, new CommandSelecting.SelectingSender(commandSender, BlockPos.ORIGIN, worldData.blockCollection.area().getHigherCorner()),
                    Arrays.copyOfRange(args, 3, args.length));
        }
        catch (MockWorld.VirtualWorldException ex)
        {
            throw ServerTranslations.commandException("commands.rcmap.nonvirtual.arguments");
        }

        structure.worldDataCompound = worldData.createTagCompound();
        PacketSaveStructureHandler.write(commandSender, structure, id, directory, true, true);
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, StructureRegistry.INSTANCE.ids());
        else if (args.length == 2)
            return getListOfStringsMatchingLastWord(args, Arrays.asList(ResourceDirectory.values()));

        return super.getTabCompletionOptions(server, sender, args, pos);
    }
}
