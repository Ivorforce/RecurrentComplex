/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.random;

import ivorius.reccomplex.files.SimpleLeveledRegistry;
import ivorius.reccomplex.utils.tokenizer.TokenReplacer;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
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

    public static Poem randomPoem(Random random, Integer maxTitleLength, TokenReplacer.Theme theme)
    {
        PoemContext poemContext = new PoemContext();
        //noinspection StatementWithEmptyBody
        while (poemContext.add(random, poemContext.names, 0.3f, Person.randomHuman(random, random.nextBoolean()).getFirstName()))
            ;
        //noinspection StatementWithEmptyBody
        while (poemContext.add(random, poemContext.places, 0.3f, Place.randomPlace(random).getFullPlaceType()))
            ;

        Map<String, List<List<TokenReplacer.Token>>> built = theme.build();

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
                String phrase = getRandomPhrase(random, built.get("line"), poemContext, built);

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
    protected static String randomTitle(Random random, PoemContext poemContext, Integer maxLength, Map<String, List<List<TokenReplacer.Token>>> built)
    {
        for (int i = 0; i < TITLE_TRIES; i++)
        {
            String title = getRandomPhrase(random, built.get("title"), poemContext, built).trim();
            if (maxLength == null || title.length() < maxLength)
                return title;
        }

        return StringUtils.abbreviate(getRandomPhrase(random, built.get("title"), poemContext, built).trim(), maxLength);
    }

    private static String getRandomPhrase(Random random, List<List<TokenReplacer.Token>> sentencePatterns, PoemContext poemContext, Map<String, List<List<TokenReplacer.Token>>> theme)
    {
        return firstCharUppercase(TokenReplacer.compute(random, getRandomElementFrom(sentencePatterns, random), poemContext, theme));
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

    public static class PoemContext
    {
        public List<String> names = new ArrayList<>();
        public List<String> places = new ArrayList<>();


        public boolean add(Random random, List<String> list, float continueChance, String value)
        {
            list.add(value);
            return random.nextFloat() < continueChance;
        }
    }

    public static class Theme extends TokenReplacer.Theme
    {
        public static Theme fromFile(String fileContents)
        {
            Theme theme = new Theme();
            theme.read(fileContents);
            return theme;
        }

        @Nonnull
        @Override
        protected TokenReplacer.ReplaceFactory factory()
        {
            return new TokenReplacer.ReplaceFactory<PoemContext>()
            {
                @Nonnull
                protected TokenReplacer.Computer<PoemContext> computer(String tag, Set<String> flags)
                {
                    switch (tag)
                    {
                        case "place":
                            return TokenReplacer.Computer.simple((token, theme, context, random) ->
                                    Poem.getRandomElementFrom(context.places, random));
                        case "name":
                            return TokenReplacer.Computer.simple((token, theme, context, random) ->
                                    Poem.getRandomElementFrom(context.names, random));
                        case "lownum":
                            return TokenReplacer.Computer.simple(numComputer(2, 10, 1));
                        case "highnum":
                            return TokenReplacer.Computer.simple(numComputer(2, 10, 10));
                        case "hugenum":
                            return TokenReplacer.Computer.simple(numComputer(1, 10, 1000));
                        default:
                            return (token, theme, context, random) ->
                            {
                                List<List<TokenReplacer.Token>> list = theme.get(tag);
                                return list == null || list.isEmpty()
                                        ? Collections.singletonList(new TokenReplacer.StringToken(0, 0, "EMPTY"))
                                        : Poem.getRandomElementFrom(list, random);
                            };
                    }
                }

                private TokenReplacer.Computer.SimpleComputer<Poem.PoemContext> numComputer(int min, int max, int mul)
                {
                    return (token, theme, context, random) ->
                            String.valueOf((random.nextInt(max - min + 1) + min) * mul);
                }
            };
        }

        @Override
        protected TokenReplacer.Theme getOther(String include)
        {
            return Poem.THEME_REGISTRY.get(include);
        }
    }
}
