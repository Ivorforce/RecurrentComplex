/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.network.PacketSaveStructureHandler;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.MockWorld;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructureInfo;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
        if (args.length < 3)
            throw ServerTranslations.wrongUsageException("commands.rcmap.usage");

        GenericStructureInfo structure = RCCommands.getGenericStructure(args[0]);
        ResourceDirectory directory = RCCommands.parseResourceDirectory(args[1]);

        ICommand other = server.getCommandManager().getCommands().get(args[2]);

        if (!(other instanceof VirtualCommand))
            throw ServerTranslations.commandException("commands.rcmap.nonvirtual");

        VirtualCommand virtual = (VirtualCommand) other;
        IvWorldData worldData = structure.constructWorldData();
        MockWorld world = new MockWorld.BlockCollection(worldData.blockCollection);

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
        PacketSaveStructureHandler.write(commandSender, structure, args[0], directory, true);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, StructureRegistry.INSTANCE.ids());
        else if (args.length == 2)
            return getListOfStringsMatchingLastWord(args, Arrays.asList(ResourceDirectory.values()));

        return super.getTabCompletions(server, sender, args, pos);
    }
}
