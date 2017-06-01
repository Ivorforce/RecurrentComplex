/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import com.google.common.collect.Lists;
import ivorius.ivtoolkit.tools.IvGsonHelper;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.commands.parameters.*;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.utils.accessor.RCAccessorBiomeDictionary;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandBiomeDict extends CommandSplit
{
    public CommandBiomeDict()
    {
        add(new SimpleCommand("search", "<terms>", Expect::start)
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
            {
                RCParameters parameters = RCParameters.of(args);

                List<String> terms = parameters.get().varargsList();

                CommandSearchStructure.postResultMessage(sender,
                        RCTextStyle::biome,
                        CommandSearchStructure.search(Biome.REGISTRY.getKeys(), loc -> CommandSearchStructure.searchRank(terms, keywords(loc, Biome.REGISTRY.getObject(loc))))
                );
            }
        });

        add(new SimpleCommand("types", "<biome>", () -> RCExpect.startRC().biome())
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
            {
                RCParameters parameters = RCParameters.of(args);

                String biomeID = parameters.get().first().require();
                Biome biome = parameters.mc().biome().require();

                sender.sendMessage(ServerTranslations.format("commands.biomedict.get", RCTextStyle.biome(biome),
                        ServerTranslations.join(Lists.newArrayList(BiomeDictionary.getTypes(biome)).stream()
                                .map(RCTextStyle::biomeType).toArray())
                ));
            }
        });

        add(new SimpleCommand("list", "<biome type", () -> RCExpect.startRC().next(RCAccessorBiomeDictionary.getMap().keySet()))
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
            {
                RCParameters parameters = RCParameters.of(args);

                String typeID = parameters.get().first().require();
                BiomeDictionary.Type type = parameters.mc().biomeDictionaryType().require();

                sender.sendMessage(ServerTranslations.format("commands.biomedict.list", RCTextStyle.biomeType(type),
                        ServerTranslations.join(Lists.newArrayList(BiomeDictionary.getBiomes(type))
                                .stream().map(RCTextStyle::biome).toArray())
                ));
            }
        });
    }

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
}
