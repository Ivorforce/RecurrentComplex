/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by lukas on 30.05.17.
 */
public class Expect
{
    protected final Map<String, Param> params = new HashMap<>();

    protected String cur;

    private Expect()
    {

    }

    public static Expect start()
    {
        return new Expect();
    }

    public Expect pos(@Nullable BlockPos pos)
    {
        return next(args1 -> CommandBase.getTabCompletionCoordinate(args1, 0, pos))
                .next(args1 -> CommandBase.getTabCompletionCoordinate(args1, 1, pos))
                .next(args1 -> CommandBase.getTabCompletionCoordinate(args1, 2, pos));
    }

    public Expect surfacePos(@Nullable BlockPos pos)
    {
        return next(args1 -> CommandBase.getTabCompletionCoordinateXZ(args1, 0, pos))
                .next(args1 -> CommandBase.getTabCompletionCoordinateXZ(args1, 1, pos));
    }

    public Expect named(String name)
    {
        cur = name;
        return this;
    }

    public Expect next(Completer completion)
    {
        Param cur = params.get(this.cur);
        if (cur == null)
            params.put(this.cur, cur = new Param());
        cur.next(completion);
        return this;
    }

    public Expect next(Collection<? extends String> completion)
    {
        return next((server, sender, args, pos) -> CommandBase.getListOfStringsMatchingLastWord(args, completion));
    }

    public Expect next(Function<String[], List<String>> completion)
    {
        return next((server, sender, args, pos) -> completion.apply(args));
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

            if (param.startsWith("-"))
            {
                flags.add(curName = param.substring(1));
                curIndex = 0;
            }
        }

        Param param = this.params.get(curName);

        if (param == null || curIndex >= param.completion.size())
            return CommandBase.getListOfStringsMatchingLastWord(paramArray, this.params.keySet().stream()
                    .filter(p -> p != null && !flags.contains(p))
                    .map(p -> "-" + p).collect(Collectors.toList()));

        return param.completion.get(curIndex).complete(server, sender, paramArray, pos).stream()
                // More than one word, let's wrap this in quotes
                .map(s -> s.contains(" ") && !s.startsWith("\"") ? String.format("\"%s\"", s) : s)
                .collect(Collectors.toList());
    }

    public interface Completer
    {
        public List<String> complete(MinecraftServer server, ICommandSender sender, String[] argss, @Nullable BlockPos pos);
    }

    public class Param
    {
        private final List<Completer> completion = new ArrayList<>();

        public Param next(Completer completion)
        {
            this.completion.add(completion);
            return this;
        }
    }
}
