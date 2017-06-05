/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Doubles;
import ivorius.reccomplex.RecurrentComplex;
import joptsimple.internal.Strings;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by lukas on 30.05.17.
 */
public class Parameters
{
    public static final String SHORT_FLAG_PREFIX = "-";
    public static final String LONG_FLAG_PREFIX = "--";

    protected final Set<String> flags;
    protected final ListMultimap<String, String> params;
    protected final List<String> order;

    protected final Map<String, String> alias;

    public Parameters(Set<String> flags, ListMultimap<String, String> params, List<String> order)
    {
        this.flags = flags;
        this.params = params;
        this.order = order;
        this.alias = new HashMap<>();
    }

    public Parameters(Parameters blueprint)
    {
        this.flags = blueprint.flags;
        this.params = blueprint.params;
        this.order = blueprint.order;
        this.alias = blueprint.alias;
    }

    protected static <T> T of(String[] args, String[] flags, Function<Parameters, T> fun)
    {
        Set<String> foundFlags = new HashSet<>();
        List<String> order = new ArrayList<>();
        ListMultimap<String, String> named = ArrayListMultimap.create();

        order.add(null);

        String curName = null;
        for (String arg : quoted(args))
        {
            if (arg.startsWith(LONG_FLAG_PREFIX)) // Quoted arguments can never be arguments
            {
                foundFlags.add(curName = arg.substring(LONG_FLAG_PREFIX.length()));
                if (ArrayUtils.contains(flags, curName)) curName = null;
            }
            else if (arg.startsWith(SHORT_FLAG_PREFIX) && Doubles.tryParse(arg) == null)
            {
                List<String> curFlags = arg.substring(SHORT_FLAG_PREFIX.length()).chars().mapToObj(c -> String.valueOf((char) c)).collect(Collectors.toList());

                for (int i = 0; i < curFlags.size(); i++)
                {
                    String flag = curFlags.get(i);
                    if (curName != null)
                    {
                        String rest = Strings.join(curFlags.subList(i, curFlags.size()), "");
                        // Direct input, e.g. -fusers/foo/file.png
                        if (rest.length() > 0)
                        {
                            order.add(curName);
                            named.put(curName, rest);
                            curName = null;
                            break;
                        }
                    }
                    foundFlags.add(curName = flag);
                    if (ArrayUtils.contains(flags, curName)) curName = null;
                }
            }
            else
            {
                order.add(curName);
                named.put(curName, arg);
                curName = null;
            }
        }

        return fun.apply(new Parameters(foundFlags, named, order));
    }

    public static Parameters of(String[] args, String... flags)
    {
        return of(args, flags, Parameters::new);
    }

    public static String[] quoted(String[] args)
    {
        String full = Strings.join(args, " ");
        StringReader reader = new StringReader(full);
        StreamTokenizer tokenizer = new StreamTokenizer(reader);
        tokenizer.resetSyntax();
        tokenizer.wordChars(0, Integer.MAX_VALUE);
        tokenizer.whitespaceChars(0, ' ');
        tokenizer.quoteChar('"');

        List<String> quoted = new ArrayList<>();
        try
        {
            while (tokenizer.nextToken() != -1)
            {
                quoted.add(tokenizer.sval);
            }
        }
        catch (IOException e)
        {
            // Should never happen
            RecurrentComplex.logger.error("Error reading string", e);
        }

        reader.close();

        if (args[args.length - 1].length() == 0)
            quoted.add(""); // Suggested param

        return quoted.stream().toArray(String[]::new);
    }

    @Nonnull
    public static String trimQuotes(String arg)
    {
        String trimmed = arg;
        if (trimmed.startsWith("\""))
            trimmed = trimmed.substring(1, trimmed.length() - (trimmed.length() > 1 && trimmed.endsWith("\"") ? 1 : 0));
        return trimmed;
    }

    public void alias(String parent, String... aliases)
    {
        List<String> result = new ArrayList<>();
        List<String> sources = Lists.newArrayList(aliases);
        sources.add(parent);

        for (int i = 0; i < order.size(); i++)
        {
            String s = order.get(i);
            if (sources.contains(s))
            {
                result.add(params.get(s).remove(0));
                order.remove(i);
                order.add(i, parent);
            }
        }

        for (String alias : aliases)
            this.alias.put(alias, parent);

        // Should be empty by now
        params.get(parent).addAll(result);

        if (flags.removeAll(sources))
            flags.add(parent);
    }

    public String root(String name)
    {
        String other;
        while ((other = alias.get(name)) != null)
            name = other;
        return name;
    }

    public Map<String, Parameter> entries()
    {
        return flags.stream().collect(Collectors.toMap(k -> k,
                k -> new Parameter(k, params.get(k))));
    }

    public boolean has(@Nonnull String flag)
    {
        flag = root(flag);
        return flags.contains(flag);
    }

    public Parameter get()
    {
        return new Parameter(null, params.get(null));
    }

    public Parameter get(@Nonnull String name)
    {
        name = root(name);
        return new Parameter(has(name) && !params.containsKey(name) ? -1 : 0, name, params.get(name));
    }
}
