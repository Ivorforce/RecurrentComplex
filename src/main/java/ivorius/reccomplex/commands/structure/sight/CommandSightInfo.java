/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.structure.sight;

import ivorius.mcopts.commands.SimpleCommand;
import ivorius.mcopts.commands.parameters.Parameters;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.commands.RCTextStyle;
import ivorius.reccomplex.nbt.NBTStorable;
import ivorius.reccomplex.utils.RCBlockAreas;
import ivorius.reccomplex.world.gen.feature.WorldStructureGenerationData;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureLoadContext;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.List;
import java.util.UUID;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandSightInfo extends SimpleCommand
{
    public CommandSightInfo()
    {
        super("info", expect -> expect.skip().descriptionU("id").required());
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        Parameters parameters = Parameters.of(args, expect()::declare);

        WorldStructureGenerationData generationData = WorldStructureGenerationData.get(sender.getEntityWorld());

        WorldStructureGenerationData.Entry entry = generationData.getEntry(UUID.fromString(parameters.get(0).require()));

        if (entry == null)
            throw RecurrentComplex.translations.commandException("commands.rcsightinfo.unknown");
        else
        {
            ITextComponent area = RCTextStyle.area(RCBlockAreas.from(entry.getBoundingBox()));
            ITextComponent sight = RCTextStyle.sight(entry, true);

            if (entry instanceof WorldStructureGenerationData.StructureEntry)
            {
                WorldStructureGenerationData.StructureEntry structureEntry = (WorldStructureGenerationData.StructureEntry) entry;
                sender.sendMessage(new TextComponentTranslation("commands.rcsightinfo.structure", RCTextStyle.structure(structureEntry.getStructureID()), area, RCTextStyle.copy(String.valueOf(structureEntry.getSeed())), sight));

                Structure structure = StructureRegistry.INSTANCE.get(structureEntry.getStructureID());
                if (structure != null)
                {
                    // TODO generateAsSource not accurate
                    NBTStorable instanceData = structure.loadInstanceData(new StructureLoadContext(structureEntry.getTransform(), entry.getBoundingBox(), false), structureEntry.getInstanceData(), RCConfig.getUniversalTransformer());
                    List<TextComponentBase> list = structure.instanceDataInfo(instanceData);
                    for (TextComponentBase s : list) sender.sendMessage(s);
                }
            }
            else
                sender.sendMessage(new TextComponentTranslation("commands.rcsightinfo.get", entry.description(), area, sight));
        }
    }
}
