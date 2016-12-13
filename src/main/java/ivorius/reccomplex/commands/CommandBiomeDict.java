/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import com.google.common.collect.Lists;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.utils.BiomeDictionaryAccessor;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandBiomeDict extends CommandBase
{
    @Nonnull
    public static Collection<String> keywords(ResourceLocation id, Biome biome)
    {
        return Arrays.asList(id.toString(), biome.getBiomeName());
    }

    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "biome";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender commandSender)
    {
        return ServerTranslations.usage("commands.biomedict.usage");
    }

    public TextComponentString createBiomeTextComponent(ResourceLocation id)
    {
        TextComponentString comp = new TextComponentString(id.toString());
        comp.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                String.format("/%s types %s", getName(), id.toString())));
        comp.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                ServerTranslations.get("commands.rcsearch.lookup")));
        comp.getStyle().setColor(TextFormatting.BLUE);
        return comp;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length < 2)
            throw ServerTranslations.wrongUsageException("commands.biomedict.usage");

        switch (args[0])
        {
            case "search":
            {
                CommandSearchStructure.outputSearch(commandSender, Biome.REGISTRY.getKeys(),
                        loc -> CommandSearchStructure.searchRank(Arrays.asList(args), keywords(loc, Biome.REGISTRY.getObject(loc))),
                        this::createBiomeTextComponent
                );
                break;
            }
            case "types":
            {
                String biomeID = args[1];
                Biome biome = Biome.REGISTRY.getObject(new ResourceLocation(biomeID));

                if (biome != null)
                {
                    List<BiomeDictionary.Type> types = Lists.newArrayList(BiomeDictionary.getTypes(biome));
                    ITextComponent[] components = new ITextComponent[types.size()];

                    for (int i = 0; i < types.size(); i++)
                        components[i] = typeTextComponent(types.get(i));

                    commandSender.sendMessage(ServerTranslations.format("commands.biomedict.get", biomeID,
                            ServerTranslations.join((Object[]) components)));
                }
                else
                    commandSender.sendMessage(ServerTranslations.format("commands.biomedict.nobiome", biomeID));
                break;
            }
            case "list":
            {
                BiomeDictionary.Type type = BiomeDictionaryAccessor.getTypeWeak(args[1]);

                if (type != null)
                {
                    List<Biome> biomes = Lists.newArrayList(BiomeDictionary.getBiomes(type));
                    ITextComponent[] components = new ITextComponent[biomes.size()];

                    for (int i = 0; i < biomes.size(); i++)
                    {
                        Biome biome = biomes.get(i);
                        String biomeID = Biome.REGISTRY.getNameForObject(biome).toString();
                        ITextComponent component = biomeTextComponent(biome, biomeID);
                        components[i] = component;
                    }

                    commandSender.sendMessage(ServerTranslations.format("commands.biomedict.list", args[1],
                            ServerTranslations.join((Object[]) components)));
                }
                else
                    commandSender.sendMessage(ServerTranslations.format("commands.biomedict.notype", args[1]));
                break;
            }
            default:
                throw ServerTranslations.wrongUsageException("commands.biomedict.usage");
        }
    }

    @Nonnull
    public ITextComponent typeTextComponent(BiomeDictionary.Type type)
    {
        ITextComponent component = new TextComponentString(type.getName());
        Style style = component.getStyle();
        style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                String.format("/%s list %s", getName(), type)));
        style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                ServerTranslations.format("commands.biomedict.get.number", BiomeDictionary.getBiomes(type).size())));
        style.setColor(TextFormatting.AQUA);
        return component;
    }

    @Nonnull
    public ITextComponent biomeTextComponent(Biome biome, String biomeID)
    {
        ITextComponent component = new TextComponentString(biomeID);
        Style style = component.getStyle();
        style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                String.format("/%s types %s", getName(), biomeID)));
        style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                ServerTranslations.format("commands.biomedict.list.number", BiomeDictionary.getTypes(biome).size())));
        style.setColor(TextFormatting.AQUA);
        return component;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, "types", "list", "search");

        if (args[0].equals("types"))
        {
            return getListOfStringsMatchingLastWord(args, Biome.REGISTRY.getKeys());
        }
        else if (args[0].equals("list"))
        {
            return getListOfStringsMatchingLastWord(args, BiomeDictionaryAccessor.getMap().keySet());
        }

        return Collections.emptyList();
    }
}
