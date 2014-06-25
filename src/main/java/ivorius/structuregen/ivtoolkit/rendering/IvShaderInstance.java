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

package ivorius.structuregen.ivtoolkit.rendering;

import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Matrix;
import org.lwjgl.util.vector.Matrix2f;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

public class IvShaderInstance
{
    public Logger logger;

    private int shaderID = 0;
    private int vertShader, fragShader = 0;

    private boolean shaderActive = false;

    private Map<String, Integer> uniformLocations = new HashMap<>();

    public int getShaderID()
    {
        return shaderID;
    }

    public int getVertShader()
    {
        return vertShader;
    }

    public int getFragShader()
    {
        return fragShader;
    }

    public IvShaderInstance(Logger logger)
    {
        this.logger = logger;
    }

    public void trySettingUpShader(String vertexShaderFile, String fragmentShaderFile)
    {
        if (shaderID <= 0)
        {
            registerShader(vertexShaderFile, fragmentShaderFile);
        }
    }

    public void registerShader(String vertexShaderCode, String fragmentShaderCode)
    {
        deleteShader();

        try
        {
            if (vertexShaderCode != null)
            {
                vertShader = createShader(vertexShaderCode, ARBVertexShader.GL_VERTEX_SHADER_ARB);
            }

            if (fragmentShaderCode != null)
            {
                fragShader = createShader(fragmentShaderCode, ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);
            }
        }
        catch (Exception exc)
        {
            exc.printStackTrace();
            return;
        }

        shaderID = ARBShaderObjects.glCreateProgramObjectARB();

        if (vertShader > 0)
        {
            ARBShaderObjects.glAttachObjectARB(shaderID, vertShader);
        }

        if (fragShader > 0)
        {
            ARBShaderObjects.glAttachObjectARB(shaderID, fragShader);
        }

        ARBShaderObjects.glLinkProgramARB(shaderID);
        if (ARBShaderObjects.glGetObjectParameteriARB(shaderID, ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB) == GL11.GL_FALSE)
        {
            logger.error(getLogInfo(shaderID));
            return;
        }

        ARBShaderObjects.glValidateProgramARB(shaderID);
        if (ARBShaderObjects.glGetObjectParameteriARB(shaderID, ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB) == GL11.GL_FALSE)
        {
            logger.error(getLogInfo(shaderID));
            return;
        }
    }

    private int createShader(String shaderCode, int shaderType) throws Exception
    {
        int shader = 0;
        try
        {
            shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType);

            if (shader == 0)
            {
                return 0;
            }

            ARBShaderObjects.glShaderSourceARB(shader, shaderCode);
            ARBShaderObjects.glCompileShaderARB(shader);

            if (ARBShaderObjects.glGetObjectParameteriARB(shader, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE)
            {
                throw new RuntimeException("Error creating shader: " + getLogInfo(shader));
            }

            return shader;
        }
        catch (Exception exc)
        {
            ARBShaderObjects.glDeleteObjectARB(shader);
            throw exc;
        }
    }

    public boolean useShader()
    {
        if (shaderID <= 0 && !shaderActive)
        {
            return false;
        }

        shaderActive = true;
        ARBShaderObjects.glUseProgramObjectARB(shaderID);

        return true;
    }

    public void stopUsingShader()
    {
        if (shaderID <= 0 && shaderActive)
        {
            return;
        }

        ARBShaderObjects.glUseProgramObjectARB(0);
        shaderActive = false;
    }

    public boolean isShaderActive()
    {
        return shaderActive;
    }

    public boolean setUniformInts(String key, int... ints)
    {
        return setUniformIntsOfType(key, ints.length, ints);
    }

    public boolean setUniformIntsOfType(String key, int typeLength, int... ints)
    {
        if (shaderID <= 0 || !shaderActive)
        {
            return false;
        }

        IntBuffer intBuffer = BufferUtils.createIntBuffer(ints.length);
        intBuffer.put(ints);
        intBuffer.position(0);

        if (typeLength == 1)
        {
            ARBShaderObjects.glUniform1ARB(getUniformLocation(key), intBuffer);
        }
        else if (typeLength == 2)
        {
            ARBShaderObjects.glUniform2ARB(getUniformLocation(key), intBuffer);
        }
        else if (typeLength == 3)
        {
            ARBShaderObjects.glUniform3ARB(getUniformLocation(key), intBuffer);
        }
        else if (typeLength == 4)
        {
            ARBShaderObjects.glUniform4ARB(getUniformLocation(key), intBuffer);
        }
        else
        {
            throw new IllegalArgumentException();
        }

        return true;
    }

    public boolean setUniformFloats(String key, float... floats)
    {
        return setUniformFloatsOfType(key, floats.length, floats);
    }

    public boolean setUniformFloatsOfType(String key, int typeLength, float... floats)
    {
        if (shaderID <= 0 || !shaderActive)
        {
            return false;
        }

        FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(floats.length);
        floatBuffer.put(floats);
        floatBuffer.position(0);

        if (typeLength == 1)
        {
            ARBShaderObjects.glUniform1ARB(getUniformLocation(key), floatBuffer);
        }
        else if (typeLength == 2)
        {
            ARBShaderObjects.glUniform2ARB(getUniformLocation(key), floatBuffer);
        }
        else if (typeLength == 3)
        {
            ARBShaderObjects.glUniform3ARB(getUniformLocation(key), floatBuffer);
        }
        else if (typeLength == 4)
        {
            ARBShaderObjects.glUniform4ARB(getUniformLocation(key), floatBuffer);
        }
        else
        {
            throw new IllegalArgumentException();
        }

        return true;
    }

    public boolean setUniformMatrix(String key, Matrix matrix)
    {
        if (shaderID <= 0 || !shaderActive)
        {
            return false;
        }

        int width;
        if (matrix instanceof Matrix2f)
        {
            width = 2;
        }
        else if (matrix instanceof Matrix3f)
        {
            width = 3;
        }
        else if (matrix instanceof Matrix4f)
        {
            width = 4;
        }
        else
        {
            throw new IllegalArgumentException();
        }

        FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(width * width);
        matrix.store(floatBuffer);
        floatBuffer.position(0);

        switch (width)
        {
            case 2:
                ARBShaderObjects.glUniformMatrix2ARB(getUniformLocation(key), false, floatBuffer);
                break;
            case 3:
                ARBShaderObjects.glUniformMatrix3ARB(getUniformLocation(key), false, floatBuffer);
                break;
            default:
                ARBShaderObjects.glUniformMatrix4ARB(getUniformLocation(key), false, floatBuffer);
                break;
        }

        return true;
    }

    public Integer getUniformLocation(String key)
    {
        if (shaderID <= 0)
        {
            return 0;
        }

        if (!uniformLocations.containsKey(key))
        {
            uniformLocations.put(key, ARBShaderObjects.glGetUniformLocationARB(shaderID, key));
        }

        return uniformLocations.get(key);
    }

    private String getLogInfo(int obj)
    {
        return ARBShaderObjects.glGetInfoLogARB(obj, ARBShaderObjects.glGetObjectParameteriARB(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
    }

    public void deleteShader()
    {
        if (shaderActive)
        {
            stopUsingShader();
        }

        if (fragShader > 0)
        {
            ARBShaderObjects.glDetachObjectARB(shaderID, fragShader);
            ARBShaderObjects.glDeleteObjectARB(fragShader);
            fragShader = 0;
        }

        if (vertShader > 0)
        {
            ARBShaderObjects.glDetachObjectARB(shaderID, vertShader);
            ARBShaderObjects.glDeleteObjectARB(vertShader);
            vertShader = 0;
        }

        if (shaderID > 0)
        {
            ARBShaderObjects.glDeleteObjectARB(shaderID);
            shaderID = 0;
        }

        uniformLocations.clear();
    }

    public static void outputShaderInfo(Logger logger)
    {
        ContextCapabilities cap = GLContext.getCapabilities();
        String renderer = GL11.glGetString(GL11.GL_RENDERER);
        String vendor = GL11.glGetString(GL11.GL_VENDOR);
        String version = GL11.glGetString(GL11.GL_VERSION);
        boolean fboSupported = cap.GL_EXT_framebuffer_object;

        String majorVersion;
        String minorVersion;

        String glslVersion;

        try
        {
            glslVersion = GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION);
        }
        catch (Exception ex)
        {
            glslVersion = "? (No GL20)";
        }

        try
        {
            minorVersion = "" + GL11.glGetInteger(GL30.GL_MINOR_VERSION);
            majorVersion = "" + GL11.glGetInteger(GL30.GL_MAJOR_VERSION);
        }
        catch (Exception ex)
        {
            minorVersion = "?";
            majorVersion = "? (No GL 30)";
        }

        printAlignedInfo("Vendor", vendor, logger);
        printAlignedInfo("Renderer", renderer, logger);
        printAlignedInfo("Version", version, logger);
        printAlignedInfo("Versions", getGLVersions(cap), logger);
        printAlignedInfo("Version Range", String.format("%s - %s", minorVersion, majorVersion), logger);
        printAlignedInfo("GLSL Version", glslVersion, logger);
        printAlignedInfo("Frame buffer object", fboSupported ? "Supported" : "Unsupported", logger);
    }

    private static void printAlignedInfo(String category, String info, Logger logger)
    {
        logger.info(String.format("%-20s: %s", category, info));
    }

    private static String getGLVersions(ContextCapabilities cap)
    {
        String versions = "";

        try
        {
            if (cap.OpenGL11)
            {
                versions += ":11";
            }
            if (cap.OpenGL12)
            {
                versions += ":12";
            }
            if (cap.OpenGL13)
            {
                versions += ":13";
            }
            if (cap.OpenGL14)
            {
                versions += ":14";
            }
            if (cap.OpenGL15)
            {
                versions += ":15";
            }
        }
        catch (Throwable throwable)
        {
            versions += ":lwjgl-Error-1";
        }

        try
        {
            if (cap.OpenGL20)
            {
                versions += ":20";
            }
            if (cap.OpenGL21)
            {
                versions += ":21";
            }
        }
        catch (Throwable throwable)
        {
            versions += ":lwjgl-Error-2";
        }

        try
        {
            if (cap.OpenGL30)
            {
                versions += ":30";
            }
            if (cap.OpenGL31)
            {
                versions += ":31";
            }
            if (cap.OpenGL32)
            {
                versions += ":32";
            }
            if (cap.OpenGL33)
            {
                versions += ":33";
            }
        }
        catch (Throwable throwable)
        {
            versions += ":lwjgl-Error-3";
        }

        try
        {
            if (cap.OpenGL40)
            {
                versions += ":40";
            }
            if (cap.OpenGL41)
            {
                versions += ":41";
            }
            if (cap.OpenGL42)
            {
                versions += ":42";
            }
            if (cap.OpenGL43)
            {
                versions += ":43";
            }
            if (cap.OpenGL44)
            {
                versions += ":44";
            }
        }
        catch (Throwable throwable)
        {
            versions += ":lwjgl-Error-4";
        }

        if (versions.length() > 0)
        {
            versions = versions.substring(1);
        }

        return versions;
    }
}
