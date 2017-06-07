/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.structure.sight;

import ivorius.reccomplex.commands.RCTextStyle;
import ivorius.reccomplex.commands.parameters.*;
import ivorius.reccomplex.utils.ServerTranslations;
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

    public CommandSightCheck(String name)
    {
        this.name = name;
    }

    @Override
    public String getCommandName()
    {
        return name;
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args, expect()::declare);
        World world = commandSender.getEntityWorld();

        BlockPos pos = parameters.get(0).pos(commandSender.getPosition(), false).require();

        List<WorldStructureGenerationData.Entry> entries = WorldStructureGenerationData.get(world).entriesAt(pos).collect(Collectors.toCollection(ArrayList::new));
        if (entries.size() > 0)
            commandSender.sendMessage(ServerTranslations.format(new ArrayList<ITextComponent>().size() > 1 ? "commands.whatisthis.many" : "commands.whatisthis.one",
                    ServerTranslations.join(entries.stream().map(RCTextStyle::sight).collect(Collectors.toList()))));
        else
            commandSender.addChatMessage(ServerTranslations.format("commands.whatisthis.none"));
    }

    @Override
    public Expect<?> expect()
    {
        return RCExpect.expectRC()
                .xyz();
    }
}
