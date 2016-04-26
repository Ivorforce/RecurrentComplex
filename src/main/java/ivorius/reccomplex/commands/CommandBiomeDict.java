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
import joptsimple.internal.Strings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import org.apache.commons.lang3.StringUtils;

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
    public void processCommand(ICommandSender commandSender, String[] args)
    {
        if (args.length < 2)
            throw ServerTranslations.wrongUsageException("commands.biomedict.usage");

        switch (args[0])
        {
            case "get":
            {
                Set<BiomeGenBase> biomes = BiomeMatcher.gatherAllBiomes();

                boolean didFindBiome = false;

                String biomeName = func_147178_a(commandSender, args, 1).getUnformattedText();

                for (BiomeGenBase biomeGenBase : biomes)
                {
                    if (biomeGenBase.biomeName.equals(biomeName))
                    {
                        BiomeDictionary.Type[] types = BiomeDictionary.getTypesForBiome(biomeGenBase);
                        IChatComponent[] components = new IChatComponent[types.length];

                        for (int i = 0; i < types.length; i++)
                        {
                            BiomeDictionary.Type type = types[i];
                            components[i] = new ChatComponentText(IvGsonHelper.serializedName(type));
                            components[i].getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                    String.format("/%s list %s", getCommandName(), type)));
                            components[i].getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    ServerTranslations.format("commands.biomedict.get.number", BiomeDictionary.getBiomesForType(type).length)));
                        }

                        commandSender.addChatMessage(ServerTranslations.format("commands.biomedict.get", biomeName,
                                new ChatComponentTranslation(StringUtils.repeat("%s", ", ", components.length), components)));

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
                    BiomeGenBase[] biomes = BiomeDictionary.getBiomesForType(type);
                    IChatComponent[] components = new IChatComponent[biomes.length];

                    for (int i = 0; i < biomes.length; i++)
                    {
                        BiomeGenBase biome = biomes[i];
                        components[i] = new ChatComponentText(biome.biomeName);
                        components[i].getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                String.format("/%s get %s", getCommandName(), biome.biomeName)));
                        components[i].getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                ServerTranslations.format("commands.biomedict.list.number", BiomeDictionary.getTypesForBiome(biome).length)));
                    }

                    commandSender.addChatMessage(ServerTranslations.format("commands.biomedict.list", args[1],
                            new ChatComponentTranslation(StringUtils.repeat("%s", ", ", components.length), components)));
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
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, "get", "list");

        if (args[0].equals("get"))
        {
            Set<BiomeGenBase> biomes = BiomeMatcher.gatherAllBiomes();
            String[] biomeNames = new String[biomes.size()];

            int index = 0;
            for (BiomeGenBase biome : biomes)
                biomeNames[index ++] = biome.biomeName;

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
