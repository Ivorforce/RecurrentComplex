/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.primitives.Doubles;
import ivorius.reccomplex.RecurrentComplex;
import joptsimple.internal.Strings;

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

    protected List<String> raw;
    protected Set<String> flags;
    protected ListMultimap<String, String> params;
    protected List<String> order;

    protected Set<String> declaredFlags;
    protected Map<String, String> alias;

    public Parameters()
    {
        flags = new HashSet<>();
        params = ArrayListMultimap.create();
        order = new ArrayList<>();

        declaredFlags = new HashSet<>();
        alias = new HashMap<>();
    }

    public Parameters(Parameters blueprint)
    {
        this.raw = blueprint.raw;
        this.flags = blueprint.flags;
        this.params = blueprint.params;
        this.order = blueprint.order;

        this.declaredFlags = blueprint.declaredFlags;
        this.alias = blueprint.alias;
    }

    public static Parameters of(String[] args, Function<Parameters, Parameters> c)
    {
        Parameters parameters = new Parameters();
        return (c != null ? c.apply(parameters) : parameters).build(args);
    }

    public static String prefix(boolean isShort)
    {
        return isShort ? SHORT_FLAG_PREFIX : LONG_FLAG_PREFIX;
    }

    public static boolean hasLongPrefix(String name)
    {
        return name.startsWith(LONG_FLAG_PREFIX);
    }

    public static boolean hasShortPrefix(String name)
    {
        return name.startsWith(SHORT_FLAG_PREFIX) && Doubles.tryParse(name) == null;
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

    public Parameters build(String[] args)
    {
        raw = Arrays.asList(quoted(args));

        order.add(null);

        String curName = null;
        for (String arg : raw)
        {
            if (hasLongPrefix(arg))
            {
                flags.add(curName = root(arg.substring(LONG_FLAG_PREFIX.length())));
                if (declaredFlags.contains(curName)) curName = null;
            }
            else if (hasShortPrefix(arg))
            {
                List<String> curFlags = arg.substring(SHORT_FLAG_PREFIX.length()).chars().mapToObj(c -> String.valueOf((char) c)).collect(Collectors.toList());

                for (int i = 0; i < curFlags.size(); i++)
                {
                    flags.add(curName = root(curFlags.get(i)));
                    if (declaredFlags.contains(curName))
                        curName = null;
                    else if (curFlags.size() > i + 1)
                    {
                        // Direct input, e.g. -fusers/foo/file.png
                        String rest = Strings.join(curFlags.subList(i + 1, curFlags.size()), "");
                        order.add(curName);
                        params.put(curName, rest);
                        curName = null;
                    }
                }
            }
            else
            {
                order.add(curName);
                params.put(curName, arg);
                curName = null;
            }
        }

        return this;
    }

    public void requireBuilt() throws IllegalStateException
    {
        if (raw == null)
            throw new IllegalStateException();
    }

    public void requireUnbuilt() throws IllegalStateException
    {
        if (raw != null)
            throw new IllegalStateException();
    }

    public Parameters alias(String parent, String... aliases)
    {
        requireUnbuilt();
        parent = root(parent);

        for (String alias : aliases)
            this.alias.put(alias, parent);

        if (flags.removeAll(Arrays.asList(aliases)))
            flags.add(parent);

        return this;
    }

    public Parameters flag(String flag, String... aliases)
    {
        requireUnbuilt();
        declaredFlags.add(root(flag));
        alias(flag, aliases);
        return this;
    }

    public Parameters flags(Collection<String> flags)
    {
        for (String flag : flags)
            flag(flag);
        return this;
    }

    public Parameters flags(String... flags)
    {
        return flags(Arrays.asList(flags));
    }

    public String root(String name)
    {
        String other;
        while ((other = alias.get(name)) != null)
            name = other;
        return name;
    }

    public List<String> raw()
    {
        requireBuilt();
        return Collections.unmodifiableList(raw);
    }

    public String last()
    {
        requireBuilt();
        return raw.get(raw.size() - 1);
    }

    public String[] lastAsArray()
    {
        return new String[]{last()};
    }

    public Map<String, Parameter> entries()
    {
        requireBuilt();
        return flags.stream().collect(Collectors.toMap(k -> k,
                k -> new Parameter(k, params.get(k))));
    }

    public boolean has(@Nonnull String flag)
    {
        requireBuilt();
        return flags.contains(root(flag));
    }

    public Parameter get()
    {
        requireBuilt();
        return new Parameter(null, params.get(null));
    }

    public Parameter get(@Nonnull String name)
    {
        requireBuilt();
        name = root(name);
        return new Parameter(has(name) && !params.containsKey(name) ? -1 : 0, name, params.get(name));
    }
}
