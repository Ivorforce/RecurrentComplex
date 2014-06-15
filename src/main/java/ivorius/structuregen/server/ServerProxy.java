package ivorius.structuregen.server;

import ivorius.structuregen.SGProxy;
import net.minecraft.server.MinecraftServer;

import java.io.File;

/**
 * Created by lukas on 24.05.14.
 */
public class ServerProxy implements SGProxy
{
    @Override
    public File getBaseFolderFile(String filename)
    {
        return MinecraftServer.getServer().getFile(filename);
    }
}
