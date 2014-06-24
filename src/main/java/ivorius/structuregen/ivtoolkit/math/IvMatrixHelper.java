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

package ivorius.structuregen.ivtoolkit.math;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

/**
 * Created by lukas on 07.03.14.
 */
public class IvMatrixHelper
{
//    public static Vector3f projectPoint( int width, int height, float fov, EntityLivingBase entityLivingBase, Vector3f point )
//    {
//        double degToRad = Math.PI / 180.0f;
//
//        double yaw = (-entityLivingBase.rotationYaw) * degToRad;
//        double pitch = entityLivingBase.rotationPitch * degToRad;
//        double roll = 0.0f * degToRad; //Do a barrel roll!
//
//        Vector4f viewedPos = new Vector4f((float) (point.x - entityLivingBase.posX), (float) (point.y - entityLivingBase.posY), (float) (point.z - entityLivingBase.posZ), 1.0f);
//        Matrix4f projectionMatrix = getProjectionMatrix(, width, height, fov);
//
//        Vector4f projectedPos = new Vector4f();
//        Matrix4f.transform(projectionMatrix, viewedPos, projectedPos);
//
//        Vector3f returnVec = new Vector3f(projectedPos.x / projectedPos.z, projectedPos.y / projectedPos.z, projectedPos.z);
//
//        return returnVec;
//    }

    public static Matrix4f getProjectionMatrix(float fov, float aspect, float nearPlane, float farPlane)
    {
//        Matrix4f projectionMatrix = new Matrix4f();
//        projectionMatrix.setZero();
//
//        //Manual calculation (Not taking xMul and yMul into account):
//        //double dx = Math.cos(yaw) * (Math.sin(roll) * viewedPos.y + Math.cos(roll) * viewedPos.x) - Math.sin(yaw) * viewedPos.z;
//        //double dy = Math.sin(pitch) * (Math.cos(yaw) * viewedPos.z + Math.sin(yaw) * (Math.sin(roll) * viewedPos.y + Math.cos(roll) * viewedPos.x)) + Math.cos(pitch) * (Math.cos(roll) * viewedPos.y - Math.sin(roll) * viewedPos.x);
//        //double dz = Math.cos(pitch) * (Math.cos(yaw) * viewedPos.z + Math.sin(yaw) * (Math.sin(roll) * viewedPos.y + Math.cos(roll) * viewedPos.x)) - Math.sin(pitch) * (Math.cos(roll) * viewedPos.y - Math.sin(roll) * viewedPos.x);
//
//        float widthInf = 0.0f;
//        float widthToHeightInf = 0.0f;
//        float heightInf = 1.0f;
//        float heightToWidthInf = 1.0f;
//
//        float fovWidthInf = 1.5f;
//        float fovHeightInf = 0.5f;
//
//        float xMul = (float) ((-Math.pow(width, widthInf) * Math.pow(height, heightToWidthInf) * 100.0f) / (2.0f * Math.pow(fov, fovWidthInf) / 8.8f));
//        float yMul = (float) ((-Math.pow(height, heightInf) * Math.pow(width, widthToHeightInf) * 100.0f) / (2.0f * fov * Math.pow(fov, fovHeightInf) / 8.5f));
//
//        //dx
//        projectionMatrix.m00 = (MathHelper.cos(yaw) * MathHelper.cos(roll)) * xMul;
//        projectionMatrix.m10 = (MathHelper.cos(yaw) * MathHelper.sin(roll)) * xMul;
//        projectionMatrix.m20 = -(MathHelper.sin(yaw)) * xMul;
//
//        //dy
//        projectionMatrix.m01 = (MathHelper.sin(pitch) * MathHelper.sin(yaw) * MathHelper.cos(roll) - MathHelper.cos(pitch) * MathHelper.sin(roll)) * yMul;
//        projectionMatrix.m11 = (MathHelper.sin(pitch) * MathHelper.sin(yaw) * MathHelper.sin(roll) + MathHelper.cos(pitch) * MathHelper.cos(roll)) * yMul/* * 0.5f*/;
//        projectionMatrix.m21 = (MathHelper.sin(pitch) * MathHelper.cos(yaw)) * yMul;
//
//        //dz
//        projectionMatrix.m02 = MathHelper.cos(pitch) * MathHelper.sin(yaw) * MathHelper.cos(roll) + MathHelper.sin(pitch) * MathHelper.sin(roll);
//        projectionMatrix.m12 = MathHelper.cos(pitch) * MathHelper.sin(yaw) * MathHelper.sin(roll) - MathHelper.sin(pitch) * MathHelper.cos(roll);
//        projectionMatrix.m22 = MathHelper.cos(pitch) * MathHelper.cos(yaw);
//
//        return projectionMatrix;

        // Setup projection matrix
        Matrix4f projectionMatrix = new Matrix4f();

        float y_scale = coTangent(fov / 2f);
        float x_scale = y_scale / aspect;
        float frustum_length = farPlane - nearPlane;

        projectionMatrix.m00 = x_scale;
        projectionMatrix.m11 = y_scale;
        projectionMatrix.m22 = -((farPlane + nearPlane) / frustum_length);
        projectionMatrix.m23 = -1;
        projectionMatrix.m32 = -((2 * nearPlane * farPlane) / frustum_length);
        projectionMatrix.m33 = 0;

        return projectionMatrix;
    }

    public static Matrix4f getOrthographicMatrix(float left, float right, float bottom, float top, float zNear, float zFar)
    {
        Matrix4f projectionMatrix = new Matrix4f();

        projectionMatrix.m00 = 2.0f / (right - left);
        projectionMatrix.m11 = 2.0f / (top - bottom);
        projectionMatrix.m22 = -2.0f / (zFar - zNear);
        projectionMatrix.m30 = -(right + left) / (right - left);
        projectionMatrix.m31 = -(top + bottom) / (top - bottom);
        projectionMatrix.m32 = -(zFar + zNear) / (zFar - zNear);
        projectionMatrix.m33 = 1;

        return projectionMatrix;
    }

    public static float coTangent(float f)
    {
        return 1.0f / (float) Math.tan(f);
    }

    public static Matrix4f lookFrom(float posX, float posY, float posZ, float yaw, float pitch, float roll, Matrix4f src, Matrix4f dest)
    {
        Matrix4f.rotate(roll, new Vector3f(0.0f, 0.0f, 1.0f), src, dest);
        Matrix4f.rotate(pitch, new Vector3f(1.0f, 0.0f, 0.0f), src, dest);
        Matrix4f.rotate(yaw, new Vector3f(0.0f, 1.0f, 0.0f), src, dest);
        Matrix4f.translate(new Vector3f(-posX, -posY, -posZ), src, dest);

        return dest;
    }

    public static Vector3f projectPoint(Matrix4f projectionMatrix, float posX, float posY, float posZ, float yaw, float pitch, float roll, Vector3f point)
    {
        Matrix4f lookProjectionMatrix = lookFrom(posX, posY, posZ, yaw, pitch, roll, projectionMatrix, projectionMatrix);
        Vector4f clippedPoint = new Vector4f(point.x, point.y, point.z, 1.0f);

        Matrix4f.transform(lookProjectionMatrix, clippedPoint, clippedPoint);

        return new Vector3f(clippedPoint.x, clippedPoint.y, clippedPoint.z);
    }


//    public static Vector3f getWorldCoords( int width, int height, float fov, EntityLivingBase entityLivingBase, Vector3f point )
//    {
//        double degToRad = Math.PI / 180.0f;
//
//        double yaw = (-entityLivingBase.rotationYaw) * degToRad;
//        double pitch = entityLivingBase.rotationPitch * degToRad;
//        double roll = 0.0f * degToRad; //Do a barrel roll!
//
//        Matrix3f inverseProjectionMatrix = getInverseProjectionMatrix((float) yaw, (float) pitch, (float) roll, width, height, fov);
//
//        Vector3f perspectivelessPoint = new Vector3f(point.x * point.z, point.y * point.z, point.z);
//
//        Vector3f worldCoords = new Vector3f();
//        Matrix3f.transform(inverseProjectionMatrix, perspectivelessPoint, worldCoords);
//
//        worldCoords.x += entityLivingBase.posX;
//        worldCoords.y += entityLivingBase.posY;
//        worldCoords.z += entityLivingBase.posZ;
//
//        return worldCoords;
//    }

//    public static Matrix3f getInverseProjectionMatrix( float yaw, float pitch, float roll, float width, float height, float fov )
//    {
//        return (Matrix3f) getProjectionMatrix(yaw, pitch, roll, width, height, fov).invert();
//    }
}
