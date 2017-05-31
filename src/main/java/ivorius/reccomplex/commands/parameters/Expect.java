/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.minecraft.command.CommandBase.getListOfStringsMatchingLastWord;

/**
 * Created by lukas on 30.05.17.
 */
public class Expect<T extends Expect<T>>
{
    protected final Map<String, Param> params = new HashMap<>();
    protected String cur;

    Expect()
    {

    }

    public static <T extends Expect<T>> T start()
    {
        //noinspection unchecked
        return (T) new Expect();
    }

    public T named(String name)
    {
        cur = name;
        return (T) this;
    }

    public T flag(String name)
    {
        return named(name);
    }

    public T skip(int num)
    {
        return next(Collections.emptyList());
    }

    public T next(Completer completion)
    {
        Param cur = params.get(this.cur);
        if (cur == null)
            params.put(this.cur, cur = new Param());
        cur.next(completion);
        return (T) this;
    }

    public T any(Object... completion)
    {
        return next(Arrays.asList(completion));
    }

    public T next(Collection<?> completion)
    {
        return next((server, sender, args, pos) -> getListOfStringsMatchingLastWord(args, completion));
    }

    public T next(Function<String[], ? extends Collection<String>> completion)
    {
        return next((server, sender, args, pos) -> completion.apply(args));
    }

    public T repeat()
    {
        Param cur = params.get(this.cur);
        if (cur == null) throw new IllegalStateException();
        cur.repeat = true;
        return (T) this;
    }

    public int index()
    {
        return params.size();
    }

    public List<String> get(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        List<String> params = Arrays.asList(Parameters.quoted(args));
        String[] paramArray = params.toArray(new String[params.size()]);

        Set<String> flags = new HashSet<>();
        int curIndex = 0;

        String curName = null;
        for (int i = 0; i < params.size() - 1; i++)
        {
            String param = params.get(i);
            curIndex++;

            if (param.startsWith("-") && Doubles.tryParse(param) == null)
            {
                if (param.length() == 1)
                    curName = null;
                else
                    flags.add(curName = param.substring(1));
                curIndex = 0;
            }
        }

        Param param = this.params.get(curName);

        if (param != null && (curIndex < param.completion.size() || param.repeat))
        {
            Lists.newArrayList(remaining(paramArray, flags));
            List<String> paramCompletion = param.completion.get(Math.min(curIndex, param.completion.size() - 1)).complete(server, sender, paramArray, pos).stream()
                    // More than one word, let's wrap this in quotes
                    .map(s -> s.contains(" ") && !s.startsWith("\"") ? String.format("\"%s\"", s) : s)
                    .collect(Collectors.toCollection(ArrayList::new));

            // Also complete flags in case the user wants to switch the current
            if (params.get(params.size() - 1).startsWith("-"))
                paramCompletion.addAll(remaining(paramArray, flags));

            return paramCompletion;
        }

        return remaining(paramArray, flags);

    }

    @Nonnull
    public List<String> remaining(String[] paramArray, Set<String> flags)
    {
        return getListOfStringsMatchingLastWord(paramArray, this.params.keySet().stream()
                .filter(p -> p != null && !flags.contains(p))
                .map(p -> "-" + p).collect(Collectors.toList()));
    }

    public interface Completer
    {
        public Collection<String> complete(MinecraftServer server, ICommandSender sender, String[] argss, @Nullable BlockPos pos);
    }

    public class Param
    {
        protected final List<Completer> completion = new ArrayList<>();
        protected boolean repeat;

        public Param next(Completer completion)
        {
            this.completion.add(completion);
            return this;
        }
    }
}
