/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import ivorius.ivtoolkit.network.SchedulingMessageHandler;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.capability.StructureEntityInfo;
import ivorius.reccomplex.files.SimpleLeveledRegistry;
import ivorius.reccomplex.files.loading.RCFileSuffix;
import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.gui.editstructure.GuiEditGenericStructure;
import ivorius.reccomplex.utils.SaveDirectoryData;
import ivorius.reccomplex.world.gen.feature.structure.StructureInfo;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructureInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketEditStructureHandler extends SchedulingMessageHandler<PacketEditStructure, IMessage>
{
    public static void openEditStructure(GenericStructureInfo structureInfo, String structureID, EntityPlayerMP player)
    {
        if (structureID == null)
            structureID = "NewStructure";

        StructureEntityInfo structureEntityInfo = StructureEntityInfo.get(player, null);

        if (structureEntityInfo != null)
            structureEntityInfo.setCachedExportStructureBlockDataNBT(structureInfo.worldDataCompound);

        SimpleLeveledRegistry<StructureInfo>.Status status = StructureRegistry.INSTANCE.status(structureID);

        RecurrentComplex.network.sendTo(new PacketEditStructure(structureInfo, structureID,
                SaveDirectoryData.defaultData(structureID, status != null && status.isActive(),
                        RecurrentComplex.loader.tryFindIDs(ResourceDirectory.ACTIVE.toPath(), RCFileSuffix.STRUCTURE),
                        RecurrentComplex.loader.tryFindIDs(ResourceDirectory.INACTIVE.toPath(), RCFileSuffix.STRUCTURE))
        ), player);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void processClient(PacketEditStructure message, MessageContext ctx)
    {
        Minecraft.getMinecraft().displayGuiScreen(new GuiEditGenericStructure(message.getStructureID(), message.getStructureInfo(), message.getSaveDirectoryData()));
    }
}
