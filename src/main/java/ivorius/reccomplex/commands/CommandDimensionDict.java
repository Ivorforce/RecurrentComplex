/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import ivorius.ivtoolkit.tools.IvGsonHelper;
import ivorius.reccomplex.dimensions.DimensionDictionary;
import ivorius.reccomplex.worldgen.genericStructures.BiomeSelector;
import joptsimple.internal.Strings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.DimensionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandDimensionDict extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "dimensiondict";
    }

    @Override
    public String getCommandUsage(ICommandSender commandSender)
    {
        return "commands.dimensiondict.usage";
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args)
    {
        if (args.length < 2)
            throw new WrongUsageException("commands.dimensiondict.usage");

        if (args[0].equals("get"))
        {
            int dimensionID = parseInt(commandSender, args[1]);

            Set<String> types = DimensionDictionary.getDimensionTypes(dimensionID);
            List<String> typeList = new ArrayList<>();
            typeList.addAll(types);
            commandSender.addChatMessage(new ChatComponentTranslation("commands.dimensiondict.get", dimensionID, Strings.join(typeList, ", ")));
        }
        else if (args[0].equals("list"))
        {
            TIntList typeDimensions = allDimensionsOfType(args[1]);
            String[] types = new String[typeDimensions.size()];
            for (int i = 0; i < typeDimensions.size(); i++)
                types[i] = String.valueOf(typeDimensions.get(i));

            commandSender.addChatMessage(new ChatComponentTranslation("commands.dimensiondict.list", args[1], Strings.join(types, ", ")));
        }
        else
            throw new WrongUsageException("commands.dimensiondict.usage");
    }

    private TIntList allDimensionsOfType(String type)
    {
        TIntList intList = new TIntArrayList();
        for (int d : DimensionManager.getIDs())
        {
            if (DimensionDictionary.dimensionMatchesType(d, type))
                intList.add(d);
        }
        return intList;
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
