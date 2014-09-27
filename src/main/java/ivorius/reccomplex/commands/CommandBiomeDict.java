/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.tools.IvGsonHelper;
import ivorius.reccomplex.json.RCGsonHelper;
import ivorius.reccomplex.worldgen.genericStructures.BiomeGenerationInfo;
import ivorius.reccomplex.worldgen.genericStructures.BiomeSelector;
import joptsimple.internal.Strings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;

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
        return "biomedict";
    }

    @Override
    public String getCommandUsage(ICommandSender commandSender)
    {
        return "commands.biomedict.usage";
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args)
    {
        if (args.length < 2)
            throw new WrongUsageException("commands.biomedict.usage");

        if (args[0].equals("get"))
        {
            Set<BiomeGenBase> biomes = BiomeSelector.gatherAllBiomes();

            boolean didFindBiome = false;

            String[] biomeNameArgs = new String[args.length - 1];
            System.arraycopy(args, 1, biomeNameArgs, 0, biomeNameArgs.length);
            String biomeName = Strings.join(biomeNameArgs, " ");

            for (BiomeGenBase biomeGenBase : biomes)
            {
                if (biomeGenBase.biomeName.equals(biomeName))
                {
                    BiomeDictionary.Type[] types = BiomeDictionary.getTypesForBiome(biomeGenBase);
                    String[] typeNames = new String[types.length];

                    for (int i = 0; i < types.length; i++)
                        typeNames[i] = IvGsonHelper.serializedName(types[i]);

                    commandSender.addChatMessage(new ChatComponentTranslation("commands.biomedict.get", biomeName, Strings.join(typeNames, ", ")));

                    didFindBiome = true;
                    break;
                }
            }

            if (!didFindBiome)
                commandSender.addChatMessage(new ChatComponentTranslation("commands.biomedict.nobiome", biomeName));
        }
        else if (args[0].equals("list"))
        {
            BiomeDictionary.Type type = RCGsonHelper.enumForNameIgnoreCase(args[1], BiomeDictionary.Type.values());

            if (type != null)
            {
                BiomeGenBase[] biomes = BiomeDictionary.getBiomesForType(type);
                String[] biomeNames = new String[biomes.length];

                for (int i = 0; i < biomes.length; i++)
                    biomeNames[i] = biomes[i].biomeName;

                commandSender.addChatMessage(new ChatComponentTranslation("commands.biomedict.list", args[1], Strings.join(biomeNames, ", ")));
            }
            else
                commandSender.addChatMessage(new ChatComponentTranslation("commands.biomedict.notype", args[1]));
        }
        else
            throw new WrongUsageException("commands.biomedict.usage");
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, "get", "list");

        if (args[0].equals("get"))
        {
            Set<BiomeGenBase> biomes = BiomeSelector.gatherAllBiomes();
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
