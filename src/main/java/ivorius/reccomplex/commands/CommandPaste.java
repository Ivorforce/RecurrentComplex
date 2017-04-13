/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.capability.RCEntityInfo;
import ivorius.reccomplex.operation.OperationRegistry;
import ivorius.reccomplex.world.gen.feature.structure.OperationGenerateStructure;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandPaste extends CommandBase
{
    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "paste";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.strucPaste.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        EntityPlayerMP entityPlayerMP = getCommandSenderAsPlayer(commandSender);
        RCEntityInfo RCEntityInfo = RCCommands.getStructureEntityInfo(entityPlayerMP, null);

        NBTTagCompound worldData = RCEntityInfo.getWorldDataClipboard();

        if (worldData != null)
        {
            BlockPos coord = RCCommands.tryParseBlockPos(commandSender, args, 0, false);

            GenericStructure structureInfo = GenericStructure.createDefaultStructure();
            structureInfo.worldDataCompound = worldData;

            AxisAlignedTransform2D transform = RCCommands.tryParseTransform(args, 3);

            // TODO Generate with generation info?
            OperationRegistry.queueOperation(new OperationGenerateStructure(structureInfo, null, transform, coord, true).prepare((WorldServer) commandSender.getEntityWorld()), commandSender);
        }
        else
        {
            throw ServerTranslations.commandException("commands.strucPaste.noClipboard");
        }
    }

    @Nonnull
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1 || args.length == 2 || args.length == 3)
            return getTabCompletionCoordinate(args, args.length, pos);
        else if (args.length == 4 || args.length == 5)
            return RCCommands.completeTransform(args, args.length - 4);

        return Collections.emptyList();
    }
}
