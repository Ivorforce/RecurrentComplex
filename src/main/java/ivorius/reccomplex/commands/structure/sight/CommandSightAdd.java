/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.structure.sight;

import ivorius.ivtoolkit.blocks.BlockAreas;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.commands.parameters.expect.RCE;
import ivorius.mcopts.commands.CommandExpecting;
import ivorius.mcopts.commands.parameters.*;
import ivorius.mcopts.commands.parameters.expect.Expect;
import ivorius.reccomplex.world.gen.feature.WorldStructureGenerationData;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSightAdd extends CommandExpecting
{
    @Override
    public String getCommandName()
    {
        return "remember";
    }

    @Override
    public void expect(Expect expect)
    {
        expect.then(RCE::randomString).descriptionU("name").required().repeat();
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        Parameters parameters = Parameters.of(args, expect()::declare);

        WorldStructureGenerationData generationData = WorldStructureGenerationData.get(commandSender.getEntityWorld());
        SelectionOwner owner = RCCommands.getSelectionOwner(commandSender, null, true);

        String name = parameters.get(0).rest(NaP.join()).require();

        generationData.addEntry(WorldStructureGenerationData.CustomEntry.from(name, BlockAreas.toBoundingBox(owner.getSelection())));
        commandSender.sendMessage(RecurrentComplex.translations.format("commands.rcremember.success", name));
    }
}
