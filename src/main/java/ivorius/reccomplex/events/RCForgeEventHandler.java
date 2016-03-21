/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.events;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ivorius.ivtoolkit.rendering.grid.GridRenderer;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.client.rendering.SelectionRenderer;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.items.ItemInputHandler;
import ivorius.reccomplex.worldgen.WorldGenStructures;
import ivorius.reccomplex.worldgen.inventory.WeightedItemCollection;
import ivorius.reccomplex.worldgen.inventory.WeightedItemCollectionRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by lukas on 24.05.14.
 */
public class RCForgeEventHandler
{
    public final Set<StructureBoundingBox> disabledTileDropAreas = new HashSet<>();
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

    @SubscribeEvent
    public void onEntityDrop(EntityJoinWorldEvent event)
    {
        if (event.entity instanceof EntityItem)
        {
            final int entityX = MathHelper.floor_double(event.entity.posX);
            final int entityY = MathHelper.floor_double(event.entity.posY);
            final int entityZ = MathHelper.floor_double(event.entity.posZ);

            if (disabledTileDropAreas.stream().anyMatch(input -> input.isVecInside(entityX, entityY, entityZ)))
                event.setCanceled(true);
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onDrawWorld(RenderWorldLastEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        int ticks = mc.thePlayer.ticksExisted;

        EntityLivingBase renderEntity = mc.renderViewEntity;
        StructureEntityInfo info = StructureEntityInfo.getStructureEntityInfo(mc.thePlayer);
        double entityX = renderEntity.lastTickPosX + (renderEntity.posX - renderEntity.lastTickPosX) * (double) event.partialTicks;
        double entityY = renderEntity.lastTickPosY + (renderEntity.posY - renderEntity.lastTickPosY) * (double) event.partialTicks;
        double entityZ = renderEntity.lastTickPosZ + (renderEntity.posZ - renderEntity.lastTickPosZ) * (double) event.partialTicks;

        GL11.glPushMatrix();
        GL11.glTranslated(-entityX, -entityY, -entityZ);

        if (info != null && info.showGrid)
        {
            int spacing = 10;
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glColor3f(0.5f, 0.5f, 0.5f);
            GL11.glPushMatrix();
            GL11.glTranslatef(MathHelper.floor_double(entityX / spacing) * spacing, MathHelper.floor_double(entityY / spacing) * spacing, MathHelper.floor_double(entityZ / spacing) * spacing);
            GridRenderer.renderGrid(8, spacing, 100, 0.05f);
            GL11.glPopMatrix();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }

        SelectionRenderer.renderSelection(mc.thePlayer, ticks, event.partialTicks);

        if (info != null && info.danglingOperation != null)
            info.danglingOperation.renderPreview(info.getPreviewType(), mc.theWorld, ticks, event.partialTicks);

        GL11.glPopMatrix();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onMouseInput(MouseEvent event)
    {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        ItemStack heldItem = player.getHeldItem();
        if (heldItem != null && heldItem.getItem() instanceof ItemInputHandler)
        {
            if (((ItemInputHandler) heldItem.getItem()).onMouseInput(player, heldItem, event.button, event.buttonstate, event.dwheel))
                event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onItemGeneration(ItemGenerationEvent event)
    {
        Pair<String, Float> pair = null;
        if (event instanceof ItemGenerationEvent.Artifact)
            pair = RCConfig.customArtifactTag;
        else if (event instanceof ItemGenerationEvent.Book)
            pair = RCConfig.customBookTag;

        if (pair != null && pair.getRight() > 0.0f && event.random.nextFloat() < pair.getRight())
        {
            WeightedItemCollection weightedItemCollection = WeightedItemCollectionRegistry.itemCollection(pair.getLeft());
            if (weightedItemCollection != null)
                event.inventory.setInventorySlotContents(event.fromSlot, weightedItemCollection.getRandomItemStack(event.random));

            event.setCanceled(true);
        }
    }
}
