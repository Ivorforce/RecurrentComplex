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
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketEditStructureHandler extends SchedulingMessageHandler<PacketEditStructure, IMessage>
{
    public static void openEditStructure(GenericStructure structure, String id, EntityPlayerMP player)
    {
        if (id == null)
            id = "NewStructure";

        RCEntityInfo entityInfo = RCEntityInfo.get(player, null);

        if (entityInfo != null)
            entityInfo.setCachedExportStructureBlockDataNBT(structure.worldDataCompound);

        SimpleLeveledRegistry<Structure<?>>.Status status = StructureRegistry.INSTANCE.status(id);

        RecurrentComplex.network.sendTo(new PacketEditStructure(structure, id,
                SaveDirectoryData.defaultData(id, status != null && status.isActive(),
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
