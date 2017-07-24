/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import ivorius.ivtoolkit.network.SchedulingMessageHandler;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.capability.RCEntityInfo;
import ivorius.reccomplex.files.SimpleLeveledRegistry;
import ivorius.reccomplex.files.loading.RCFileSuffix;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.gui.editstructure.GuiEditGenericStructure;
import ivorius.reccomplex.utils.SaveDirectoryData;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketEditStructureHandler extends SchedulingMessageHandler<PacketEditStructure, IMessage>
{
    public static void openEditStructure(@Nonnull EntityPlayerMP player, @Nonnull GenericStructure structure, BlockPos lowerCoord, @Nullable String id, @Nullable ResourceDirectory directory)
    {
        if (id == null)
            id = "NewStructure";

        RCEntityInfo entityInfo = RCEntityInfo.get(player, null);

        if (entityInfo != null)
            entityInfo.setCachedExportStructureBlockDataNBT(structure.worldDataCompound);

        if (directory == null)
        {
            SimpleLeveledRegistry<Structure<?>>.Status status = StructureRegistry.INSTANCE.status(id);
            directory = ResourceDirectory.custom(status != null && status.isActive());
        }

        RecurrentComplex.network.sendTo(new PacketEditStructure(structure, id, lowerCoord,
                SaveDirectoryData.defaultData(id, directory,
                        RecurrentComplex.loader.tryFindIDs(ResourceDirectory.ACTIVE.toPath(), RCFileSuffix.STRUCTURE),
                        RecurrentComplex.loader.tryFindIDs(ResourceDirectory.INACTIVE.toPath(), RCFileSuffix.STRUCTURE))
        ), player);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void processClient(PacketEditStructure message, MessageContext ctx)
    {
        Minecraft.getMinecraft().displayGuiScreen(new GuiEditGenericStructure(message.getStructureID(), message.getStructureInfo(), message.getLowerCoord(), message.getSaveDirectoryData()));
    }
}
