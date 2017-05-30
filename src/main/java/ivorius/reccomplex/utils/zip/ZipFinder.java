/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.zip;

import ivorius.reccomplex.utils.ByteArrays;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.zip.ZipInputStream;

/**
 * Created by lukas on 30.05.17.
 */
public class ZipFinder
{
    private final Map<String, EntryConsumer<InputStream>> map = new HashMap<>();

    public void read(ZipInputStream stream) throws IOException
    {
        IvZips.walkStreams(stream, (name, inputStream) ->
        {
            EntryConsumer<InputStream> consumer = map.get(name);
            if (consumer != null)
                consumer.accept(inputStream);
        });
    }

    public <T> Result<T> stream(String name, Function<InputStream, T> function)
    {
        if (map.containsKey(name))
            throw new IllegalArgumentException();

        Result<T> result = new Result<>(name);
        map.put(name, inputStream -> result.set(function.apply(inputStream)));
        return result;
    }

    public <T> Result<T> bytes(String name, Function<byte[], T> function)
    {
        return stream(name, inputStream ->
        {
            byte[] bytes = ByteArrays.completeByteArray(inputStream);
            return bytes != null ? function.apply(bytes) : null;
        });
    }

    @FunctionalInterface
    public interface Function<S, D>
    {
        D apply(S t) throws IOException;
    }

    @FunctionalInterface
    private interface EntryConsumer<T>
    {
        void accept(T t) throws IOException;
    }

    public class Result<T>
    {
        protected String name;
        protected T t;
        protected boolean present;

        public Result(String name)
        {
            this.name = name;
        }

        protected void set(T t)
        {
            this.t = t;
            present = true;
        }

        public boolean isPresent()
        {
            return present;
        }

        public T get()
        {
            if (!isPresent())
                throw new MissingEntryException(name);
            return t;
        }

        public T orElse(Supplier<T> defaultVal)
        {
            return isPresent() ? t : defaultVal.get();
        }

        public Optional<T> peek()
        {
            return present ? Optional.of(t) : Optional.empty();
        }
    }

    public class MissingEntryException extends RuntimeException
    {
        public final String name;

        public MissingEntryException(String name)
        {
            super("Missing Entry: " + name);
            this.name = name;
        }
    }
}
