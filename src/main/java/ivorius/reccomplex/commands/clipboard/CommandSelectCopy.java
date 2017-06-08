/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.clipboard;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.ivtoolkit.world.MockWorld;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.capability.RCEntityInfo;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.commands.CommandVirtual;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.commands.RCTextStyle;
import ivorius.reccomplex.mcopts.commands.CommandExpecting;
import ivorius.reccomplex.mcopts.commands.parameters.expect.Expect;
import ivorius.reccomplex.mcopts.commands.parameters.Parameters;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectCopy extends CommandExpecting implements CommandVirtual
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "copy";
    }

    @Override
    public Expect expect()
    {
        return Parameters.expect();
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MockWorld world, ICommandSender sender, String[] args) throws CommandException
    {
        RCEntityInfo RCEntityInfo = RCCommands.getStructureEntityInfo(sender, null);

        SelectionOwner selectionOwner = RCCommands.getSelectionOwner(sender, null, true);
        RCCommands.assertSize(sender, selectionOwner);
        BlockArea area = selectionOwner.getSelection();

        IvWorldData worldData = IvWorldData.capture(world, area, true);

        RCEntityInfo.setWorldDataClipboard(worldData.createTagCompound());
        sender.sendMessage(ServerTranslations.format("commands.selectCopy.success", RCTextStyle.area(area)));
    }
}
