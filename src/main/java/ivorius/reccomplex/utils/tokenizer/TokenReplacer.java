/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.tokenizer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
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
    public static <T> String evaluate(Random random, List<Token> text, T context, Map<String, List<List<Token>>> theme)
    {
        StringBuilder builder = new StringBuilder();
        ArrayDeque<Token> queue = new ArrayDeque<>();
        queue.addAll(text);

        Map<String, List<Token>> repeats = new HashMap<>();

        Token token;
        boolean nextUpper = true;
        while ((token = queue.poll()) != null)
        {
            if (token instanceof ExplodingToken)
            {
                ExplodingToken<T> symbol = (ExplodingToken) token;
                boolean repeat = symbol.flags.contains("r");

                List<Token> tokens = repeat ? repeats.get(symbol.tag) : null;
                if (tokens == null)
                {
                    tokens = symbol.explode(theme, context, random);
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

        return builder.toString();
    }

    public static String firstCharUppercase(String name)
    {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    public interface Exploder<T>
    {
        static <T> Exploder<T> string(StringExploder<T> exploder)
        {
            return (token, theme, context, random) ->
                    Collections.singletonList(new StringToken(0, 0,
                            exploder.evaluate(token, theme, context, random)));
        }

        List<Token> explode(ExplodingToken token, Map<String, List<List<Token>>> theme, T context, Random random);

        interface StringExploder<T>
        {
            String evaluate(ExplodingToken token, Map<String, List<List<Token>>> theme, T context, Random random);
        }
    }

    public static abstract class Token extends SymbolTokenizer.Token
    {
        public Token(int startIndex, int endIndex)
        {
            super(startIndex, endIndex);
        }
    }

    public static class ExplodingToken<T> extends Token
    {
        public final String tag;
        public final List<String> flags;
        public final Exploder<T> exploder;

        public ExplodingToken(int startIndex, int endIndex, String tag, List<String> flags, Exploder<T> exploder)
        {
            super(startIndex, endIndex);
            this.tag = tag;
            this.flags = flags;
            this.exploder = exploder;
        }

        public List<Token> explode(Map<String, List<List<Token>>> theme, T context, Random random)
        {
            return exploder.explode(this, theme, context, random);
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

                Exploder<T> exploder = exploder(tag, flags);
                if (exploder != null)
                    return new ExplodingToken<>(index, end + 1, tag, flags, exploder);
            }

            return null;
        }

        protected abstract Exploder<T> exploder(String tag, List<String> flags);

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

        public static Map<String, List<List<Token>>> build(Multimap<String, List<Token>> build)
        {
            return build.asMap().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, k -> Lists.newArrayList(k.getValue())));
        }

        public static String flag(List<String> flags, int index, String def)
        {
            return flags.size() > index ? flags.get(index) : def;
        }

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

        public Multimap<String, List<Token>> build(Multimap<String, List<Token>> map, SymbolTokenizer<Token> tokenizer)
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

        protected abstract Theme getOther(String include);
    }
}
