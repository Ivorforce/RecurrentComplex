/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.former;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.world.MockWorld;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.commands.CommandVirtual;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.commands.parameters.CommandExpecting;
import ivorius.reccomplex.commands.parameters.Expect;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.commands.parameters.RCParameters;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandNaturalAll extends CommandExpecting implements CommandVirtual
{
    @Override
    public String getCommandName()
    {
        return "smart";
    }

    @Override
    public Expect<?> expect()
    {
        return RCExpect.expectRC()
                .named("floor-expansion", "e").any("0", "1", "2")
                .named("space-distance-to-floor", "f").any("3", "2", "1")
                .named("space-max-closed-sides", "s").any("3", "4", "5");
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MockWorld world, ICommandSender sender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args, expect()::declare);

        SelectionOwner selectionOwner = RCCommands.getSelectionOwner(sender, null, true);
        RCCommands.assertSize(sender, selectionOwner);
        BlockArea area = selectionOwner.getSelection();

        double expandFloor = parameters.get("floor-expansion").asDouble().optional().orElse(1.);
        int floorDistance = parameters.get("space-distance-to-floor").asInt().optional().orElse(0) + 1;
        int maxClosedSides = parameters.get("space-max-closed-sides").asInt().optional().orElse(3);

        CommandNaturalFloor.placeNaturalFloor(world, area, expandFloor);
        CommandNaturalSpace.placeNaturalAir(world, area, 3, 3);
    }
}
