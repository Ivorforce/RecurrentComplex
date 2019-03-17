/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.mcopts.commands.CommandSplit;
import ivorius.mcopts.commands.parameters.DirectCommand;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.Repository;
import ivorius.reccomplex.capability.RCEntityInfo;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.commands.clipboard.CommandPaste;
import ivorius.reccomplex.commands.clipboard.CommandSelectCopy;
import ivorius.reccomplex.commands.files.CommandDelete;
import ivorius.reccomplex.commands.files.CommandReload;
import ivorius.reccomplex.commands.files.CommandWrite;
import ivorius.reccomplex.commands.former.*;
import ivorius.reccomplex.commands.info.CommandBiomeDict;
import ivorius.reccomplex.commands.info.CommandDimensionDict;
import ivorius.reccomplex.commands.preview.CommandCancel;
import ivorius.reccomplex.commands.preview.CommandConfirm;
import ivorius.reccomplex.commands.preview.CommandPreview;
import ivorius.reccomplex.commands.schematic.CommandConvertSchematic;
import ivorius.reccomplex.commands.schematic.CommandExportSchematic;
import ivorius.reccomplex.commands.schematic.CommandImportSchematic;
import ivorius.reccomplex.commands.structure.*;
import ivorius.reccomplex.commands.structure.sight.CommandSight;
import ivorius.reccomplex.commands.structure.sight.CommandSightCheck;
import ivorius.reccomplex.files.RCFiles;
import ivorius.reccomplex.files.loading.FileLoader;
import ivorius.reccomplex.files.loading.LeveledRegistry;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.utils.algebra.ExpressionCache;
import ivorius.reccomplex.utils.algebra.FunctionExpressionCaches;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.util.Set;

/**
 * Created by lukas on 18.01.15.
 */
public class RCCommands
{
    public static ICommand sanity;

    @Nullable
    public static ICommand confirm;
    @Nullable
    public static ICommand cancel;

    public static ICommand generate;
    public static CommandStructures structures;

    public static ICommand reopen;

    public static CommandSelection select;
    public static CommandSight sight;

    public static ICommand biomeDict;
    public static ICommand dimensionDict;

    public static void onServerStart(FMLServerStartingEvent event)
    {
        event.registerServerCommand(sanity = new CommandSanity());

        event.registerServerCommand(new CommandAnd());

        event.registerServerCommand(new CommandWrite());
        event.registerServerCommand(new CommandDelete());
        event.registerServerCommand(new CommandReload());

        if (RCConfig.asCommandPermissionLevel >= 0)
            event.registerServerCommand(new CommandAs());

        if (!RecurrentComplex.isLite()) {
            event.registerServerCommand(new CommandExportStructure());
            event.registerServerCommand(new CommandEditStructure());
        }
        event.registerServerCommand(generate = new CommandGenerateStructure());
        event.registerServerCommand(new CommandImportStructure());

        event.registerServerCommand(new CommandMapStructure());

        event.registerServerCommand(select = new CommandSelection());

        if (!RecurrentComplex.isLite()) {
            event.registerServerCommand(new CommandPreview());
            event.registerServerCommand(confirm = new CommandConfirm());
            event.registerServerCommand(cancel = new CommandCancel());
        }

        event.registerServerCommand(reopen = new CommandReopen());

        if (!RecurrentComplex.isLite())
            event.registerServerCommand(new CommandVisual());

        event.registerServerCommand(new CommandFill());
        event.registerServerCommand(new CommandSetProperty());
        if (!RecurrentComplex.isLite()) {
            event.registerServerCommand(new CommandSplit(RCConfig.commandPrefix + "natural",
                    new CommandNaturalAll(),
                    new CommandNaturalSpace(),
                    new CommandNaturalFloor()
            ));
        }
        event.registerServerCommand(new CommandSelectSetBiome());

        event.registerServerCommand(new CommandSelectCopy());
        event.registerServerCommand(new CommandPaste());

        event.registerServerCommand(new CommandSelectMove());

        event.registerServerCommand(biomeDict = new CommandBiomeDict());
        event.registerServerCommand(dimensionDict = new CommandDimensionDict());

        event.registerServerCommand(new CommandSplit(RCConfig.commandPrefix + "schematic",
                new CommandImportSchematic(),
                new CommandExportSchematic(),
                new CommandConvertSchematic()
        ));
        event.registerServerCommand(new CommandVanilla());

        event.registerServerCommand(sight = new CommandSight());
        event.registerServerCommand(new CommandSightCheck(RCConfig.commandPrefix + "whatisthis", true));

        event.registerServerCommand(structures = new CommandStructures(RCConfig.commandPrefix + "structures",
                new CommandLookupStructure(),
                new CommandListStructures(),
                new CommandSearchStructure(),
                new CommandTweakStructures()
        ));

        event.registerServerCommand(new CommandRetrogen());
        event.registerServerCommand(new CommandDecorate());

        event.registerServerCommand(new CommandEval());
    }

    @SideOnly(Side.CLIENT)
    public static void registerClientCommands(ClientCommandHandler handler)
    {
        handler.registerCommand(new CommandVisitFiles());

        handler.registerCommand(new DirectCommand(RCConfig.commandPrefix + "repository", s -> Repository.openWebLink(Repository.BASE_URL)).permitFor(0));
        handler.registerCommand(new DirectCommand(RCConfig.commandPrefix + "browse", s -> Repository.openWebLink(Repository.browseURL())).permitFor(0));
    }

    @Nonnull
    public static RCEntityInfo getStructureEntityInfo(Object object, @Nullable EnumFacing facing) throws CommandException
    {
        RCEntityInfo info = RCEntityInfo.get(object, facing);

        if (info == null)
            throw RecurrentComplex.translations.commandException("commands.rc.noEntityInfo");

        return info;
    }

    @Nonnull
    public static SelectionOwner getSelectionOwner(Object object, @Nullable EnumFacing facing, boolean ensureValid) throws CommandException
    {
        SelectionOwner owner = SelectionOwner.getOwner(object, facing);

        if (owner == null)
            throw RecurrentComplex.translations.commandException("commands.rc.noSelection");

        if (ensureValid) ensureValidSelection(owner, false);

        return owner;
    }

    public static void ensureValidSelection(SelectionOwner owner, boolean inferSecond) throws CommandException
    {
        if (!owner.hasValidSelection()) {
            if (inferSecond && owner.getSelectedPoint1() != null)
                owner.setSelectedPoint2(owner.getSelectedPoint1());
            else if (inferSecond && owner.getSelectedPoint2() != null)
                owner.setSelectedPoint1(owner.getSelectedPoint2());
            else
                throw RecurrentComplex.translations.commandException("commands.selectModify.noSelection");
        }
    }

    public static void assertSize(ICommandSender sender, SelectionOwner owner) throws CommandException
    {
        int[] sides = owner.getSelection().areaSize();
        long size = (long) sides[0] * (long) sides[1] * (long) sides[2];

        if (size >= (long) Integer.MAX_VALUE)
            throw RecurrentComplex.translations.commandException("commands.rc.large.error");
        else if (size >= 100 * 100 * 100)
            sender.sendMessage(RecurrentComplex.translations.get("commands.rc.large.warn"));
    }

    public static void informDeleteResult(Pair<Set<Path>, Set<Path>> result, ICommandSender sender, String filetype, String id, ResourceDirectory directory)
    {
        ITextComponent pathComponent = RCTextStyle.path(directory, id);

        if (result.getRight().size() > 0)
            sender.sendMessage(RecurrentComplex.translations.format("reccomplex.delete.failure", filetype, pathComponent));
        else if (result.getLeft().size() > 0)
            sender.sendMessage(RecurrentComplex.translations.format("reccomplex.delete.success", filetype, pathComponent));
    }

    public static boolean informSaveResult(boolean result, ICommandSender sender, ResourceDirectory directory, String filetype, String id)
    {
        ITextComponent pathComponent = RCTextStyle.path(directory, id);

        if (result) {
            sender.sendMessage(RecurrentComplex.translations.format("reccomplex.save.full",
                    RecurrentComplex.translations.format("reccomplex.save.success", filetype, pathComponent),
                    RCTextStyle.submit(id))
            );
        }
        else
            sender.sendMessage(RecurrentComplex.translations.format("reccomplex.save.failure", filetype, pathComponent));

        return result;
    }

    public static void ensureValid(ExpressionCache<?> matcher, String argument) throws CommandException
    {
        if (!matcher.isExpressionValid())
            throw new CommandException(String.format("Argument %s: %s", argument, FunctionExpressionCaches.readableException(matcher)));
    }

    public static void tryReload(@Nonnull FileLoader loader, @Nonnull LeveledRegistry.Level level) throws CommandException
    {
        try {
            ResourceDirectory.reload(loader, level);
        }
        catch (IllegalArgumentException e) {
            throw new CommandException("Invalid reload type!");
        }
        catch (RCFiles.ResourceLocationLoadException e) {
            RecurrentComplex.logger.error("Can't load from resource '" + e.getLocation() + "'", e);

            throw new CommandException(reason(e));
        }
    }

    @Nonnull
    protected static String reason(RCFiles.ResourceLocationLoadException e)
    {
        return e.getCause() instanceof AccessDeniedException ? "Access Denied! (check your server's read privileges on the Minecraft directory)" : "Unknown Cause! (see logs)";
    }

    public static void select(ICommandSender sender, BlockArea area) throws CommandException
    {
        SelectionOwner owner = getSelectionOwner(sender, null, false);
        owner.setSelection(area);
    }
}
