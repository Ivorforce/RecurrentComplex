/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.utils.BlockSurfacePos;
import net.minecraft.command.*;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.entity.Entity;

import javax.annotation.Nonnull;

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
        if (!RecurrentComplex.isLite())
        {
            event.registerServerCommand(new CommandExportStructure());
            event.registerServerCommand(new CommandEditStructure());
        }
        event.registerServerCommand(new CommandGenerateStructure());
        event.registerServerCommand(new CommandImportStructure());

        event.registerServerCommand(new CommandStructuresReload());

        event.registerServerCommand(new CommandSelect());
        event.registerServerCommand(new CommandSelectShift());
        event.registerServerCommand(new CommandSelectCrop());
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
        return new BlockPos(CommandBase.parseDouble((double)blockpos.getX(), args[startIndex], -30000000, 30000000, centerBlock), CommandBase.parseDouble((double)blockpos.getY(), args[startIndex + 1], 0, 256, false), CommandBase.parseDouble((double)blockpos.getZ(), args[startIndex + 2], -30000000, 30000000, centerBlock));
    }

    public static BlockSurfacePos parseSurfaceBlockPos(ICommandSender sender, String[] args, int startIndex, boolean centerBlock) throws NumberInvalidException
    {
        return parseSurfaceBlockPos(sender.getPosition(), args, startIndex, centerBlock);
    }

    public static BlockSurfacePos parseSurfaceBlockPos(BlockPos blockpos, String[] args, int startIndex, boolean centerBlock) throws NumberInvalidException
    {
        return BlockSurfacePos.from(new BlockPos(CommandBase.parseDouble((double)blockpos.getX(), args[startIndex], -30000000, 30000000, centerBlock), 0, CommandBase.parseDouble((double)blockpos.getZ(), args[startIndex + 1], -30000000, 30000000, centerBlock)));
    }
}
