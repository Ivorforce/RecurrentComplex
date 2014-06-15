/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.events;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ivorius.structuregen.entities.StructureEntityInfo;
import ivorius.structuregen.items.ItemBlockSelectorFloating;
import ivorius.structuregen.ivtoolkit.BlockCoord;
import ivorius.structuregen.ivtoolkit.IvRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import org.lwjgl.opengl.GL11;

/**
 * Created by lukas on 24.05.14.
 */
public class SGForgeEventHandler
{
    public void register()
    {
        MinecraftForge.EVENT_BUS.register(this);
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
    public void onDrawWorld(RenderWorldLastEvent event)
    {
        EntityLivingBase renderEntity = Minecraft.getMinecraft().thePlayer;
        StructureEntityInfo structureEntityInfo = StructureEntityInfo.getStructureEntityInfo(renderEntity);
        BlockCoord hoverPoint = null;
        BlockCoord selPoint1 = null;
        BlockCoord selPoint2 = null;

        if (structureEntityInfo != null)
        {
            selPoint1 = structureEntityInfo.selectedPoint1;
            selPoint2 = structureEntityInfo.selectedPoint2;
        }

        ItemStack heldItem = renderEntity.getHeldItem();

        if (heldItem != null && heldItem.getItem() instanceof ItemBlockSelectorFloating)
        {
            hoverPoint = ItemBlockSelectorFloating.getHoveredBlock(renderEntity, ((ItemBlockSelectorFloating) heldItem.getItem()).selectionRange);
        }

        if (selPoint1 != null || selPoint2 != null || hoverPoint != null)
        {
            double entityX = renderEntity.lastTickPosX + (renderEntity.posX - renderEntity.lastTickPosX) * (double) event.partialTicks;
            double entityY = renderEntity.lastTickPosY + (renderEntity.posY - renderEntity.lastTickPosY) * (double) event.partialTicks;
            double entityZ = renderEntity.lastTickPosZ + (renderEntity.posZ - renderEntity.lastTickPosZ) * (double) event.partialTicks;

            GL11.glPushMatrix();
            GL11.glTranslated(-entityX, -entityY, -entityZ);

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glLineWidth(3.0f);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glDepthMask(false);
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

            if (selPoint1 != null && selPoint2 != null)
            {
                BlockCoord smaller = structureEntityInfo.selectedPoint1.getLowerCorner(structureEntityInfo.selectedPoint2);
                BlockCoord bigger = structureEntityInfo.selectedPoint1.getHigherCorner(structureEntityInfo.selectedPoint2);

                BlockCoord biggerMax = bigger.add(1, 1, 1);

                GL11.glColor4f(0.6f, 0.6f, 0.6f, 0.2f);
                drawCuboid(smaller, biggerMax, false);

                GL11.glColor3f(0.6f, 0.6f, 0.6f);
                drawCuboid(smaller, biggerMax, true);
            }

            if (selPoint1 != null)
            {
                GL11.glColor3f(1.0f, 0.3f, 0.3f);
                drawCuboid(selPoint1, selPoint1.add(1, 1, 1), true);
            }

            if (selPoint2 != null)
            {
                GL11.glColor3f(0.3f, 1.0f, 0.3f);
                drawCuboid(selPoint2, selPoint2.add(1, 1, 1), true);
            }

            if (hoverPoint != null)
            {
                GL11.glColor3f(0.3f, 0.3f, 1.0f);
                drawCuboid(hoverPoint, hoverPoint.add(1, 1, 1), true);
            }

            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_TEXTURE_2D);

            GL11.glPopMatrix();
        }
    }

    private static void drawCuboid(BlockCoord min, BlockCoord max, boolean lined)
    {
        float width2 = ((float) max.x - (float) min.x) * 0.5f;
        float height2 = ((float) max.y - (float) min.y) * 0.5f;
        float length2 = ((float) max.z - (float) min.z) * 0.5f;

        double centerX = min.x + width2;
        double centerY = min.y + height2;
        double centerZ = min.z + length2;

        GL11.glPushMatrix();
        GL11.glTranslated(centerX, centerY, centerZ);
        IvRenderHelper.drawCuboid(Tessellator.instance, width2 + 0.062f, height2 + 0.062f, length2 + 0.062f, 1.0f, lined);
        GL11.glPopMatrix();
    }
}
