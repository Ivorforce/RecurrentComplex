/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.former;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.ivtoolkit.world.MockWorld;
import ivorius.reccomplex.commands.CommandVirtual;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.commands.parameters.RCParameters;
import net.minecraft.command.CommandException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.ICommandSender;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandNaturalAll extends CommandVirtual
{
    @Override
    public String getName()
    {
        return "smart";
    }

    @Override
    public String getUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.selectNatural.usage");
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        return RCExpect.expectRC()
                .named("floor-expansion").any("0", "1", "2")
                .named("space-distance-to-floor").any("3", "2", "1")
                .named("space-max-closed-sides").any("3", "4", "5")
                .get(server, sender, args, pos);
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MockWorld world, ICommandSender commandSender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args, null);

        SelectionOwner selectionOwner = RCCommands.getSelectionOwner(commandSender, null, true);
        RCCommands.assertSize(commandSender, selectionOwner);
        BlockArea area = selectionOwner.getSelection();

        double expandFloor = parameters.get("floor-expansion").doubleAt(0).optional().orElse(1.);
        int floorDistance = parameters.get("space-distance-to-floor").intAt(0).optional().orElse(0) + 1;
        int maxClosedSides = parameters.get("space-max-closed-sides").intAt(1).optional().orElse(3);

        CommandNaturalFloor.placeNaturalFloor(world, area, expandFloor);
        CommandNaturalSpace.placeNaturalAir(world, area, 3, 3);
    }
}
