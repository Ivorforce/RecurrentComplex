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
import ivorius.mcopts.commands.CommandExpecting;
import ivorius.mcopts.commands.parameters.*;
import ivorius.mcopts.commands.parameters.expect.Expect;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandNaturalAll extends CommandExpecting implements CommandVirtual
{
    @Override
    public String getName()
    {
        return "smart";
    }

    @Override
    public void expect(Expect expect)
    {
        expect
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
        Parameters parameters = Parameters.of(args, expect()::declare);

        SelectionOwner selectionOwner = RCCommands.getSelectionOwner(sender, null, true);
        RCCommands.assertSize(sender, selectionOwner);
        BlockArea area = selectionOwner.getSelection();

        double expandFloor = parameters.get("floor-expansion").to(NaP::asDouble).optional().orElse(1.);
        int floorDistance = parameters.get("space-distance-to-floor").to(NaP::asInt).optional().orElse(2) + 1;
        int maxClosedSides = parameters.get("space-max-closed-sides").to(NaP::asInt).optional().orElse(3);

        CommandNaturalFloor.placeNaturalFloor(world, area, expandFloor);
        CommandNaturalSpace.placeNaturalAir(world, area, floorDistance, maxClosedSides);
    }
}
