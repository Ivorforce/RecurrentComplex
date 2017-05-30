/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.random;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.io.LineReader;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.files.SimpleLeveledRegistry;
import ivorius.reccomplex.utils.SymbolTokenizer;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lukas on 25.06.14.
 */
public class Poem
{
    private static final int TITLE_TRIES = 50;
    public static SimpleLeveledRegistry<Theme> THEME_REGISTRY = new SimpleLeveledRegistry<>("poem theme");
    private String title;
    private String text;

    public Poem(String title, String text)
    {
        this.title = title;
        this.text = text;
    }

    public static Poem randomPoem(Random random, Integer maxTitleLength)
    {
        return randomPoem(random, maxTitleLength, getRandomElementFrom(THEME_REGISTRY.allActive().stream().collect(Collectors.toList()), random));
    }

    public static Poem randomPoem(Random random, Integer maxTitleLength, Theme theme)
    {
        PoemContext poemContext = new PoemContext();
        //noinspection StatementWithEmptyBody
        while (poemContext.add(random, poemContext.names, 0.3f, Person.randomHuman(random, random.nextBoolean()).getFirstName()))
            ;
        //noinspection StatementWithEmptyBody
        while (poemContext.add(random, poemContext.places, 0.3f, Place.randomPlace(random).getFullPlaceType())) ;

        Map<String, List<List<Token>>> built = theme.build();

        String title = randomTitle(random, poemContext, maxTitleLength, built);
        char titleLastChar = title.charAt(title.length() - 1);
        if (titleLastChar == '.' || titleLastChar == ',' || titleLastChar == ';')
            title = title.substring(0, title.length() - 1);

        StringBuilder poem = new StringBuilder();

        int verses = random.nextInt(5) + 1;
        for (int verse = 0; verse < verses; verse++)
        {
            int lines = random.nextInt(10) + 1;
            for (int line = 0; line < lines; line++)
            {
                String phrase = getRandomPhrase(random, built.get("l"), poemContext, built);

                if (line == lines - 1)
                {
                    char phraseLastChar = phrase.charAt(phrase.length() - 1);
                    if (phraseLastChar == ',' || phraseLastChar == ';')
                        phrase = phrase.substring(0, phrase.length() - 1) + ".";
                }

                poem.append(phrase).append("\n");
            }

            poem.append("\n");
        }

        return new Poem(title, poem.toString());
    }

    @Nonnull
    protected static String randomTitle(Random random, PoemContext poemContext, Integer maxLength, Map<String, List<List<Token>>> built)
    {
        for (int i = 0; i < TITLE_TRIES; i++)
        {
            String title = getRandomPhrase(random, built.get("t"), poemContext, built).trim();
            if (maxLength == null || title.length() < maxLength)
                return title;
        }

        return StringUtils.abbreviate(getRandomPhrase(random, built.get("t"), poemContext, built).trim(), maxLength);
    }

    private static String getRandomPhrase(Random random, List<List<Token>> sentencePatterns, PoemContext poemContext, Map<String, List<List<Token>>> theme)
    {
        return firstCharUppercase(compute(random, getRandomElementFrom(sentencePatterns, random), poemContext, theme));
    }

    private static String compute(Random random, List<Token> text, PoemContext poemContext, Map<String, List<List<Token>>> theme)
    {
        StringBuilder builder = new StringBuilder();
        ArrayDeque<Token> queue = new ArrayDeque<>();
        queue.addAll(text);

        Map<String, List<Token>> repeats = new HashMap<>();

        Token token;
        while ((token = queue.poll()) != null)
        {
            if (token instanceof ComputeToken)
            {
                ComputeToken symbol = (ComputeToken) token;
                boolean repeat = symbol.flags.contains("r");

                List<Token> tokens = repeat ? repeats.get(symbol.tag) : null;
                if (tokens == null)
                {
                    tokens = symbol.compute(theme, poemContext, random);
                    if (repeat) repeats.put(symbol.tag, tokens);
                }

                // Add it backwards since it's reversed
                for (int i = tokens.size() - 1; i >= 0; i--)
                    queue.addFirst(tokens.get(i));
            }
            else if (token instanceof StringToken)
                builder.append(((StringToken) token).string);
        }

        return builder.toString().trim();
    }

    private static String firstCharUppercase(String name)
    {
        return Character.toString(name.charAt(0)).toUpperCase() + name.substring(1);
    }

    private static <O> O getRandomElementFrom(List<O> list, Random random)
    {
        return list.get(random.nextInt(list.size()));
    }

    public String getTitle()
    {
        return title;
    }

    public String getText()
    {
        return text;
    }

    protected interface Computer
    {
        List<Token> compute(ComputeToken token, Map<String, List<List<Token>>> theme, PoemContext context, Random random);

        static Computer simple(SimpleComputer computer)
        {
            return (token, theme, context, random) ->
                    Collections.singletonList(new StringToken(0, 0,
                            computer.compute(token, theme, context, random)));
        }

        interface SimpleComputer
        {
            String compute(ComputeToken token, Map<String, List<List<Token>>> theme, PoemContext context, Random random);
        }
    }

    public static class Theme
    {
        public Multimap<String, String> contents = HashMultimap.create();

        public static Theme fromFile(String fileContents)
        {
            Theme theme = new Theme();
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
                        currentList = listForTitle(theme, tag);
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

            return theme;
        }

        @Nullable
        protected static Collection<String> listForTitle(Theme theme, String tag)
        {
            return theme.contents.get(tag);
        }

        public Map<String, List<List<Token>>> build()
        {
            HashMultimap<String, List<Token>> map = HashMultimap.create();
            SymbolTokenizer<Token> tokenizer = new SymbolTokenizer<>(
                    new SymbolTokenizer.SimpleCharacterRules('\\', null, new char[0], null),
                    new ReplaceFactory()
            );

            return build(map, tokenizer).asMap().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, k -> Lists.newArrayList(k.getValue())));
        }

        protected Multimap<String, List<Token>> build(Multimap<String, List<Token>> map, SymbolTokenizer<Token> tokenizer)
        {
            for (String include : contents.get("Include"))
            {
                Theme theme = THEME_REGISTRY.get(include);

                if (theme == null)
                {
                    RecurrentComplex.logger.error("Can't find theme to include: " + include);
                    continue;
                }

                theme.build(map, tokenizer);
            }

            map(map, "l", "Lines", tokenizer);
            map(map, "t", "Titles", tokenizer);
            map(map, "1", "Concrete Nouns", tokenizer);
            map(map, "2", "Abstract Nouns", tokenizer);
            map(map, "3", "Present Transitive Verbs", tokenizer);
            map(map, "4", "Past Transitive Verbs", tokenizer);
            map(map, "5", "Present Intransitive Verbs", tokenizer);
            map(map, "6", "Adjectives", tokenizer);
            map(map, "7", "Adverbs", tokenizer);
            map(map, "8", "Prepositions", tokenizer);
            map(map, "9", "Interjections", tokenizer);
            map(map, "10", "Concrete Nouns Plural", tokenizer);

            return map;
        }

        public void map(Multimap<String, List<Token>> map, String key, String title, SymbolTokenizer<Token> tokenizer)
        {
            map.putAll(key, contents.get(title).stream()
                    .<List<Token>>map(s ->
                    {
                        try
                        {
                            return tokenizer.tokenize(s);
                        }
                        catch (ParseException e)
                        {
                            RecurrentComplex.logger.error("Error tokenizing poem", e);
                            return Collections.singletonList(new StringToken(0, s.length(), s));
                        }
                    })
                    .collect(Collectors.toList()));
        }

    }

    protected static abstract class Token extends SymbolTokenizer.Token
    {
        public Token(int startIndex, int endIndex)
        {
            super(startIndex, endIndex);
        }
    }

    protected static class ComputeToken extends Token
    {
        protected String tag;
        protected Set<String> flags;
        protected Computer computer;

        public ComputeToken(int startIndex, int endIndex, String tag, Set<String> flags, Computer computer)
        {
            super(startIndex, endIndex);
            this.tag = tag;
            this.flags = flags;
            this.computer = computer;
        }

        public List<Token> compute(Map<String, List<List<Token>>> theme, PoemContext context, Random random)
        {
            return computer.compute(this, theme, context, random);
        }
    }

    protected static class StringToken extends Token
    {
        public String string;

        public StringToken(int startIndex, int endIndex, String string)
        {
            super(startIndex, endIndex);
            this.string = string;
        }
    }

    protected static class PoemContext
    {
        public List<String> names = new ArrayList<>();
        public List<String> places = new ArrayList<>();


        public boolean add(Random random, List<String> list, float continueChance, String value)
        {
            list.add(value);
            return random.nextFloat() < continueChance;
        }
    }

    protected static class ReplaceFactory implements SymbolTokenizer.TokenFactory
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
                Set<String> flags = Sets.newHashSet(parts.subList(1, parts.size()));

                return new ComputeToken(index, end + 1, tag, flags, computer(tag, flags));
            }

            return null;
        }

        @Nonnull
        protected Computer computer(String tag, Set<String> flags)
        {
            switch (tag)
            {
                case "place":
                    return Computer.simple((token, theme, context, random) ->
                            getRandomElementFrom(context.places, random));
                case "name":
                    return Computer.simple((token, theme, context, random) ->
                            getRandomElementFrom(context.names, random));
                case "lownum":
                    return Computer.simple(numComputer(2, 10, 1));
                case "highnum":
                    return Computer.simple(numComputer(2, 10, 10));
                case "hugenum":
                    return Computer.simple(numComputer(1, 10, 1000));
                default:
                    return (token, theme, context, random) ->
                    {
                        List<List<Token>> list = theme.get(tag);
                        return list == null || list.isEmpty()
                                ? Collections.singletonList(new StringToken(0, 0, "EMPTY"))
                                : getRandomElementFrom(list, random);
                    };
            }
        }

        private Computer.SimpleComputer numComputer(int min, int max, int mul)
        {
            return (token, theme, context, random) ->
                    String.valueOf((random.nextInt(max - min + 1) + min) * mul);
        }

        @Nonnull
        @Override
        public SymbolTokenizer.Token constructStringToken(int index, @Nonnull String string)
        {
            return new StringToken(index, index + string.length(), string);
        }
    }
}
