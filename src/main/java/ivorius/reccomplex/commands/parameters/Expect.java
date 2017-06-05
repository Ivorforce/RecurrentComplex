/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import com.google.common.primitives.Doubles;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.random.Person;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by lukas on 30.05.17.
 */
public class Expect<T extends Expect<T>>
{
    protected final Map<String, SuggestParameter> params = new HashMap<>();
    protected final Set<String> shortParams = new HashSet<>();
    protected final Set<String> flags = new HashSet<>();

    protected String currentName;

    Expect()
    {
        getOrCreate(null);
    }

    public static <T extends Expect<T>> T expect()
    {
        //noinspection unchecked
        return (T) new Expect();
    }

    public static List<String> toStrings(Object arg)
    {
        while (arg instanceof Optional)
            //noinspection unchecked
            arg = ((Optional) arg).orElse(Collections.emptyList());
        if (arg instanceof IntStream)
            arg = ((IntStream) arg).mapToObj(String::valueOf);
        if (arg instanceof Collection<?>)
            arg = ((Collection) arg).stream();
        if (arg instanceof Stream<?>)
            return ((Stream<?>) arg).map(Object::toString).collect(Collectors.toList());
        return Collections.singletonList(arg.toString());
    }

    public static List<String> matching(String arg, Object completion)
    {
        return CommandBase.getListOfStringsMatchingLastWord(new String[]{arg}, toStrings(completion));
    }

    public static List<String> matchingAny(String arg, Object... suggest)
    {
        return CommandBase.getListOfStringsMatchingLastWord(new String[]{arg}, Arrays.asList(suggest));
    }

    public Parameters declare(Parameters parameters)
    {
        parameters.flags(flags);
        this.params.forEach((key, param) ->
        {
            if (!Objects.equals(param.name, key))
                parameters.alias(param.name, key);
        });
        return parameters;
    }

    protected T identity()
    {
        //noinspection unchecked
        return (T) this;
    }

    public T named(@Nonnull String name, String... aliases)
    {
        Pair<String, Boolean> p = name(name);

        SuggestParameter param = getOrCreate(currentName = p.getKey());
        if (p.getRight()) shortParams.add(p.getKey());

        for (String alias : aliases)
        {
            p = name(alias);
            params.put(alias, param);
            if (p.getRight()) shortParams.add(p.getKey());
        }

        return identity();
    }

    private Pair<String, Boolean> name(String name)
    {
        boolean isShort = false;
        if (name.startsWith(Parameters.LONG_FLAG_PREFIX))
            name = name.substring(Parameters.LONG_FLAG_PREFIX.length());
        else if (name.length() == 1)
            isShort = true;
        else if (name.startsWith(Parameters.SHORT_FLAG_PREFIX))
            throw new IllegalArgumentException();

        return Pair.of(name, isShort);
    }

    public T flag(@Nonnull String name, String... aliases)
    {
        flags.add(name);
        Collections.addAll(flags, aliases);
        return named(name, aliases);
    }

    public T skip(int num)
    {
        return next(Collections.emptyList());
    }

    public T next(Completer completion)
    {
        SuggestParameter cur = getOrCreate(this.currentName);
        cur.next(completion);
        return identity();
    }

    @Nonnull
    protected SuggestParameter getOrCreate(@Nullable String name)
    {
        SuggestParameter param = params.get(name);
        if (param == null)
            params.put(currentName, param = new SuggestParameter(name));
        return param;
    }

    public T any(Object... completion)
    {
        return next(Arrays.asList(completion));
    }

    public T next(Object completion)
    {
        return next((server, sender, params, pos) -> matching(params.last(), completion));
    }

    public T next(Function<Parameters, ?> completion)
    {
        return next((server, sender, params, pos) -> completion.apply(params));
    }

    public T randomString()
    {
        Random rand = new Random();
        return any(Person.chaoticName(rand, rand.nextBoolean()));
    }

    public T repeat()
    {
        SuggestParameter cur = params.get(this.currentName);
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
        Parameters parameters = Parameters.of(args, this::declare);

        String lastID = parameters.order.get(parameters.order.size() - 1);
        Parameter entered = lastID != null ? parameters.get(lastID) : parameters.get();
        SuggestParameter param = this.params.get(lastID);

        String currentArg = parameters.last();
        boolean longFlag = currentArg.startsWith(Parameters.LONG_FLAG_PREFIX);
        boolean shortFlag = currentArg.startsWith(Parameters.SHORT_FLAG_PREFIX) && Doubles.tryParse(currentArg) == null;
        if (param != null && (entered.count() <= param.completions.size() || param.repeat)
                // It notices we are entering a parameter so it won't be added to the parameters args anyway
                && !longFlag && !shortFlag)
        {
            return toStrings(param.completions.get(Math.min(entered.count() - 1, param.completions.size() - 1)).complete(server, sender, parameters, pos)).stream()
                    // More than one word, let's wrap this in quotes
                    .map(s -> s.contains(" ") && !s.startsWith("\"") ? String.format("\"%s\"", s) : s)
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        List<String> suggest = new ArrayList<>();
        suggest.addAll(remaining(currentArg, parameters, false));
        suggest.addAll(remaining(currentArg, parameters, true));
        return matching(parameters.last(), suggest);
    }

    @Nonnull
    public List<String> remaining(String currentArg, Parameters parameters, boolean useShort)
    {
        return matching(currentArg, this.params.entrySet().stream()
                .filter(e -> e.getKey() != null)
                .filter(e -> !parameters.has(e.getKey()) // For flags
                        || parameters.get(e.getKey()).count() < e.getValue().completions.size() || e.getValue().repeat)
                .map(Map.Entry::getKey)
                .filter(p -> shortParams.contains(p) == useShort)
                .map(p -> (shortParams.contains(p) ? Parameters.SHORT_FLAG_PREFIX : Parameters.LONG_FLAG_PREFIX) + p)
        );
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
        params.get(currentName).description(description);
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
        SuggestParameter param = params.get(currentName);
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
        Object complete(MinecraftServer server, ICommandSender sender, Parameters parameters, @Nullable BlockPos pos);
    }

    protected class SuggestParameter
    {
        protected String name;
        protected final List<Completer> completions = new ArrayList<>();
        protected final List<String> descriptions = new ArrayList<>();
        protected boolean repeat;

        public SuggestParameter(String name)
        {
            this.name = name;
        }

        public SuggestParameter next(Completer completion)
        {
            completions.add(completion);
            descriptions.add(String.format("[%d]", completions.size()));
            return this;
        }

        public SuggestParameter description(String description)
        {
            descriptions.remove(descriptions.size() - 1);
            descriptions.add(description);
            return this;
        }
    }
}
