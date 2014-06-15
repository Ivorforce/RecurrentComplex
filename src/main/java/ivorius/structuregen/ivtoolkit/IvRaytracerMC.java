/*
 * Copyright (c) 2014, Lukas Tenbrink.
 * http://lukas.axxim.net
 *
 * You are free to:
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes, unless you have a permit by the creator.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package ivorius.structuregen.ivtoolkit;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * Created by lukas on 13.02.14.
 */
public class IvRaytracerMC
{
    public static List<IvRaytracedIntersection> getIntersections(List<IvRaytraceableObject> objects, Entity entity)
    {
        double x = entity.posX;
        double y = entity.posY + entity.getEyeHeight();
        double z = entity.posZ;

        Vec3 lookVec = entity.getLookVec();
        return IvRaytracer.getIntersections(objects, x, y, z, lookVec.xCoord, lookVec.yCoord, lookVec.zCoord);
    }

    public static IvRaytracedIntersection getFirstIntersection(List<IvRaytraceableObject> objects, Entity entity)
    {
        double x = entity.posX;
        double y = entity.posY + entity.getEyeHeight();
        double z = entity.posZ;

        Vec3 lookVec = entity.getLookVec();

        List<IvRaytracedIntersection> intersections = IvRaytracer.getIntersections(objects, x, y, z, lookVec.xCoord, lookVec.yCoord, lookVec.zCoord);

        return IvRaytracer.findFirstIntersection(intersections, x, y, z, lookVec.xCoord, lookVec.yCoord, lookVec.zCoord);
    }

    public static void drawStandardOutlinesFromTileEntity(List<IvRaytraceableObject> objects, double d, double d1, double d2, TileEntity tileEntity)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef((float) d - tileEntity.xCoord, (float) d1 - tileEntity.yCoord, (float) d2 - tileEntity.zCoord);
        IvRaytracer.drawStandardOutlines(objects);
        GL11.glPopMatrix();
    }
}
