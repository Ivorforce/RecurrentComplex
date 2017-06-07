/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.random.Person;
import joptsimple.internal.Strings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
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
    protected final Multimap<String, String> aliases = HashMultimap.create();
    protected final Set<String> shortParams = new HashSet<>();
    protected final Set<String> flags = new HashSet<>();

    protected String currentName;
    protected final List<String> order = new ArrayList<>();
    protected int currentCount;
    protected int until = -1;

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
        parameters.until(until);
        return parameters;
    }

    protected T identity()
    {
        //noinspection unchecked
        return (T) this;
    }

    @Nonnull
    protected SuggestParameter getOrCreate(@Nullable String name)
    {
        SuggestParameter param = params.get(name);
        if (param == null)
            params.put(name, param = new SuggestParameter(name));
        return param;
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
        this.aliases.putAll(name, Arrays.asList(aliases));

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

    public T nextRaw(Completer completion)
    {
        SuggestParameter cur = getOrCreate(currentName);
        order.add(currentName);
        cur.next(completion);

        currentCount = 1;
        optional(); // All params are expected to be optional by default

        return identity();
    }

    protected T atOnce(int num)
    {
        currentCount = num;
        return identity();
    }

    public T skip()
    {
        return nextRaw((server, sender, parameters, pos) -> Stream.of());
    }

    public T any(Object... completion)
    {
        return nextRaw((server, sender, params, pos) -> matchingAny(params.last(), completion));
    }

    public T next(Object completion)
    {
        return nextRaw((server, sender, params, pos) -> matching(params.last(), completion));
    }

    public T next(Completer completion)
    {
        return nextRaw((server, sender, params, pos) -> matching(params.last(), completion.complete(server, sender, params, pos)));
    }

    public T next(Function<Parameters, ?> completion)
    {
        return nextRaw((server, sender, params, pos) -> matching(params.last(), completion.apply(params)));
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

    public T stopNamed()
    {
        until = params.get(null).completions.size();
        return identity();
    }

    public List<String> get(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        Parameters parameters = Parameters.of(args, this::declare);

        String lastName = parameters.lastName();
        Parameter entered = lastName != null ? parameters.get(lastName) : parameters.get();
        SuggestParameter param = this.params.get(lastName);

        String currentArg = parameters.last();
        boolean longFlag = Parameters.hasLongPrefix(currentArg);
        boolean shortFlag = Parameters.hasShortPrefix(currentArg);
        if (param != null && (entered.count() <= param.completions.size() || param.repeat)
                // It notices we are entering a parameter so it won't be added to the parameters args anyway
                && !(parameters.allowsNamed() && (longFlag || shortFlag)))
        {
            return toStrings(param.completions.get(Math.min(entered.count() - 1, param.completions.size() - 1)).complete(server, sender, parameters, pos)).stream()
                    // More than one word, let's wrap this in quotes
                    .map(s -> s.contains(" ") && !s.startsWith("\"") ? String.format("\"%s\"", s) : s)
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        if (!parameters.allowsNamed())
            return Collections.emptyList();

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
                .map(p -> Parameters.prefix(shortParams.contains(p)) + p)
        );
    }

    protected void mapLastDescriptions(BiFunction<Integer, String, String> fun)
    {
        List<String> relevant = order.subList(order.size() - currentCount, order.size());
        int[] idx = new int[1];
        List<String> last = relevant.stream().map(s ->
        {
            List<String> list = params.get(s).descriptions;
            return list.remove(list.size() - 1);
        }).map(s -> fun.apply(idx[0]++, s)).collect(Collectors.toList()); // Collect since we want to remove all first and then add back
        relevant.stream().forEach(s ->
                params.get(s).descriptions.add(last.remove(last.size() - 1))
        );
    }

    protected String stripOptionality(String description)
    {
        return description.startsWith("[") || description.startsWith("<")
                ? description.substring(1, description.length() - 1) : description;
    }

    protected String preserveOptionality(String description, String ref)
    {
        return ref.startsWith("[") ? String.format("[%s]", description)
                : ref.startsWith("<") ? String.format("<%s>", description)
                : description;
    }

    /**
     * Useful only for usage()
     */
    public T descriptionU(List<String> description)
    {
        if (description.size() != currentCount)
            throw new IllegalArgumentException();
        mapLastDescriptions((i, s) -> preserveOptionality(description.get(i), s));
        return identity();
    }

    public T descriptionU(String... descriptions)
    {
        return descriptionU(Arrays.asList(descriptions));
    }

    public T description(String... keys)
    {
        return descriptionU(Arrays.stream(keys)
                .map(IvTranslations::get)
                .collect(Collectors.toList()));
    }

    public T required()
    {
        mapLastDescriptions((i, s) -> String.format("<%s>", stripOptionality(s)));
        return identity();
    }

    public T optional()
    {
        mapLastDescriptions((i, s) -> String.format("[%s]", stripOptionality(s)));
        return identity();
    }

    public T naked()
    {
        mapLastDescriptions((i, s) -> stripOptionality(s));
        return identity();
    }

    public String usage()
    {
        return String.format("%s%s%s%s%s", TextFormatting.RESET, TextFormatting.YELLOW,
                params.get(null).descriptions.stream()
                        .map(s -> s + " ")
                        .reduce("", (l, r) -> l + r),
                TextFormatting.RESET,
                params.entrySet().stream()
                        .filter(e -> e.getKey() != null)
                        .filter(e -> e.getKey().equals(e.getValue().name))
                        .flatMap(e -> flags.contains(e.getKey()) ? Stream.of(keyRepresentation(e.getKey())) : e.getValue().descriptions.stream()
                                .map(desc -> String.format("%s %s%s%s", keyRepresentation(e.getKey()),
                                        TextFormatting.YELLOW, desc, TextFormatting.RESET
                                ))
                        )
                        .reduce("", (l, r) -> String.format("%s %s", l, r))
        );
    }

    protected String keyRepresentation(String key)
    {
        List<String> aliases = Lists.newArrayList(this.aliases.get(key));

        for (Iterator<String> iterator = aliases.iterator(); iterator.hasNext(); )
        {
            String alias = iterator.next();
            if (key.contains(alias))
            {
                key = key.replaceFirst(alias, String.format("%s%s%s", TextFormatting.AQUA, alias, TextFormatting.RESET));
                iterator.remove();
            }
        }

        aliases.add(0, key);

        return Parameters.prefix(shortParams.contains(key)) + Strings.join(aliases, "|");
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
    }
}
