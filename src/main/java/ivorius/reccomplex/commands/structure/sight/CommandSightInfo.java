/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.structure.sight;

import ivorius.reccomplex.commands.RCTextStyle;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.commands.parameters.RCParameters;
import ivorius.reccomplex.commands.parameters.SimpleCommand;
import ivorius.reccomplex.utils.RCBlockAreas;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.WorldStructureGenerationData;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.UUID;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandSightInfo extends SimpleCommand
{
    public CommandSightInfo()
    {
        super("info",  () -> RCExpect.expectRC().skip(1).requiredU("id"));
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args);
        WorldStructureGenerationData generationData = WorldStructureGenerationData.get(sender.getEntityWorld());

        WorldStructureGenerationData.Entry entry = generationData.getEntry(UUID.fromString(parameters.get().first().require()));

        if (entry == null)
            throw ServerTranslations.commandException("commands.rcsightinfo.unknown");
        else
        {
            ITextComponent area = RCTextStyle.area(RCBlockAreas.from(entry.getBoundingBox()));
            ITextComponent sight = RCTextStyle.sight(entry, true);

            if (entry instanceof WorldStructureGenerationData.StructureEntry)
            {
                WorldStructureGenerationData.StructureEntry structureEntry = (WorldStructureGenerationData.StructureEntry) entry;
                sender.sendMessage(new TextComponentTranslation("commands.rcsightinfo.structure", RCTextStyle.structure(structureEntry.getStructureID()), area, RCTextStyle.copy(String.valueOf(structureEntry.getSeed())), sight));
            }
            else
                sender.sendMessage(new TextComponentTranslation("commands.rcsightinfo.get", entry.description(), area, sight));
        }
    }
}
