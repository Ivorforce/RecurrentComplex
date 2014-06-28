/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.ivtoolkit.items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

/**
 * Created by lukas on 18.04.14.
 */
public class IvItemRendererModel implements IItemRenderer
{
    public ModelBase model;
    public ResourceLocation texture;
    public Entity fakeEntity;
    public float[] modelSize;

    public IvItemRendererModel(ModelBase model, ResourceLocation texture, Entity fakeEntity, float[] modelSize)
    {
        this.model = model;
        this.texture = texture;
        this.fakeEntity = fakeEntity;
        this.modelSize = modelSize;
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type)
    {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
    {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data)
    {
        GL11.glPushMatrix();

        if (type == ItemRenderType.ENTITY)
        {
            GL11.glTranslated(0.0, 1.0, 0.0);
        }
        else if (type == ItemRenderType.INVENTORY)
        {
            GL11.glTranslated(0.0, 0.3, 0.0);
        }
        else
        {
            GL11.glTranslated(0.5, 1.0, 0.5);
        }

        if (type != ItemRenderType.ENTITY)
        {
            GL11.glScalef(1.0f / modelSize[0], 1.0f / modelSize[1], 1.0f / modelSize[2]);
        }

        GL11.glRotatef(180.0f, 0.0f, 0.0f, 1.0f);
        GL11.glTranslated(0.0, -1.0, 0.0);

        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        model.render(fakeEntity, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);

        GL11.glPopMatrix();
    }
}
