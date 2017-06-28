/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.structure.sight;

import ivorius.mcopts.commands.CommandExpecting;
import ivorius.mcopts.commands.parameters.MCP;
import ivorius.mcopts.commands.parameters.Parameters;
import ivorius.mcopts.commands.parameters.expect.Expect;
import ivorius.mcopts.commands.parameters.expect.MCE;
import ivorius.mcopts.translation.ServerTranslations;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.commands.CommandVanilla;
import ivorius.reccomplex.commands.RCTextStyle;
import ivorius.reccomplex.world.gen.feature.WorldStructureGenerationData;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandSightCheck extends CommandExpecting
{
    private String name;
    public boolean checkAll;

    public CommandSightCheck(String name, boolean checkAll)
    {
        this.name = name;
        this.checkAll = checkAll;
    }

    public static ITextComponent list(List<ITextComponent> names)
    {
        if (names.size() == 0)
            return RecurrentComplex.translations.format("commands.whatisthis.none");

        return RecurrentComplex.translations.format(names.size() > 1 ? "commands.whatisthis.many" : "commands.whatisthis.one",
                ServerTranslations.join(names));
    }

    @Override
    public String getName()
    {
        return name;
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        Parameters parameters = Parameters.of(args, expect()::declare);
        World world = sender.getEntityWorld();

        BlockPos pos = parameters.get(0).to(MCP.pos(sender.getPosition(), false)).require();

        List<ITextComponent> names = WorldStructureGenerationData.get(world).entriesAt(pos)
                .map(RCTextStyle::sight)
                .collect(Collectors.toCollection(ArrayList::new));

        if (checkAll)
            names.addAll(CommandVanilla.sightNames(world, pos));

        sender.sendMessage(list(names));
    }

    @Override
    public void expect(Expect expect)
    {
        expect.then(MCE::xyz);
    }
}
