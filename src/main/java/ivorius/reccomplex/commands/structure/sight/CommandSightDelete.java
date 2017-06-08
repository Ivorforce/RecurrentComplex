/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.structure.sight;

import ivorius.reccomplex.commands.parameters.*;
import ivorius.reccomplex.commands.parameters.expect.MCE;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.WorldStructureGenerationData;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandSightDelete extends CommandSplit
{
    public CommandSightDelete()
    {
        super("forget");

        add(new SimpleCommand("id", () -> Parameters.expect().skip().descriptionU("id").required())
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
            {
                Parameters parameters = Parameters.of(args, null);
                WorldStructureGenerationData generationData = WorldStructureGenerationData.get(sender.getEntityWorld());

                WorldStructureGenerationData.Entry entry = generationData.removeEntry(UUID.fromString(parameters.get(0).require()));

                if (entry == null)
                    throw ServerTranslations.commandException("commands.rcsightinfo.unknown");
                else
                    sender.sendMessage(ServerTranslations.format("commands.rcforget.success", entry.description()));
            }
        });

        add(new SimpleCommand("all", () -> Parameters.expect().then(MCE::xyz))
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
            {
                Parameters parameters = Parameters.of(args, null);
                WorldStructureGenerationData generationData = WorldStructureGenerationData.get(sender.getEntityWorld());

                BlockPos pos = parameters.get(0).to(MCP.pos_(sender.getPosition(), false)).require();

                List<WorldStructureGenerationData.Entry> entries = generationData.entriesAt(pos).collect(Collectors.toList());

                entries.forEach(e -> generationData.removeEntry(e.getUuid()));

                if (entries.size() == 1)
                    sender.sendMessage(ServerTranslations.format("commands.rcforget.success", entries.get(0).description()));
                else
                    sender.sendMessage(ServerTranslations.format("commands.rcforgetall.success", entries.size()));
            }
        });

        permitFor(2);
    }
}
