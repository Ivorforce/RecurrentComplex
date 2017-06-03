/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.reccomplex.Repository;
import ivorius.reccomplex.commands.info.CommandDimensionDict;
import ivorius.reccomplex.dimensions.DimensionDictionary;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.utils.RCStrings;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.WorldStructureGenerationData;
import joptsimple.internal.Strings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by lukas on 20.03.17.
 */
public class RCTextStyle
{
    @Nonnull
    public static ITextComponent path(ResourceDirectory directory, String... path)
    {
        ITextComponent pathComponent = new TextComponentString(String.format("%s%s%s", directory, path.length > 0 ? "/" : "", Strings.join(path, "/")));
        pathComponent.getStyle().setColor(TextFormatting.GOLD);
        pathComponent.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Visit File")));
        pathComponent.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, directory.toFile().getAbsolutePath()));
        return pathComponent;
    }

    @Nonnull
    public static ITextComponent submit(String id)
    {
        ITextComponent submit = ServerTranslations.get("reccomplex.save.submit");
        submit.getStyle().setColor(TextFormatting.AQUA);
        submit.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ServerTranslations.get("reccomplex.save.submit.hover")));
        submit.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Repository.submitURL(id)));
        return submit;
    }

    @Nonnull
    public static ITextComponent visitFile(String id)
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
                String.format("/%s %s", RCCommands.lookup.getName(), id)));
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
                String.format("/%s types %s", RCCommands.biomeDict.getName(), biome.getRegistryName())));
        style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                ServerTranslations.format("commands.biomedict.list.number", BiomeDictionary.getTypes(biome).size())));
        style.setColor(TextFormatting.AQUA);
        return component;
    }

    @Nonnull
    public static ITextComponent biomeType(BiomeDictionary.Type type)
    {
        ITextComponent component = new TextComponentString(type.getName());
        Style style = component.getStyle();
        style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                String.format("/%s list %s", RCCommands.biomeDict.getName(), type)));
        style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                ServerTranslations.format("commands.biomedict.get.number", BiomeDictionary.getBiomes(type).size())));
        style.setColor(TextFormatting.AQUA);
        return component;
    }

    @Nonnull
    public static ITextComponent dimension(int dimensionID)
    {
        ITextComponent component = new TextComponentString(String.valueOf(dimensionID));
        component.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                String.format("/%s types %s", RCCommands.dimensionDict.getName(), dimensionID)));
        component.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                ServerTranslations.format("commands.dimensiondict.list.number", DimensionDictionary.getDimensionTypes(DimensionManager.getProvider(dimensionID)).size())));
        return component;
    }

    @Nonnull
    public static ITextComponent dimensionType(String type)
    {
        ITextComponent component = new TextComponentString(type);
        component.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                String.format("/%s list %s", RCCommands.dimensionDict.getName(), type)));
        component.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                ServerTranslations.format("commands.dimensiondict.get.number", CommandDimensionDict.allDimensionsOfType(type).size())));
        return component;
    }

    public static ITextComponent area(BlockPos left, BlockPos right)
    {
        return left == null || right == null
                ? ServerTranslations.format("commands.rcarea.get", pos(left), pos(right))
                : area(new BlockArea(left, right));
    }

    public static ITextComponent area(BlockArea area)
    {
        ITextComponent component = ServerTranslations.format("commands.rcarea.get", pos(area.getPoint1()), pos(area.getPoint2()));
        component.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ServerTranslations.get("commands.rcarea.select")));
        component.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/%s %s %d %d %d %d %d %d",
                RCCommands.select.getName(), RCCommands.select.set.getName(),
                area.getPoint1().getX(), area.getPoint1().getY(), area.getPoint1().getZ(),
                area.getPoint2().getX(), area.getPoint2().getY(), area.getPoint2().getZ()
        )));
        return component;
    }

    @Nonnull
    public static ITextComponent sight(WorldStructureGenerationData.Entry entry)
    {
        return sight(entry, false);
    }

    public static ITextComponent sight(WorldStructureGenerationData.Entry entry, boolean useID)
    {
        TextComponentString forget = new TextComponentString("X");
        String uuidString = entry.getUuid().toString();
        forget.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                String.format("/%s %s id %s", RCCommands.sight.getName(), RCCommands.sight.delete.getName(), uuidString)));
        forget.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                ServerTranslations.format("commands.rcforget.forget", uuidString)));
        forget.getStyle().setColor(TextFormatting.RED);

        ITextComponent name;
        if (useID)
            name = copy(entry.getUuid().toString());
        else
        {
            name = new TextComponentString(entry.description());
            name.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ServerTranslations.get("commands.rcsightinfo.lookup")));
            name.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/%s %s %s", RCCommands.sight.getName(), RCCommands.sight.info.getName(), entry.getUuid())));
        }

        name.getStyle().setColor(TextFormatting.AQUA);

        return new TextComponentTranslation("%s (%s)", name, forget);
    }

    public static ITextComponent copy(String text)
    {
        ITextComponent comp = new TextComponentString(text);
        comp.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ServerTranslations.get("commands.rccopy.suggest")));
        comp.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, text));
        return comp;
    }

    public static ITextComponent pos(BlockPos pos)
    {
        return pos != null
                ? ServerTranslations.format("commands.rcpos.get", pos.getX(), pos.getY(), pos.getZ())
                : ServerTranslations.format("commands.selectSet.point.none");
    }

    public static ITextComponent size(int[] size)
    {
        return size != null
                ? ServerTranslations.format("commands.rcsize.get", size[0], size[1], size[2])
                : ServerTranslations.format("commands.selectSet.point.none");
    }
}
