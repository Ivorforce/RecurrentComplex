/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import cpw.mods.fml.common.event.FMLServerStartingEvent;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.entities.StructureEntityInfo;
import net.minecraft.command.CommandException;
import net.minecraft.entity.Entity;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 18.01.15.
 */
public class RCCommands
{
    public static void onServerStart(FMLServerStartingEvent event)
    {
        if (!RecurrentComplex.isLite())
            event.registerServerCommand(new CommandExportStructure());
        event.registerServerCommand(new CommandEditStructure());
        event.registerServerCommand(new CommandGenerateStructure());
        event.registerServerCommand(new CommandImportStructure());

        event.registerServerCommand(new CommandStructuresReload());

        event.registerServerCommand(new CommandSelect());
        event.registerServerCommand(new CommandSelectCrop());
        event.registerServerCommand(new CommandSelectShrink());
        event.registerServerCommand(new CommandSelectExpand());

        if (!RecurrentComplex.isLite())
        {
            event.registerServerCommand(new CommandPreview());
            event.registerServerCommand(new CommandConfirm());
            event.registerServerCommand(new CommandCancel());
        }

        if (!RecurrentComplex.isLite())
            event.registerServerCommand(new CommandVisual());

        event.registerServerCommand(new CommandSelectFill());
        event.registerServerCommand(new CommandSelectReplace());
        event.registerServerCommand(new CommandSelectFillSphere());
        event.registerServerCommand(new CommandSelectNatural());

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
        event.registerServerCommand(new CommandLookupStructure());
    }

    @Nonnull
    public static StructureEntityInfo getStructureEntityInfo(Entity entity)
    {
        StructureEntityInfo info = StructureEntityInfo.getStructureEntityInfo(entity);

        if (info == null)
            throw new CommandException("commands.rc.noEntityInfo");

        return info;
    }
}
