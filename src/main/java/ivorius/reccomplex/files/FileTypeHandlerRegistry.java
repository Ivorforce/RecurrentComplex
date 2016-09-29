/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.files;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.events.FileLoadEvent;
import ivorius.reccomplex.events.RCEventBus;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nullable;
import java.nio.file.Path;

/**
 * Created by lukas on 29.09.16.
 */
public abstract class FileTypeHandlerRegistry<S> implements FileTypeHandler
{
    public String fileSuffix;
    public LeveledRegistry<? super S> registry;

    public FileTypeHandlerRegistry(String fileSuffix, LeveledRegistry<? super S> registry)
    {
        this.fileSuffix = fileSuffix;
        this.registry = registry;
    }

    @Override
    public boolean loadFile(Path path, @Nullable String customID, FileLoadContext context)
    {
        String domain = context.domain;
        boolean active = context.active;
        String id = FileTypeHandler.defaultName(path, customID);

        S s = null;

        try
        {
            s = read(path, id);
        }
        catch (Exception e)
        {
            RecurrentComplex.logger.error("Error reading file: " + path, e);
        }

        if (s != null)
        {
            FileLoadEvent.Pre<S> event = new FileLoadEvent.Pre<>(s, fileSuffix, id, domain, path, active);

            if (event.getResult() != Event.Result.DENY && RCConfig.shouldResourceLoad(fileSuffix, id, domain))
            {
                registry.register(id, domain, s, active, context.level);

                RCEventBus.INSTANCE.post(new FileLoadEvent.Post<>(s, fileSuffix, id, domain, path, active));
            }

            return true;
        }

        return false;
    }

    public abstract S read(Path path, String name) throws Exception;

    @Override
    public void clearFiles(LeveledRegistry.Level level)
    {
        registry.clear(level);
    }
}
