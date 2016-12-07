/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.network.PacketEditStructureHandler;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructureInfo;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandEditStructure extends CommandBase
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
    public String getUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.strucEdit.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        EntityPlayerMP entityPlayerMP = getCommandSenderAsPlayer(commandSender);

        if (args.length >= 1)
        {
            String structureID = args[0];

            GenericStructureInfo structureInfo = CommandExportStructure.getGenericStructureInfo(structureID);
            PacketEditStructureHandler.openEditStructure(structureInfo, structureID, entityPlayerMP);
        }
        else
        {
            throw ServerTranslations.commandException("commands.strucEdit.usage");
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, StructureRegistry.INSTANCE.ids());

        return Collections.emptyList();
    }
}
