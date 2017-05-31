/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import ivorius.reccomplex.commands.CommandImportSchematic;
import joptsimple.internal.Strings;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Created by lukas on 30.05.17.
 */
public class Parameters
{
    protected final Set<String> flags;
    protected final ListMultimap<String, String> params;

    public Parameters(Set<String> flags, ListMultimap<String, String> params)
    {
        this.flags = flags;
        this.params = params;
    }

    public Parameters(Parameters blueprint)
    {
        this.flags = blueprint.flags;
        this.params = blueprint.params;
    }

    protected static <T> T of(String[] args, Function<Parameters, T> fun)
    {
        List<String> params = Arrays.asList(quoted(args));

        Set<String> flags = new HashSet<>();
        ListMultimap<String, String> named = ArrayListMultimap.create();

        String curName = null;
        for (int i = 0; i < params.size(); i++)
        {
            String param = params.get(i);

            if (param.startsWith("-") && Doubles.tryParse(param) == null)
            {
                if (param.length() == 1)
                    curName = null;
                else
                    flags.add(curName = param.substring(1));
            }
            else
                named.put(curName, param); // Can be infinite
        }

        return fun.apply(new Parameters(flags, named));
    }

    public static Parameters of(String[] args)
    {
        return of(args, Parameters::new);
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

        return list.stream().map(CommandImportSchematic::trimQuotes).toArray(String[]::new);
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
        return new Parameter(name, params.get(name));
    }
}
