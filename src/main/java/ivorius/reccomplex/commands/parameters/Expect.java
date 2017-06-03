/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.random.Person;
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
    protected final Set<String> flags = new HashSet<>();
    protected String cur;

    Expect()
    {
        getOrCreate(null);
    }

    public static <T extends Expect<T>> T expect()
    {
        //noinspection unchecked
        return (T) new Expect();
    }

    protected T identity()
    {
        //noinspection unchecked
        return (T) this;
    }

    public T named(@Nonnull String name)
    {
        getOrCreate(cur = name);
        return identity();
    }

    public T flag(@Nonnull String name)
    {
        flags.add(name);
        return named(name);
    }

    public T skip(int num)
    {
        return next(Collections.emptyList());
    }

    public T next(Completer completion)
    {
        Param cur = getOrCreate(this.cur);
        cur.next(completion);
        return identity();
    }

    @Nonnull
    protected Param getOrCreate(String id)
    {
        Param param = params.get(id);
        if (param == null)
            params.put(this.cur, param = new Param());
        return param;
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

    public T randomString()
    {
        Random rand = new Random();
        return any(Person.chaoticName(rand, rand.nextBoolean()));
    }

    public T repeat()
    {
        Param cur = params.get(this.cur);
        if (cur == null) throw new IllegalStateException();
        cur.repeat = true;
        return identity();
    }

    public int index()
    {
        return params.size();
    }

    public List<String> get(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        List<String> quoted = Arrays.stream(Parameters.quoted(args)).map(Parameters::trimQuotes).collect(Collectors.toList());
        String[] paramArray = quoted.toArray(new String[quoted.size()]);

        Parameters parameters = Parameters.of(args, flags.stream().toArray(String[]::new));

        String lastID = parameters.order.get(parameters.order.size() - 1);
        Parameter entered = lastID != null ? parameters.get(lastID) : parameters.get();
        Param param = this.params.get(lastID);

        if (param != null && (entered.count() <= param.completions.size() || param.repeat)
                // It notices we are entering a parameter so it won't be added to the parameters args anyway
                && !quoted.get(quoted.size() - 1).startsWith(Parameters.flagPrefix))
        {
            return param.completions.get(Math.min(entered.count() - 1, param.completions.size() - 1)).complete(server, sender, paramArray, pos).stream()
                    // More than one word, let's wrap this in quotes
                    .map(s -> s.contains(" ") && !s.startsWith("\"") ? String.format("\"%s\"", s) : s)
                    .collect(Collectors.toList());
        }

        return remaining(paramArray, parameters.flags);
    }

    @Nonnull
    public List<String> remaining(String[] paramArray, Set<String> flags)
    {
        return getListOfStringsMatchingLastWord(paramArray, this.params.keySet().stream()
                .filter(p -> p != null && !flags.contains(p))
                .map(p -> Parameters.flagPrefix + p).collect(Collectors.toList()));
    }

    /**
     * Useful only for usage()
     */
    public T description(String key)
    {
        return descriptionU(IvTranslations.get(key));
    }

    public T descriptionU(String description)
    {
        getOrCreate(cur).description(description);
        return identity();
    }

    public T optional(String key)
    {
        return optional(IvTranslations.get(key));
    }

    public T optionalU(String description)
    {
        return descriptionU(String.format("[%s]", description));
    }

    public T required()
    {
        Param param = getOrCreate(cur);
        String prev = param.descriptions.get(param.descriptions.size() - 1);
        param.description(String.format("<%s>", prev.substring(1, prev.length() - 1)));
        return identity();
    }

    public T required(String key)
    {
        return requiredU(IvTranslations.get(key));
    }

    public T requiredU(String description)
    {
        return descriptionU(String.format("<%s>", description));
    }

    public String usage()
    {
        return String.format("%s %s",
                params.get(null).descriptions.stream()
                        .reduce("", (l, r) -> String.format("%s %s", l, r)),
                params.entrySet().stream()
                        .filter(e -> e.getKey() != null)
                        .flatMap(e -> e.getValue().descriptions.stream()
                                .map(d -> String.format("--%s %s", e.getKey(), d))
                        )
                        .reduce("", (l, r) -> String.format("%s %s", l, r))
        );
    }

    public interface Completer
    {
        Collection<String> complete(MinecraftServer server, ICommandSender sender, String[] argss, @Nullable BlockPos pos);
    }

    protected class Param
    {
        protected final List<Completer> completions = new ArrayList<>();
        protected final List<String> descriptions = new ArrayList<>();
        protected boolean repeat;

        public Param next(Completer completion)
        {
            completions.add(completion);
            descriptions.add(String.format("[%d]", completions.size()));
            return this;
        }

        public Param description(String description)
        {
            descriptions.remove(descriptions.size() - 1);
            descriptions.add(description);
            return this;
        }
    }
}
