/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import ivorius.reccomplex.commands.CommandImportSchematic;
import joptsimple.internal.Strings;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;

/**
 * Created by lukas on 30.05.17.
 */
public class Parameters
{
    public static final String flagPrefix = "--";

    protected final Set<String> flags;
    protected final ListMultimap<String, String> params;

    protected final List<String> order;

    public Parameters(Set<String> flags, ListMultimap<String, String> params, List<String> order)
    {
        this.flags = flags;
        this.params = params;
        this.order = order;
    }

    public Parameters(Parameters blueprint)
    {
        this.flags = blueprint.flags;
        this.params = blueprint.params;
        this.order = blueprint.order;
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
            if (arg.startsWith(flagPrefix)) // Quoted arguments can never be arguments
            {
                foundFlags.add(curName = arg.substring(flagPrefix.length()));
                if (ArrayUtils.contains(flags, curName)) curName = null;
            }
            else
            {
                order.add(curName);
                named.put(curName, CommandImportSchematic.trimQuotes(arg));
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
        List<String> list = Lists.newArrayList();

        int lastQuote = -1;
        for (int i = 0; i < args.length; i++)
        {
            if (lastQuote == -1 && args[i].indexOf("\"") == 0)
                lastQuote = i;

            if (lastQuote == -1)
                list.add(args[i]);
            else if (lastQuote >= 0 && args[i].lastIndexOf("\"") == args[i].length() - 1)
            {
                list.add(Strings.join(Arrays.asList(args).subList(lastQuote, i + 1), " "));
                lastQuote = -1;
            }
        }

        if (lastQuote >= 0)
            list.add(Strings.join(Arrays.asList(args).subList(lastQuote, args.length), " "));

        return list.stream().toArray(String[]::new);
    }

    public boolean has(@Nonnull String flag)
    {
        return flags.contains(flag);
    }

    public Parameter get()
    {
        return new Parameter(null, params.get(null));
    }

    public Parameter get(@Nonnull String name)
    {
        return new Parameter(has(name) && !params.containsKey(name) ? -1 : 0, name, params.get(name));
    }
}
