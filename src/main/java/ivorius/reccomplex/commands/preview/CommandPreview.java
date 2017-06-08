/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.preview;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.capability.RCEntityInfo;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.mcopts.commands.CommandExpecting;
import ivorius.mcopts.commands.parameters.*;
import ivorius.mcopts.commands.parameters.expect.Expect;
import ivorius.reccomplex.operation.Operation;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandPreview extends CommandExpecting
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "preview";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public Expect expect()
    {
        return Parameters.expect()
                .any((Object[]) Operation.PreviewType.keys()).descriptionU("type").required();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        Parameters parameters = Parameters.of(args, expect()::declare);

        EntityPlayer player = getCommandSenderAsPlayer(commandSender);
        RCEntityInfo RCEntityInfo = RCCommands.getStructureEntityInfo(player, null);

        Operation.PreviewType previewType = parameters.get(0)
                .map(Operation.PreviewType::find, s -> RecurrentComplex.translations.commandException("commands.rcpreview.invalid"))
                .require();

        RCEntityInfo.setPreviewType(previewType);
        RCEntityInfo.sendPreviewTypeToClients(player);

        commandSender.sendMessage(RecurrentComplex.translations.format("commands.rcpreview.success", previewType.key));
    }
}
