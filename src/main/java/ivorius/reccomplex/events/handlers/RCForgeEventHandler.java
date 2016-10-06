/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.events.handlers;

import ivorius.ivtoolkit.rendering.grid.GridRenderer;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.capability.CapabilitySelection;
import ivorius.reccomplex.capability.StructureEntityInfo;
import ivorius.reccomplex.client.rendering.SelectionRenderer;
import ivorius.reccomplex.events.ItemGenerationEvent;
import ivorius.reccomplex.item.ItemInputHandler;
import ivorius.reccomplex.world.gen.feature.WorldGenStructures;
import ivorius.reccomplex.world.storage.loot.WeightedItemCollection;
import ivorius.reccomplex.world.storage.loot.WeightedItemCollectionRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by lukas on 24.05.14.
 */
public class RCForgeEventHandler
{
    public final Set<StructureBoundingBox> disabledTileDropAreas = new HashSet<>();

    public void register()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPreChunkDecoration(PopulateChunkEvent.Pre event)
    {
        WorldGenStructures.decorate((WorldServer) event.getWorld(), event.getRand(), new ChunkPos(event.getChunkX(), event.getChunkZ()), null);
    }

    @SubscribeEvent
    public void onEntityDrop(EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof EntityItem)
        {
            if (disabledTileDropAreas.stream().anyMatch(input -> input.isVecInside(new BlockPos(event.getEntity()))))
                event.setCanceled(true);
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onDrawWorld(RenderWorldLastEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        int ticks = mc.thePlayer.ticksExisted;

        Entity renderEntity = mc.getRenderViewEntity();
        StructureEntityInfo info = StructureEntityInfo.getStructureEntityInfo(mc.thePlayer, null);
        double entityX = renderEntity.lastTickPosX + (renderEntity.posX - renderEntity.lastTickPosX) * (double) event.getPartialTicks();
        double entityY = renderEntity.lastTickPosY + (renderEntity.posY - renderEntity.lastTickPosY) * (double) event.getPartialTicks();
        double entityZ = renderEntity.lastTickPosZ + (renderEntity.posZ - renderEntity.lastTickPosZ) * (double) event.getPartialTicks();

        GlStateManager.pushMatrix();
        GlStateManager.translate(-entityX, -entityY, -entityZ);

        if (info != null && info.showGrid)
        {
            int spacing = 10;
            GlStateManager.disableTexture2D();
            GlStateManager.color(0.5f, 0.5f, 0.5f);
            GlStateManager.pushMatrix();
            GlStateManager.translate(MathHelper.floor_double(entityX / spacing) * spacing, MathHelper.floor_double(entityY / spacing) * spacing, MathHelper.floor_double(entityZ / spacing) * spacing);
            GridRenderer.renderGrid(8, spacing, 100, 0.05f);
            GlStateManager.popMatrix();
            GlStateManager.enableTexture2D();
        }

        SelectionRenderer.renderSelection(mc.thePlayer, ticks, event.getPartialTicks());

        if (info != null && info.danglingOperation != null)
            info.danglingOperation.renderPreview(info.getPreviewType(), mc.theWorld, ticks, event.getPartialTicks());

        GlStateManager.popMatrix();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onMouseInput(MouseEvent event)
    {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
        if (heldItem != null && heldItem.getItem() instanceof ItemInputHandler)
        {
            if (((ItemInputHandler) heldItem.getItem()).onMouseInput(player, heldItem, event.getButton(), event.isButtonstate(), event.getDwheel()))
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
            WeightedItemCollection weightedItemCollection = WeightedItemCollectionRegistry.INSTANCE.get(pair.getLeft());
            if (weightedItemCollection != null)
                event.inventory.setInventorySlotContents(event.fromSlot, weightedItemCollection.getRandomItemStack(event.server, event.random));

            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        StructureEntityInfo structureEntityInfo = StructureEntityInfo.getStructureEntityInfo(event.player, null);
        if (structureEntityInfo != null)
        {
            structureEntityInfo.update(event.player);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent event)
    {
        if ((event.type == TickEvent.Type.CLIENT || event.type == TickEvent.Type.SERVER) && event.phase == TickEvent.Phase.END)
        {
            RecurrentComplex.communicationHandler.handleMessages(event.type == TickEvent.Type.SERVER, true);
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent event)
    {
        if (event instanceof ConfigChangedEvent.OnConfigChangedEvent && event.getModID().equals(RecurrentComplex.MOD_ID))
        {
            RCConfig.loadConfig(event.getConfigID());

            if (RecurrentComplex.config.hasChanged())
                RecurrentComplex.config.save();
        }
    }

    @SubscribeEvent
    public void onCommand(CommandEvent event)
    {
        if (!RCConfig.canUseCommand(event.getCommand().getCommandName(), event.getSender()))
        {
            event.setCanceled(true);

            // From CommandHandler.executeCommand
            TextComponentTranslation TextComponent = new TextComponentTranslation("commands.generic.permission");
            TextComponent.getStyle().setColor(TextFormatting.RED);
            event.getSender().addChatMessage(TextComponent);
        }
    }

    @SubscribeEvent
    public void onEntityCapapabilityAttach(AttachCapabilitiesEvent.Entity event)
    {
        if (event.getEntity() instanceof EntityPlayer)
        {
            event.addCapability(new ResourceLocation(RecurrentComplex.MOD_ID, StructureEntityInfo.CAPABILITY_KEY), new SimpleCapabilityProvider<>(StructureEntityInfo.CAPABILITY));
            event.addCapability(new ResourceLocation(RecurrentComplex.MOD_ID, CapabilitySelection.CAPABILITY_KEY), new SimpleCapabilityProvider<>(CapabilitySelection.CAPABILITY));
        }
    }

    private static class SimpleCapabilityProvider<T> implements ICapabilityProvider, INBTSerializable
    {
        public Capability<T> capability;
        public T t;

        public SimpleCapabilityProvider(Capability<T> capability)
        {
            this.capability = capability;
            this.t = capability.getDefaultInstance();
        }

        @Override
        public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
        {
            return capability == this.capability;
        }

        @Override
        public <TI> TI getCapability(Capability<TI> capability, @Nullable EnumFacing facing)
        {
            return capability == this.capability ? this.capability.cast(t) : null;
        }

        @Override
        public NBTBase serializeNBT()
        {
            return capability.writeNBT(t, null);
        }

        @Override
        public void deserializeNBT(NBTBase nbt)
        {
            capability.readNBT(t, null, nbt);
        }
    }
}
