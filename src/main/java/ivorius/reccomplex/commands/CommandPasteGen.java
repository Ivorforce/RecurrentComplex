/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.operation.OperationRegistry;
import ivorius.reccomplex.structures.OperationGenerateStructure;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandPasteGen extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "pastegen";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.strucPasteGen.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        EntityPlayerMP entityPlayerMP = getCommandSenderAsPlayer(commandSender);
        StructureEntityInfo structureEntityInfo = RCCommands.getStructureEntityInfo(entityPlayerMP);

        NBTTagCompound worldData = structureEntityInfo.getWorldDataClipboard();

        if (worldData != null)
        {
            BlockPos coord = RCCommands.tryParseBlockPos(commandSender, args, 0, false);
            AxisAlignedTransform2D transform = RCCommands.tryParseTransform(args, 3);

            GenericStructureInfo structureInfo = GenericStructureInfo.createDefaultStructure();
            structureInfo.worldDataCompound = worldData;

            OperationRegistry.queueOperation(new OperationGenerateStructure(structureInfo, null, transform, coord, false), commandSender);
        }
        else
        {
            throw ServerTranslations.commandException("commands.strucPaste.noClipboard");
        }
    }

    @Nonnull
    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1 || args.length == 2 || args.length == 3)
            return getTabCompletionCoordinate(args, args.length, pos);
        else if (args.length == 4 || args.length == 5)
            return RCCommands.completeTransform(args, args.length - 4);

        return Collections.emptyList();
    }
}
