/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.preview;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.capability.RCEntityInfo;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.commands.parameters.Expect;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.commands.parameters.RCParameters;
import ivorius.reccomplex.commands.parameters.CommandExpecting;
import ivorius.reccomplex.operation.Operation;
import ivorius.reccomplex.utils.ServerTranslations;
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
    public Expect<?> expect()
    {
        return RCExpect.expectRC()
                .any((Object[]) Operation.PreviewType.keys()).requiredU("type");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args, expect()::declare);

        EntityPlayer player = getCommandSenderAsPlayer(commandSender);
        RCEntityInfo RCEntityInfo = RCCommands.getStructureEntityInfo(player, null);

        Operation.PreviewType previewType = parameters.get().first()
                .map(Operation.PreviewType::find, s -> ServerTranslations.commandException("commands.rcpreview.invalid"))
                .require();

        RCEntityInfo.setPreviewType(previewType);
        RCEntityInfo.sendPreviewTypeToClients(player);

        commandSender.sendMessage(ServerTranslations.format("commands.rcpreview.success", previewType.key));
    }
}
