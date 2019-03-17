/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.events.handlers;

import ivorius.ivtoolkit.rendering.grid.GridRenderer;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.Repository;
import ivorius.reccomplex.Wiki;
import ivorius.reccomplex.capability.CapabilitySelection;
import ivorius.reccomplex.capability.RCEntityInfo;
import ivorius.reccomplex.client.rendering.SelectionRenderer;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.events.ItemGenerationEvent;
import ivorius.reccomplex.gui.GuiHider;
import ivorius.reccomplex.item.ItemInputHandler;
import ivorius.reccomplex.world.gen.feature.WorldGenStructures;
import ivorius.reccomplex.world.gen.feature.WorldRandomData;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.storage.loot.LootTable;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
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
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
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
        int ticks = mc.player.ticksExisted;

        Entity renderEntity = mc.getRenderViewEntity();
        RCEntityInfo info = RCEntityInfo.get(mc.player, null);
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
            GlStateManager.translate(MathHelper.floor(entityX / spacing) * spacing, MathHelper.floor(entityY / spacing) * spacing, MathHelper.floor(entityZ / spacing) * spacing);
            GridRenderer.renderGrid(8, spacing, 100, 0.05f);
            GlStateManager.popMatrix();
            GlStateManager.enableTexture2D();
        }

        GuiHider.draw(renderEntity, event.getPartialTicks());

        SelectionRenderer.renderSelection(mc.player, ticks, event.getPartialTicks());

        if (info != null && info.danglingOperation != null)
            info.danglingOperation.renderPreview(info.getPreviewType(), mc.world, ticks, event.getPartialTicks());

        GlStateManager.popMatrix();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onMouseInput(MouseEvent event)
    {
        EntityPlayer player = Minecraft.getMinecraft().player;
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
            LootTable lootTable = WeightedItemCollectionRegistry.INSTANCE.get(pair.getLeft());
            if (lootTable != null)
                event.inventory.setStackInSlot(event.fromSlot, lootTable.getRandomItemStack(event.server, event.random));

            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        RCEntityInfo sei = RCEntityInfo.get(event.player, null);
        if (sei != null)
            sei.update(event.player);

        CapabilitySelection sel = CapabilitySelection.get(event.player, null);
        if (sel != null)
            sel.update(event.player);
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
        if (!RCConfig.canUseCommand(event.getCommand().getName(), event.getSender()))
        {
            event.setCanceled(true);

            // From CommandHandler.executeCommand
            TextComponentTranslation TextComponent = new TextComponentTranslation("commands.generic.permission");
            TextComponent.getStyle().setColor(TextFormatting.RED);
            event.getSender().sendMessage(TextComponent);
        }
    }

    @SubscribeEvent
    public void onEntityCapapabilityAttach(AttachCapabilitiesEvent event)
    {
        if (event.getObject() instanceof EntityPlayer)
        {
            event.addCapability(new ResourceLocation(RecurrentComplex.MOD_ID, RCEntityInfo.CAPABILITY_KEY), new SimpleCapabilityProvider<>(RCEntityInfo.CAPABILITY));
            event.addCapability(new ResourceLocation(RecurrentComplex.MOD_ID, CapabilitySelection.CAPABILITY_KEY), new SimpleCapabilityProvider<>(CapabilitySelection.CAPABILITY));
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (RCConfig.postWorldStatus && event.player.canUseCommand(3, "op"))
        {
            WorldRandomData randomData = WorldRandomData.get(FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld());
            if (randomData.postWorldStatus(event.player.getName()))
            {
                event.player.getServer().commandManager.executeCommand(event.player, RCCommands.sanity.getName() + " --silent --short");

                ITextComponent count = new TextComponentString("" + StructureRegistry.INSTANCE.activeIDs().size());
                count.getStyle().setColor(TextFormatting.AQUA);

                ITextComponent list = new TextComponentString("[List]");
                list.getStyle().setColor(TextFormatting.AQUA);
                list.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Show List")));
                list.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/%s", RCCommands.structures.list())));

                ITextComponent add = new TextComponentString("[Add]");
                add.getStyle().setColor(TextFormatting.GREEN);
                add.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Browse Repository")));
                add.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Repository.browseURL()));

                ITextComponent remove = new TextComponentString("[Remove]");
                remove.getStyle().setColor(TextFormatting.RED);
                remove.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Disabling Structures")));
                remove.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Wiki.DISABLING_STRUCTURES));

                ITextComponent help = new TextComponentString("[Help]");
                help.getStyle().setColor(TextFormatting.AQUA);
                help.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Open Wiki")));
                help.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Wiki.HOME));

                ITextComponent statusMessage = RecurrentComplex.translations.format("reccomplex.server.status", count, list, add, remove, help);

                event.player.sendMessage(statusMessage);
            }
        }
    }

    @SubscribeEvent
    public void onMissingMapping(RegistryEvent.MissingMappings event)
    {
        RecurrentComplex.missingRemapper.onMissingMapping(event);
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
