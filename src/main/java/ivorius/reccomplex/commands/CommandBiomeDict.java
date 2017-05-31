/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import com.google.common.collect.Lists;
import ivorius.ivtoolkit.tools.IvGsonHelper;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.commands.parameters.RCParameters;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.utils.accessor.RCAccessorBiomeDictionary;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
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
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "biome";
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
        RCParameters parameters = RCParameters.of(args);

        switch (parameters.get().first().require())
        {
            case "search":
            {
                List<String> terms = parameters.get().move(1).varargsList();

                CommandSearchStructure.postResultMessage(commandSender,
                        RCTextStyle::biome,
                        CommandSearchStructure.search(Biome.REGISTRY.getKeys(), loc -> CommandSearchStructure.searchRank(terms, keywords(loc, Biome.REGISTRY.getObject(loc))))
                );
                break;
            }
            case "types":
            {
                String biomeID = parameters.get().at(1).require();
                Biome biome = parameters.mc().biome().require();

                commandSender.sendMessage(ServerTranslations.format("commands.biomedict.get", biomeID,
                        ServerTranslations.join(Lists.newArrayList(BiomeDictionary.getTypes(biome)).stream()
                                .map(RCTextStyle::biomeType).toArray())
                ));

                break;
            }
            case "list":
            {
                BiomeDictionary.Type type = parameters.mc().move(1).biomeDictionaryType().require();

                commandSender.sendMessage(ServerTranslations.format("commands.biomedict.list", args[1],
                        ServerTranslations.join(Lists.newArrayList(BiomeDictionary.getBiomes(type))
                                .stream().map(RCTextStyle::biome).toArray())
                ));
                break;
            }
            default:
                throw ServerTranslations.wrongUsageException("commands.biomedict.usage");
        }
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        RCExpect<?> expect = RCExpect.startRC()
                .any("types", "list", "search");

        switch (args[0])
        {
            case "types":
                expect.biome();
                break;
            case "list":
                expect.next(RCAccessorBiomeDictionary.getMap().keySet());
                break;
            default:
                expect.skip(1);
                break;
        }

        return expect.get(server, sender, args, pos);
    }
}
