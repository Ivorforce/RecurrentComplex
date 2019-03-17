/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.network;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockPositions;
import ivorius.ivtoolkit.network.SchedulingMessageHandler;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.block.TileEntityBlockScript;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.worldscripts.TableDataSourceWorldScriptHolder;
import ivorius.reccomplex.world.gen.feature.StructureGenerator;
import ivorius.reccomplex.world.gen.feature.structure.Structures;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import ivorius.reccomplex.world.gen.script.WorldScriptHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketWorldDataHandler extends SchedulingMessageHandler<PacketWorldData, IMessage>
{
    public static void capture(WorldScriptHolder script, BlockPos source, BlockPos point2)
    {
        RecurrentComplex.network.sendToServer(new PacketWorldData(null, source, script.origin, point2));
    }

    public static void swap(WorldScriptHolder script, BlockPos source)
    {
        RecurrentComplex.network.sendToServer(new PacketWorldData(script.worldData, source,
                script.origin, script.origin.add(BlockPositions.fromIntArray(Structures.size(script.worldData, null))).add(-1, -1, -1)));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void processClient(PacketWorldData message, MessageContext ctx)
    {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;

        if (!(screen instanceof TableNavigator))
            throw new RuntimeException();

        TableDataSource dataSource = ((TableNavigator) screen).currentTable().getDataSource();

        if (!(dataSource instanceof TableDataSourceWorldScriptHolder))
            throw new RuntimeException();

        WorldScriptHolder script = ((TableDataSourceWorldScriptHolder) dataSource).script;
        script.worldData = message.worldData;
        // From now this MUST be lower corner
        script.origin = new BlockArea(message.capturePoint1, message.capturePoint2).getLowerCorner();

        if (screen instanceof TableDelegate) ((TableDelegate) screen).reloadData();
    }

    @Override
    public void processServer(PacketWorldData message, MessageContext ctx, WorldServer world)
    {
        BlockPos origin = message.source;
        BlockArea area = new BlockArea(message.capturePoint1.add(origin), message.capturePoint2.add(origin));

        // Send captured back
        IvWorldData worldData = IvWorldData.capture(world, area, true);
        // Unsupported because of recursion and TE scripts not working anyway
        worldData.tileEntities.removeIf(te -> TileEntity.create(world, te) instanceof TileEntityBlockScript);

        PacketWorldData packet = new PacketWorldData(worldData.createTagCompound(), origin, message.capturePoint1, message.capturePoint2);
        RecurrentComplex.network.sendTo(packet, ctx.getServerHandler().player);

        if (message.worldData != null)
        {
            GenericStructure structure = new GenericStructure();
            structure.worldDataCompound = message.worldData;

            new StructureGenerator<>(structure)
                    .world(world)
                    .lowerCoord(message.capturePoint1.add(origin))
                    .generationPredicate(p -> !p.equals(origin))
                    .generate();
        }
    }
}
