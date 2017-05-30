/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.tokenizer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.io.LineReader;
import ivorius.reccomplex.RecurrentComplex;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lukas on 30.05.17.
 */
public class TokenReplacer
{
    public static <T> String compute(Random random, List<Token> text, T context, Map<String, List<List<Token>>> theme)
    {
        StringBuilder builder = new StringBuilder();
        ArrayDeque<Token> queue = new ArrayDeque<>();
        queue.addAll(text);

        Map<String, List<Token>> repeats = new HashMap<>();

        Token token;
        boolean nextUpper = true;
        while ((token = queue.poll()) != null)
        {
            if (token instanceof ComputeToken)
            {
                ComputeToken<T> symbol = (ComputeToken) token;
                boolean repeat = symbol.flags.contains("r");

                List<Token> tokens = repeat ? repeats.get(symbol.tag) : null;
                if (tokens == null)
                {
                    tokens = symbol.compute(theme, context, random);
                    if (repeat) repeats.put(symbol.tag, tokens);
                }

                // Add it backwards since it's reversed
                for (int i = tokens.size() - 1; i >= 0; i--)
                    queue.addFirst(tokens.get(i));
            }
            else if (token instanceof StringToken)
            {
                String string = ((StringToken) token).string;
                builder.append(nextUpper ? firstCharUppercase(string) : string);
                nextUpper = string.matches(".*[.?!]$");
            }
        }

        return builder.toString().trim();
    }

    public static String firstCharUppercase(String name)
    {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    public interface Computer<T>
    {
        static <T> Computer<T> simple(SimpleComputer<T> computer)
        {
            return (token, theme, context, random) ->
                    Collections.singletonList(new StringToken(0, 0,
                            computer.compute(token, theme, context, random)));
        }

        List<Token> compute(ComputeToken token, Map<String, List<List<Token>>> theme, T context, Random random);

        interface SimpleComputer<T>
        {
            String compute(ComputeToken token, Map<String, List<List<Token>>> theme, T context, Random random);
        }
    }

    public static abstract class Token extends SymbolTokenizer.Token
    {
        public Token(int startIndex, int endIndex)
        {
            super(startIndex, endIndex);
        }
    }

    public static class ComputeToken<T> extends Token
    {
        public final String tag;
        public final List<String> flags;
        public final Computer<T> computer;

        public ComputeToken(int startIndex, int endIndex, String tag, List<String> flags, Computer<T> computer)
        {
            super(startIndex, endIndex);
            this.tag = tag;
            this.flags = flags;
            this.computer = computer;
        }

        public List<Token> compute(Map<String, List<List<Token>>> theme, T context, Random random)
        {
            return computer.compute(this, theme, context, random);
        }
    }

    public static class StringToken extends Token
    {
        public String string;

        public StringToken(int startIndex, int endIndex, String string)
        {
            super(startIndex, endIndex);
            this.string = string;
        }
    }

    public static abstract class ReplaceFactory<T> implements SymbolTokenizer.TokenFactory
    {
        public static String join(Collection<List<Token>> lists)
        {
            return lists.stream()
                    .map(Object::toString)
                    .reduce("", (s, r) -> s + " " + r);
        }

        @Nullable
        @Override
        public SymbolTokenizer.Token tryConstructSymbolTokenAt(int index, @Nonnull String string)
        {
            if (string.charAt(index) == '<')
            {
                int end = string.indexOf('>', index);
                String contents = string.substring(index + 1, end);
                List<String> parts = Arrays.asList(contents.split(" "));

                String tag = parts.get(0);
                List<String> flags = Lists.newArrayList(parts.subList(1, parts.size()));

                Computer<T> computer = computer(tag, flags);
                if (computer != null)
                    return new ComputeToken<>(index, end + 1, tag, flags, computer);
            }

            return null;
        }

        protected abstract Computer<T> computer(String tag, List<String> flags);

        @Nonnull
        @Override
        public SymbolTokenizer.Token constructStringToken(int index, @Nonnull String string)
        {
            return new StringToken(index, index + string.length(), string);
        }
    }

    public abstract static class Theme
    {
        public Multimap<String, String> contents = HashMultimap.create();

        protected void read(String fileContents)
        {
            LineReader reader = new LineReader(new StringReader(fileContents));

            Collection<String> currentList = null;
            String line;
            try
            {
                while ((line = reader.readLine()) != null)
                {
                    if (line.startsWith("***"))
                    {
                        String tag = line.substring(4).trim();
                        currentList = contents.get(tag);
                    }
                    else
                    {
                        String word = line.trim();
                        if (word.length() > 0 && currentList != null)
                        {
                            currentList.add(word);
                        }
                    }
                }
            }
            catch (IOException e)
            {
                RecurrentComplex.logger.error(e);
            }
        }

        public Map<String, List<List<Token>>> build()
        {
            HashMultimap<String, List<Token>> map = HashMultimap.create();
            SymbolTokenizer<Token> tokenizer = new SymbolTokenizer<>(
                    new SymbolTokenizer.SimpleCharacterRules('\\', null, new char[0], null),
                    factory()
            );

            return build(map, tokenizer).asMap().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, k -> Lists.newArrayList(k.getValue())));
        }

        protected Multimap<String, List<Token>> build(Multimap<String, List<Token>> map, SymbolTokenizer<Token> tokenizer)
        {
            for (String include : contents.get("include"))
            {
                Theme theme = getOther(include);

                if (theme == null)
                {
                    RecurrentComplex.logger.error("Can't find theme to include: " + include);
                    continue;
                }

                theme.build(map, tokenizer);
            }

            for (String key : contents.keySet())
                map.putAll(key, contents.get(key).stream()
                        .<List<Token>>map(s ->
                        {
                            try
                            {
                                return tokenizer.tokenize(s);
                            }
                            catch (ParseException e)
                            {
                                RecurrentComplex.logger.warn("Unable to read line: " + s, e);
                                return Collections.emptyList();
                            }
                        })::iterator);

            return map;
        }

        @Nonnull
        protected abstract ReplaceFactory factory();

        protected abstract Theme getOther(String include);

        public String flag(List<String> flags, int index, String def)
        {
            return flags.size() > index ? flags.get(index) : def;
        }
    }
}
