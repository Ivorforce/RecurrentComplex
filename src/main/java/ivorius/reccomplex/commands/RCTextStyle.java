/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.Repository;
import ivorius.reccomplex.dimensions.DimensionDictionary;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.utils.RCStrings;
import ivorius.reccomplex.utils.ServerTranslations;
import joptsimple.internal.Strings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.DimensionManager;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by lukas on 20.03.17.
 */
public class RCTextStyle
{
    @Nonnull
    protected static ITextComponent path(ResourceDirectory directory, String... path)
    {
        ITextComponent pathComponent = new TextComponentString(String.format("%s%s%s", directory, path.length > 0 ? "/" : "", Strings.join(path, "/")));
        pathComponent.getStyle().setColor(TextFormatting.GOLD);
        pathComponent.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Visit File")));
        pathComponent.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, directory.toFile().getAbsolutePath()));
        return pathComponent;
    }

    @Nonnull
    protected static ITextComponent submit(String id)
    {
        ITextComponent submit = ServerTranslations.get("reccomplex.save.submit");
        submit.getStyle().setColor(TextFormatting.AQUA);
        submit.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ServerTranslations.get("reccomplex.save.submit.hover")));
        submit.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Repository.submitURL(id)));
        return submit;
    }

    @Nonnull
    protected static ITextComponent visitFile(String id)
    {
        ITextComponent submit = ServerTranslations.get("reccomplex.save.submit");
        submit.getStyle().setColor(TextFormatting.AQUA);
        submit.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ServerTranslations.get("reccomplex.save.submit.hover")));
        submit.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Repository.submitURL(id)));
        return submit;
    }

    public static ITextComponent users(@Nullable String names)
    {
        boolean has = names != null && !names.trim().isEmpty();

        if (!has)
            return ServerTranslations.format("commands.rclookup.reply.noauthor");

        TextComponentString component = new TextComponentString(RCStrings.abbreviateFormatted(names, 30));

        component.getStyle().setColor(TextFormatting.GOLD);
        component.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(names)));

        return component;
    }

    public static ITextComponent structure(String id)
    {
        TextComponentString comp = new TextComponentString(id);
        comp.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                String.format("/%s %s", RCCommands.lookup.getCommandName(), id)));
        comp.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                ServerTranslations.get("commands.rcsearch.lookup")));
        comp.getStyle().setColor(TextFormatting.AQUA);
        return comp;
    }

    public static TextComponentString biome(ResourceLocation id)
    {
        return (TextComponentString) biome(Biome.REGISTRY.getObject(id));
    }

    @Nonnull
    public static ITextComponent biome(Biome biome)
    {
        ITextComponent component = new TextComponentString(biome.getBiomeName());
        Style style = component.getStyle();
        style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                String.format("/%s types %s", RCCommands.biomeDict.getCommandName(), biome.getRegistryName())));
        style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                ServerTranslations.format("commands.biomedict.list.number", BiomeDictionary.getTypesForBiome(biome).length)));
        style.setColor(TextFormatting.AQUA);
        return component;
    }

    @Nonnull
    public static ITextComponent biomeType(BiomeDictionary.Type type)
    {
        ITextComponent component = new TextComponentString(type.name());
        Style style = component.getStyle();
        style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                String.format("/%s list %s", RCCommands.biomeDict.getCommandName(), type)));
        style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                ServerTranslations.format("commands.biomedict.get.number", BiomeDictionary.getBiomesForType(type).length)));
        style.setColor(TextFormatting.AQUA);
        return component;
    }

    @Nonnull
    public static ITextComponent dimension(int dimensionID)
    {
        ITextComponent component = new TextComponentString(String.valueOf(dimensionID));
        component.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                String.format("/%s types %s", RCCommands.dimensionDict.getCommandName(), dimensionID)));
        component.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                ServerTranslations.format("commands.dimensiondict.list.number", DimensionDictionary.getDimensionTypes(DimensionManager.getProvider(dimensionID)).size())));
        return component;
    }

    @Nonnull
    public static ITextComponent dimensionType(String type)
    {
        ITextComponent component = new TextComponentString(type);
        component.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                String.format("/%s list %s", RCCommands.dimensionDict.getCommandName(), type)));
        component.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                ServerTranslations.format("commands.dimensiondict.get.number", CommandDimensionDict.allDimensionsOfType(type).size())));
        return component;
    }
}
