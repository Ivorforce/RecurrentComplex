/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import net.minecraft.command.CommandException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.operation.OperationRegistry;
import ivorius.reccomplex.structures.OperationGenerateStructure;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.StructureInfo;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.worldgen.StructureGenerator;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandImportStructure extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "import";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.strucImport.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length <= 0)
        {
            throw ServerTranslations.wrongUsageException("commands.strucImport.usage");
        }

        String structureName = args[0];
        StructureInfo structureInfo = StructureRegistry.INSTANCE.getStructure(structureName);
        World world = commandSender.getEntityWorld();

        if (structureInfo == null)
        {
            throw ServerTranslations.commandException("commands.strucImport.noStructure", structureName);
        }

        BlockPos coord;

        if (args.length >= 4)
            coord = parseBlockPos(commandSender, args, 1, false);
        else
            coord = commandSender.getPosition();

        AxisAlignedTransform2D transform = AxisAlignedTransform2D.ORIGINAL;

        if (structureInfo instanceof GenericStructureInfo)
            OperationRegistry.queueOperation(new OperationGenerateStructure((GenericStructureInfo) structureInfo, transform, coord, true), commandSender);
        else
            StructureGenerator.directly(structureInfo, StructureSpawnContext.complete((WorldServer) world, world.rand, transform, coord, structureInfo, 0, true));
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, StructureRegistry.INSTANCE.allStructureIDs());
        else if (args.length == 2 || args.length == 3 || args.length == 4)
        {
            return getListOfStringsMatchingLastWord(args, "~");
        }

        return null;
    }
}
