/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.events;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.rendering.IvRenderHelper;
import ivorius.reccomplex.client.rendering.AreaRenderer;
import ivorius.reccomplex.client.rendering.SelectionRenderer;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.items.ItemBlockSelectorFloating;
import ivorius.reccomplex.worldgen.WorldGenStructures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import org.lwjgl.opengl.GL11;

/**
 * Created by lukas on 24.05.14.
 */
public class RCForgeEventHandler
{
    private WorldGenStructures worldGenStructures;

    public RCForgeEventHandler()
    {
        this.worldGenStructures = new WorldGenStructures();
    }

    public void register()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPreChunkDecoration(PopulateChunkEvent.Pre event)
    {
        worldGenStructures.generate(event.rand, event.chunkX, event.chunkZ, event.world, event.chunkProvider, event.chunkProvider);
    }

    @SubscribeEvent
    public void onEntityConstruction(EntityEvent.EntityConstructing event)
    {
        if (event.entity instanceof EntityPlayer)
        {
            StructureEntityInfo.initInEntity(event.entity);
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onDrawWorld(RenderWorldLastEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        SelectionRenderer.renderSelection(mc.thePlayer, mc.thePlayer.ticksExisted, event.partialTicks);
    }
}
