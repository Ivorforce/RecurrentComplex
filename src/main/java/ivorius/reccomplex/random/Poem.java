/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.random;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.io.LineReader;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.files.SimpleLeveledRegistry;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.StringReader;
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

        String title = randomTitle(random, theme, poemContext, maxTitleLength);
        char titleLastChar = title.charAt(title.length() - 1);
        if (titleLastChar == '.' || titleLastChar == ',' || titleLastChar == ';')
            title = title.substring(0, title.length() - 1);

        StringBuilder poem = new StringBuilder();

        Map<String, List<String>> built = theme.build();

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
    protected static String randomTitle(Random random, Theme theme, PoemContext poemContext, Integer maxLength)
    {
        Map<String, List<String>> built = theme.build();

        for (int i = 0; i < TITLE_TRIES; i++)
        {
            String title = getRandomPhrase(random, built.get("t"), poemContext, built).trim();
            if (maxLength == null || title.length() < maxLength)
                return title;
        }

        return StringUtils.abbreviate(getRandomPhrase(random, built.get("t"), poemContext, built).trim(), maxLength);
    }

    private static String getRandomPhrase(Random random, List<String> sentencePatterns, PoemContext poemContext, Map<String, List<String>> theme)
    {
        return firstCharUppercase(replaceAllWords(random, getRandomElementFrom(sentencePatterns, random), poemContext, theme));
    }

    private static String replaceAllWords(Random random, String text, PoemContext poemContext, Map<String, List<String>> theme)
    {
        StringBuilder builder = new StringBuilder(text);

        for (Map.Entry<String, List<String>> entry : theme.entrySet())
            replaceAll(random, builder, entry.getValue(), entry.getKey());

        replaceAll(random, builder, poemContext.names, "name");
        replaceAll(random, builder, poemContext.places, "place");
        replaceAllWithNums(random, builder, "lownum", 2, 10, 1);
        replaceAllWithNums(random, builder, "highnum", 2, 10, 10);
        replaceAllWithNums(random, builder, "hugenum", 1, 10, 1000);

        return builder.toString();
    }

    private static void replaceAll(Random random, StringBuilder builder, List<String> words, String tag)
    {
        String repeatWord = null;
        int index;
        while ((index = builder.indexOf("<" + tag)) >= 0)
        {
            int endIndex = builder.indexOf(">", index);
            if (builder.charAt(endIndex - 1) == 'r')
            {
                if (repeatWord == null)
                {
                    repeatWord = getRandomElementFrom(words, random);
                }

                builder.replace(index, endIndex + 1, repeatWord);
            }
            else
            {
                builder.replace(index, endIndex + 1, getRandomElementFrom(words, random));
            }
        }
    }

    private static void replaceAllWithNums(Random random, StringBuilder builder, String tag, int min, int max, int mul)
    {
        String repeatWord = null;
        int index;
        while ((index = builder.indexOf("<" + tag)) >= 0)
        {
            int endIndex = builder.indexOf(">", index);
            if (builder.charAt(endIndex - 1) == 'r')
            {
                if (repeatWord == null)
                {
                    repeatWord = String.valueOf((random.nextInt(max - min + 1) + min) * mul);
                }

                builder.replace(index, endIndex + 1, repeatWord);
            }
            else
            {
                builder.replace(index, endIndex + 1, String.valueOf((random.nextInt(max - min + 1) + min) * mul));
            }
        }
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

        private static void assertContents(List<String> list)
        {
            if (list.size() == 0)
            {
                list.add("MISSING");
            }
        }

        public Map<String, List<String>> build()
        {
            return build(HashMultimap.create()).asMap().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, k -> Lists.newArrayList(k.getValue())));
        }

        protected Multimap<String, String> build(Multimap<String, String> map)
        {
            for (String include : contents.get("Include"))
            {
                Theme theme = THEME_REGISTRY.get(include);

                if (theme == null)
                {
                    RecurrentComplex.logger.error("Can't find theme to include: " + include);
                    continue;
                }

                theme.build(map);
            }

            map(map, "l", "Lines");
            map(map, "t", "Titles");
            map(map, "1", "Concrete Nouns");
            map(map, "2", "Abstract Nouns");
            map(map, "3", "Present Transitive Verbs");
            map(map, "4", "Past Transitive Verbs");
            map(map, "5", "Present Intransitive Verbs");
            map(map, "6", "Adjectives");
            map(map, "7", "Adverbs");
            map(map, "8", "Prepositions");
            map(map, "9", "Interjections");
            map(map, "10", "Concrete Nouns Plural");

            return map;
        }

        public void map(Multimap<String, String> map, String key, String title)
        {
            map.putAll(key, contents.get(title));
        }
    }

    private static class PoemContext
    {
        public List<String> names = new ArrayList<>();
        public List<String> places = new ArrayList<>();


        public boolean add(Random random, List<String> list, float continueChance, String value)
        {
            list.add(value);
            return random.nextFloat() < continueChance;
        }
    }
}
