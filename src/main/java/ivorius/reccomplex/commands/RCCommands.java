/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.utils.BlockSurfacePos;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.*;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by lukas on 18.01.15.
 */
public class RCCommands
{
    public static ICommand confirm;
    public static ICommand cancel;

    public static ICommand lookup;
    public static ICommand list;

    public static void onServerStart(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandWrite());

        if (!RecurrentComplex.isLite())
        {
            event.registerServerCommand(new CommandExportStructure());
            event.registerServerCommand(new CommandEditStructure());
        }
        event.registerServerCommand(new CommandGenerateStructure());
        event.registerServerCommand(new CommandGenerateStructureAt());
        event.registerServerCommand(new CommandImportStructure());

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

        if (!RecurrentComplex.isLite())
            event.registerServerCommand(new CommandVisual());

        event.registerServerCommand(new CommandSelectFill());
        event.registerServerCommand(new CommandSelectReplace());
        event.registerServerCommand(new CommandSetProperty());
        event.registerServerCommand(new CommandSelectFillSphere());
        if (!RecurrentComplex.isLite())
        {
            event.registerServerCommand(new CommandSelectFloor());
            event.registerServerCommand(new CommandSelectSpace());
            event.registerServerCommand(new CommandSelectNatural());
        }

        event.registerServerCommand(new CommandSelectCopy());
        event.registerServerCommand(new CommandPaste());
        event.registerServerCommand(new CommandPasteGen());

        event.registerServerCommand(new CommandSelectMove());
        event.registerServerCommand(new CommandSelectDuplicate());

        event.registerServerCommand(new CommandBiomeDict());
        event.registerServerCommand(new CommandDimensionDict());

        event.registerServerCommand(new CommandImportSchematic());
        event.registerServerCommand(new CommandExportSchematic());

        event.registerServerCommand(new CommandWhatIsThis());
        event.registerServerCommand(lookup = new CommandLookupStructure());
        event.registerServerCommand(list = new CommandListStructures());
        event.registerServerCommand(new CommandSearchStructure());

        event.registerServerCommand(new CommandBrowseFiles());

        event.registerServerCommand(new CommandDecorate());
        event.registerServerCommand(new CommandDecorateOne());
    }

    @Nonnull
    public static StructureEntityInfo getStructureEntityInfo(Entity entity) throws CommandException
    {
        StructureEntityInfo info = StructureEntityInfo.getStructureEntityInfo(entity);

        if (info == null)
            throw ServerTranslations.commandException("commands.rc.noEntityInfo");

        return info;
    }

    public static BlockPos parseBlockPos(BlockPos blockpos, String[] args, int startIndex, boolean centerBlock) throws NumberInvalidException
    {
        return new BlockPos(CommandBase.parseDouble((double) blockpos.getX(), args[startIndex], -30000000, 30000000, centerBlock), CommandBase.parseDouble((double) blockpos.getY(), args[startIndex + 1], 0, 256, false), CommandBase.parseDouble((double) blockpos.getZ(), args[startIndex + 2], -30000000, 30000000, centerBlock));
    }

    public static BlockPos tryParseBlockPos(ICommandSender sender, String[] args, int startIndex, boolean centerBlock) throws NumberInvalidException
    {
        return args.length >= startIndex + 3
                ? CommandBase.parseBlockPos(sender, args, startIndex, centerBlock)
                : sender.getPosition();
    }

    @Nonnull
    public static BlockSurfacePos tryParseSurfaceBlockPos(ICommandSender sender, String[] args, int startIndex, boolean centerBlock) throws NumberInvalidException
    {
        return args.length >= startIndex + 2
                ? parseSurfaceBlockPos(sender, args, startIndex, centerBlock)
                : BlockSurfacePos.from(sender.getPosition());
    }

    public static BlockSurfacePos parseSurfaceBlockPos(ICommandSender sender, String[] args, int startIndex, boolean centerBlock) throws NumberInvalidException
    {
        return parseSurfaceBlockPos(sender.getPosition(), args, startIndex, centerBlock);
    }

    public static BlockSurfacePos parseSurfaceBlockPos(BlockPos blockpos, String[] args, int startIndex, boolean centerBlock) throws NumberInvalidException
    {
        return BlockSurfacePos.from(new BlockPos(CommandBase.parseDouble((double) blockpos.getX(), args[startIndex], -30000000, 30000000, centerBlock), 0, CommandBase.parseDouble((double) blockpos.getZ(), args[startIndex + 1], -30000000, 30000000, centerBlock)));
    }

    public static List<String> completeRotation(String[] args)
    {
        return CommandBase.getListOfStringsMatchingLastWord(args, "0", "1", "2", "3");
    }

    public static List<String> completeMirror(String[] args)
    {
        return CommandBase.getListOfStringsMatchingLastWord(args, "false", "true");
    }

    public static List<String> completeTransform(String[] args, int index)
    {
        return index == 0 ? completeRotation(args)
                : index == 1 ? completeMirror(args)
                : Collections.emptyList();
    }

    public static AxisAlignedTransform2D tryParseTransform(String[] args, int index) throws CommandException
    {
        return AxisAlignedTransform2D.from(tryParseRotation(args, index), tryParseMirror(args, index + 1));
    }

    public static int tryParseRotation(String[] args, int index) throws NumberInvalidException
    {
        return args.length > index ? CommandBase.parseInt(args[index]) : 0;
    }

    public static boolean tryParseMirror(String[] args, int mirrorIndex) throws CommandException
    {
        return args.length > mirrorIndex && CommandBase.parseBoolean(args[mirrorIndex]);
    }

    public static void informDeleteResult(Pair<Set<Path>, Set<Path>> result, ICommandSender sender, String filetype, String id, String path)
    {
        if (result.getRight().size() > 0)
            sender.addChatMessage(ServerTranslations.format("reccomplex.delete.failure", filetype, String.format("%s/%s", path, id)));
        else if (result.getLeft().size() > 0)
            sender.addChatMessage(ServerTranslations.format("reccomplex.delete.success", filetype, String.format("%s/%s", path, id)));
    }

    public static boolean informSaveResult(boolean result, ICommandSender sender, String path, String filetype, String id)
    {
        if (result)
            sender.addChatMessage(ServerTranslations.format("reccomplex.save.success", filetype, String.format("%s/%s", path, id)));
        else
            sender.addChatMessage(ServerTranslations.format("reccomplex.save.failure", filetype, String.format("%s/%s", path, id)));

        return result;
    }
}
