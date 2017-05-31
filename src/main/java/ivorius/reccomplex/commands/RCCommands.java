/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockSurfacePos;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.capability.RCEntityInfo;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.commands.parameters.Parameter;
import ivorius.reccomplex.files.RCFiles;
import ivorius.reccomplex.files.loading.FileLoader;
import ivorius.reccomplex.files.loading.LeveledRegistry;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.utils.algebra.ExpressionCache;
import ivorius.reccomplex.utils.algebra.FunctionExpressionCaches;
import ivorius.reccomplex.utils.expression.ResourceMatcher;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import net.minecraft.command.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
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
import java.util.function.Predicate;

/**
 * Created by lukas on 18.01.15.
 */
public class RCCommands
{
    @Nullable
    public static ICommand confirm;
    @Nullable
    public static ICommand cancel;

    public static ICommand lookup;
    public static ICommand list;

    public static ICommand reopen;

    public static ICommand forget;

    public static ICommand biomeDict;
    public static ICommand dimensionDict;

    public static void onServerStart(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandSanity());

        event.registerServerCommand(new CommandWrite());
        event.registerServerCommand(new CommandWriteAll());
        event.registerServerCommand(new CommandDelete());

        if (RCConfig.asCommandPermissionLevel >= 0)
            event.registerServerCommand(new CommandAs());
        event.registerServerCommand(new CommandSelecting());

        if (!RecurrentComplex.isLite())
        {
            event.registerServerCommand(new CommandExportStructure());
            event.registerServerCommand(new CommandEditStructure());
        }
        event.registerServerCommand(new CommandGenerateStructure());
        event.registerServerCommand(new CommandImportStructure());

        event.registerServerCommand(new CommandMapStructure());
        event.registerServerCommand(new CommandMapAllStructure());

        event.registerServerCommand(new CommandReload());

        event.registerServerCommand(new CommandSelect());
        event.registerServerCommand(new CommandSelectShift());
        event.registerServerCommand(new CommandSelectCrop());
        event.registerServerCommand(new CommandSelectWand());
        event.registerServerCommand(new CommandSelectShrink());
        event.registerServerCommand(new CommandSelectExpand());

        if (!RecurrentComplex.isLite())
        {
            event.registerServerCommand(new CommandPreview());
            event.registerServerCommand(confirm = new CommandConfirm());
            event.registerServerCommand(cancel = new CommandCancel());
        }

        event.registerServerCommand(reopen = new CommandReopen());

        if (!RecurrentComplex.isLite())
            event.registerServerCommand(new CommandVisual());

        event.registerServerCommand(new CommandSelectFill());
        event.registerServerCommand(new CommandSelectReplace());
        event.registerServerCommand(new CommandSetProperty());
        event.registerServerCommand(new CommandSelectFillSphere());
        event.registerServerCommand(new CommandSelectFlood());
        if (!RecurrentComplex.isLite())
        {
            event.registerServerCommand(new CommandSelectFloor());
            event.registerServerCommand(new CommandSelectSpace());
            event.registerServerCommand(new CommandSelectNatural());
        }
        event.registerServerCommand(new CommandSelectSetBiome());

        event.registerServerCommand(new CommandSelectCopy());
        event.registerServerCommand(new CommandPaste(true, "paste", "commands.strucPaste.usage"));
        event.registerServerCommand(new CommandPaste(false, "pastegen", "commands.strucPasteGen.usage"));

        event.registerServerCommand(new CommandSelectMove());
        event.registerServerCommand(new CommandSelectDuplicate());

        event.registerServerCommand(biomeDict = new CommandBiomeDict());
        event.registerServerCommand(dimensionDict = new CommandDimensionDict());

        event.registerServerCommand(new CommandImportSchematic());
        event.registerServerCommand(new CommandExportSchematic());
        event.registerServerCommand(new CommandConvertSchematic());

        event.registerServerCommand(new CommandWhatIsThis());
        event.registerServerCommand(forget = new CommandForget());
        event.registerServerCommand(new CommandForgetAll());
        event.registerServerCommand(new CommandSelectRemember());

        event.registerServerCommand(lookup = new CommandLookupStructure());
        event.registerServerCommand(list = new CommandListStructures());
        event.registerServerCommand(new CommandSearchStructure());
        event.registerServerCommand(new CommandContaining());

        event.registerServerCommand(new CommandRetrogen());
        event.registerServerCommand(new CommandDecorate());
        event.registerServerCommand(new CommandDecorateOne());

        event.registerServerCommand(new CommandEval());
    }

    @SideOnly(Side.CLIENT)
    public static void registerClientCommands(ClientCommandHandler handler)
    {
        handler.registerCommand(new CommandVisitFiles());

        handler.registerCommand(new CommandVisitRepository());
        handler.registerCommand(new CommandVisitRepositoryBrowse());
    }

    @Nonnull
    public static RCEntityInfo getStructureEntityInfo(Object object, @Nullable EnumFacing facing) throws CommandException
    {
        RCEntityInfo info = RCEntityInfo.get(object, facing);

        if (info == null)
            throw ServerTranslations.commandException("commands.rc.noEntityInfo");

        return info;
    }

    @Nonnull
    public static SelectionOwner getSelectionOwner(Object object, @Nullable EnumFacing facing, boolean ensureValid) throws CommandException
    {
        SelectionOwner owner = SelectionOwner.getOwner(object, facing);

        if (owner == null)
            throw ServerTranslations.commandException("commands.rc.noSelection");

        if (ensureValid && !owner.hasValidSelection())
            throw ServerTranslations.commandException("commands.selectModify.noSelection");

        return owner;
    }

    public static void assertSize(ICommandSender sender, SelectionOwner owner) throws CommandException
    {
        int[] sides = owner.getSelection().areaSize();
        long size = (long) sides[0] * (long) sides[1] * (long) sides[2];

        if (size >= (long) Integer.MAX_VALUE)
            throw ServerTranslations.commandException("commands.rc.large.error");
        else if (size >= 100 * 100 * 100)
            sender.addChatMessage(ServerTranslations.get("commands.rc.large.warn"));
    }

    @Nonnull
    protected static Parameter.Result<ResourceMatcher> resourceMatcher(Parameter parameter, Predicate<String> isKnown)
    {
        return parameter.first().map(s -> ExpressionCache.of(new ResourceMatcher(isKnown), s))
                .filter(ExpressionCache::isExpressionValid, t -> new CommandException(t.getParseException().getMessage()));
    }

    protected static Parameter.Result<Predicate<Structure>> structurePredicate(Parameter parameter)
    {
        return resourceMatcher(parameter, s1 -> !s1.isEmpty()).map(m -> s -> m.test(StructureRegistry.INSTANCE.resourceLocation(s)));
    }

    public static void informDeleteResult(Pair<Set<Path>, Set<Path>> result, ICommandSender sender, String filetype, String id, ResourceDirectory directory)
    {
        ITextComponent pathComponent = RCTextStyle.path(directory, id);

        if (result.getRight().size() > 0)
            sender.addChatMessage(ServerTranslations.format("reccomplex.delete.failure", filetype, pathComponent));
        else if (result.getLeft().size() > 0)
            sender.addChatMessage(ServerTranslations.format("reccomplex.delete.success", filetype, pathComponent));
    }

    public static boolean informSaveResult(boolean result, ICommandSender sender, ResourceDirectory directory, String filetype, String id)
    {
        ITextComponent pathComponent = RCTextStyle.path(directory, id);

        if (result)
        {
            sender.addChatMessage(ServerTranslations.format("reccomplex.save.full",
                    ServerTranslations.format("reccomplex.save.success", filetype, pathComponent),
                    RCTextStyle.submit(id))
            );
        }
        else
            sender.addChatMessage(ServerTranslations.format("reccomplex.save.failure", filetype, pathComponent));

        return result;
    }

    public static void ensureValid(ExpressionCache<?> matcher, String argument) throws CommandException
    {
        if (!matcher.isExpressionValid())
            throw new CommandException(String.format("Argument %s: %s", argument, FunctionExpressionCaches.readableException(matcher)));
    }

    public static void tryReload(@Nonnull FileLoader loader, @Nonnull LeveledRegistry.Level level) throws CommandException
    {
        try
        {
            ResourceDirectory.reload(loader, level);
        }
        catch (IllegalArgumentException e)
        {
            throw new CommandException("Invalid reload type!");
        }
        catch (RCFiles.ResourceLocationLoadException e)
        {
            RecurrentComplex.logger.error("Can't load from resource '" + e.getLocation() + "'", e);

            throw new CommandException(reason(e));
        }
    }

    @Nonnull
    protected static String reason(RCFiles.ResourceLocationLoadException e)
    {
        return e.getCause() instanceof AccessDeniedException ? "Access Denied! (check your server's read privileges on the Minecraft directory)" : "Unknown Cause! (see logs)";
    }

    protected static Parameter.Result<AxisAlignedTransform2D> transform(Parameter rot, Parameter mir) throws CommandException
    {
        if (rot.has(1) || mir.has(1))
            return rot.first().missable().map(CommandBase::parseInt).orElse(() -> 0)
                    .flatMap(r -> mir.first().missable().map(CommandBase::parseBoolean).orElse(() -> false).map(m -> AxisAlignedTransform2D.from(r, m)));
        return Parameter.Result.empty();
    }
}
