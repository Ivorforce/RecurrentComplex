/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature;

import ivorius.reccomplex.RCRegistryHandler;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.SyslogLayout;

import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorldgenMonitor extends AbstractAppender
{
    private String modName;
    private BiConsumer<ChunkPos, Integer> consumer;
    private Pattern regex;

    public WorldgenMonitor(String modName, BiConsumer<ChunkPos, Integer> consumer)
    {
        super(modName + "_worldgen_monitor", null, SyslogLayout.newBuilder().build());
        this.modName = modName;
        this.consumer = consumer;
        regex = Pattern.compile(modName + " loaded a new chunk \\(([^,]*), ([^,]*)  Dimension: ([^)]*)\\)");
    }

    public static void create(String modName, BiConsumer<ChunkPos, Integer> consumer)
    {
        AbstractAppender appender = new WorldgenMonitor(modName, consumer);
        appender.start();
        ((Logger) FMLLog.log).addAppender(appender);
    }

    @Override
    public void append(LogEvent event)
    {
        String message = event.getMessage().getFormattedMessage();
        Matcher matcher = regex.matcher(message);
        if (matcher.find())
        {
            int l = Integer.valueOf(matcher.group(1));
            int r = Integer.valueOf(matcher.group(2));
            int d = Integer.valueOf(matcher.group(3));
            consumer.accept(new ChunkPos(l, r), d);
        }
    }
}
