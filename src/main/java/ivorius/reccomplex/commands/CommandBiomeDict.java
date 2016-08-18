/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.tools.IvGsonHelper;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.json.RCGsonHelper;
import ivorius.reccomplex.structures.generic.matchers.BiomeMatcher;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandBiomeDict extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "biomedict";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender commandSender)
    {
        return ServerTranslations.usage("commands.biomedict.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length < 2)
            throw ServerTranslations.wrongUsageException("commands.biomedict.usage");

        switch (args[0])
        {
            case "get":
            {
                Set<Biome> biomes = BiomeMatcher.gatherAllBiomes();

                boolean didFindBiome = false;

                String biomeName = buildString(args, 1);

                for (Biome Biome : biomes)
                {
                    if (Biome.getBiomeName().equals(biomeName))
                    {
                        BiomeDictionary.Type[] types = BiomeDictionary.getTypesForBiome(Biome);
                        ITextComponent[] components = new ITextComponent[types.length];

                        for (int i = 0; i < types.length; i++)
                        {
                            BiomeDictionary.Type type = types[i];
                            components[i] = new TextComponentString(IvGsonHelper.serializedName(type));
                            components[i].getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                    String.format("/%s list %s", getCommandName(), type)));
                            components[i].getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    ServerTranslations.format("commands.biomedict.get.number", BiomeDictionary.getBiomesForType(type).length)));
                        }

                        commandSender.addChatMessage(ServerTranslations.format("commands.biomedict.get", biomeName,
                                new TextComponentTranslation(StringUtils.repeat("%s", ", ", components.length), components)));

                        didFindBiome = true;
                        break;
                    }
                }

                if (!didFindBiome)
                    commandSender.addChatMessage(ServerTranslations.format("commands.biomedict.nobiome", biomeName));
                break;
            }
            case "list":
            {
                BiomeDictionary.Type type = RCGsonHelper.enumForNameIgnoreCase(args[1], BiomeDictionary.Type.values());

                if (type != null)
                {
                    Biome[] biomes = BiomeDictionary.getBiomesForType(type);
                    ITextComponent[] components = new ITextComponent[biomes.length];

                    for (int i = 0; i < biomes.length; i++)
                    {
                        Biome biome = biomes[i];
                        components[i] = new TextComponentString(biome.getBiomeName());
                        components[i].getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                String.format("/%s get %s", getCommandName(), biome.getBiomeName())));
                        components[i].getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                ServerTranslations.format("commands.biomedict.list.number", BiomeDictionary.getTypesForBiome(biome).length)));
                    }

                    commandSender.addChatMessage(ServerTranslations.format("commands.biomedict.list", args[1],
                            new TextComponentTranslation(StringUtils.repeat("%s", ", ", components.length), components)));
                }
                else
                    commandSender.addChatMessage(ServerTranslations.format("commands.biomedict.notype", args[1]));
                break;
            }
            default:
                throw ServerTranslations.wrongUsageException("commands.biomedict.usage");
        }
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, "get", "list");

        if (args[0].equals("get"))
        {
            Set<Biome> biomes = BiomeMatcher.gatherAllBiomes();
            String[] biomeNames = new String[biomes.size()];

            int index = 0;
            for (Biome biome : biomes)
                biomeNames[index ++] = biome.getBiomeName();

            return getListOfStringsMatchingLastWord(args, biomeNames);
        }
        else if (args[0].equals("list"))
        {
            BiomeDictionary.Type[] types = BiomeDictionary.Type.values();
            String[] typeNames = new String[types.length];

            for (int i = 0; i < types.length; i++)
                typeNames[i] = IvGsonHelper.serializedName(types[i]);

            return getListOfStringsMatchingLastWord(args, typeNames);
        }

        return null;
    }
}
