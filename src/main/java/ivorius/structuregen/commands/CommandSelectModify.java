package ivorius.structuregen.commands;

import ivorius.structuregen.entities.StructureEntityInfo;
import ivorius.structuregen.ivtoolkit.BlockCoord;
import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.MathHelper;

/**
 * Created by lukas on 25.05.14.
 */
public abstract class CommandSelectModify extends CommandBase
{
    @Override
    public void processCommand(ICommandSender commandSender, String[] args)
    {
        EntityPlayerMP entityPlayerMP = getCommandSenderAsPlayer(commandSender);

        if (entityPlayerMP != null)
        {
            StructureEntityInfo structureEntityInfo = StructureEntityInfo.getStructureEntityInfo(entityPlayerMP);

            if (structureEntityInfo != null)
            {
                if (structureEntityInfo.hasValidSelection())
                {
                    processCommandSelection(entityPlayerMP, structureEntityInfo, structureEntityInfo.selectedPoint1, structureEntityInfo.selectedPoint2, args);
                }
                else
                {
                    throw new WrongUsageException("commands.selectModify.noSelection");
                }
            }
        }
        else
        {
            throw new WrongUsageException("commands.selectModify.noPlayer");
        }
    }

    public static Block getBlock(String blockID)
    {
        Block block = Block.getBlockFromName(blockID);

        if (block == null)
            throw new WrongUsageException("commands.selectModify.invalidBlock", blockID);

        return block;
    }

    public static int[] getMetadatas(String arg)
    {
        try
        {
            String[] strings = arg.split(",");
            int[] ints = new int[strings.length];

            for (int i = 0; i < strings.length; i++)
            {
                ints[i] = Integer.valueOf(strings[i]);
            }

            return ints;
        }
        catch (Exception ex)
        {
            throw new WrongUsageException("commands.selectModify.invalidMetadata", arg);
        }
    }

    public abstract void processCommandSelection(EntityPlayerMP player, StructureEntityInfo structureEntityInfo, BlockCoord point1, BlockCoord point2, String[] args);
}
