/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.files.loading;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.events.FileLoadEvent;
import ivorius.reccomplex.events.RCEventBus;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.file.Path;

/**
 * Created by lukas on 29.09.16.
 */
public abstract class FileLoaderRegistry<S> extends FileLoaderAdapter
{
    public LeveledRegistry<? super S> registry;

    public FileLoaderRegistry(String suffix, LeveledRegistry<? super S> registry)
    {
        super(suffix);
        this.registry = registry;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean loadFile(Path path, String id, FileLoadContext context)
    {
        String domain = context.domain;
        boolean active = context.active;

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
            FileLoadEvent.Pre<S> event = new FileLoadEvent.Pre<>(s, suffix, id, domain, path, active);
            active = event.active;

            if (event.getResult() != Event.Result.DENY && RCConfig.shouldResourceLoad(suffix, id, domain))
            {
                registry.register(id, domain, s, active, context.level);

                RCEventBus.INSTANCE.post(new FileLoadEvent.Post<>(s, suffix, id, domain, path, active));
            }

            return true;
        }

        return false;
    }

    public abstract S read(Path path, String name) throws Exception;

    @Override
    @ParametersAreNonnullByDefault
    public void clearFiles(LeveledRegistry.Level level)
    {
        registry.clear(level);
    }
}
