/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.info;

import com.google.common.collect.Lists;
import ivorius.ivtoolkit.tools.IvGsonHelper;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.commands.RCTextStyle;
import ivorius.reccomplex.commands.parameters.*;
import ivorius.reccomplex.commands.rcparameters.RCExpect;
import ivorius.reccomplex.commands.structure.CommandSearchStructure;
import ivorius.reccomplex.utils.ServerTranslations;
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
        super(RCConfig.commandPrefix + "biome");

        add(new SimpleCommand("search", () -> RCExpect.expectRC().skip().descriptionU("terms").required())
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
            {
                Parameters blueprint = Parameters.of(args, null);
                Parameters blueprint1 = blueprint;
                Parameters blueprint2 = blueprint1;
                Parameters parameters = new Parameters(blueprint2);

                List<String> terms = parameters.get(0).varargsList().require();

                CommandSearchStructure.postResultMessage(sender,
                        RCTextStyle::biome,
                        CommandSearchStructure.search(Biome.REGISTRY.getKeys(), loc -> CommandSearchStructure.searchRank(terms, keywords(loc, Biome.REGISTRY.getObject(loc))))
                );
            }
        });

        add(new SimpleCommand("types", () -> RCExpect.expectRC().biome().required())
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
            {
                Parameters blueprint = Parameters.of(args, null);
                Parameters blueprint1 = blueprint;
                Parameters blueprint2 = blueprint1;
                Parameters parameters = new Parameters(blueprint2);

                Biome biome = parameters.get(0).to(MCP::biome).require();

                sender.sendMessage(ServerTranslations.format("commands.biomedict.get", RCTextStyle.biome(biome),
                        ServerTranslations.join(Lists.newArrayList(BiomeDictionary.getTypes(biome)).stream()
                                .map(RCTextStyle::biomeType).toArray())
                ));
            }
        });

        add(new SimpleCommand("list", () -> RCExpect.expectRC().biomeType().required())
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
            {
                Parameters blueprint = Parameters.of(args, null);
                Parameters blueprint1 = blueprint;
                Parameters blueprint2 = blueprint1;
                Parameters parameters = new Parameters(blueprint2);

                BiomeDictionary.Type type = parameters.get(0).to(MCP::biomeDictionaryType).require();

                sender.sendMessage(ServerTranslations.format("commands.biomedict.list", RCTextStyle.biomeType(type),
                        ServerTranslations.join(Lists.newArrayList(BiomeDictionary.getBiomes(type))
                                .stream().map(RCTextStyle::biome).toArray())
                ));
            }
        });

        permitFor(2);
    }

    @Nonnull
    public static Collection<String> keywords(ResourceLocation id, Biome biome)
    {
        return Arrays.asList(id.toString(), biome.getBiomeName());
    }
}
