/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import ivorius.reccomplex.RecurrentComplex;
import joptsimple.internal.Strings;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
                named.put(curName, trimQuotes(arg));
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
            while(tokenizer.nextToken() != -1) {
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
