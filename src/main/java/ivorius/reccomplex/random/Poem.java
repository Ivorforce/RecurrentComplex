/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.random;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import ivorius.reccomplex.files.SimpleLeveledRegistry;
import ivorius.reccomplex.utils.tokenizer.SymbolTokenizer;
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

    public static Poem randomPoem(Random random, Integer maxTitleLength, Person author)
    {
        Map<String, List<List<TokenReplacer.Token>>> stitchedTheme = stitchStyledTheme(author);

        PoemContext poemContext = new PoemContext();
        //noinspection StatementWithEmptyBody
        while (poemContext.add(random, poemContext.names, 0.3f, Person.randomHuman(random, random.nextBoolean()).getFirstName()))
            ;
        //noinspection StatementWithEmptyBody
        while (poemContext.add(random, poemContext.places, 0.3f, Place.randomPlace(random).getFullPlaceType()))
            ;

        String title = randomTitle(random, poemContext, maxTitleLength, stitchedTheme);
        String phrase = evaluate(random, stitchedTheme.get("text"), poemContext, stitchedTheme);

        return new Poem(title, phrase);
    }

    public static Map<String, List<List<TokenReplacer.Token>>> stitchStyledTheme(Person author)
    {
        Random styleRandom = new Random(author.hashCode());

        SymbolTokenizer<TokenReplacer.Token> tokenizer = new SymbolTokenizer<>(
                new SymbolTokenizer.SimpleCharacterRules('\\', null, new char[0], null),
                factory()
        );

        // Stitch together from random themes
        HashMultimap<String, List<TokenReplacer.Token>> map = HashMultimap.create();
        do
        {
            Theme theme = getRandomElementFrom(THEME_REGISTRY.allActive().stream().collect(Collectors.toList()), styleRandom);
            float acceptance = styleRandom.nextFloat();

            Multimap<String, List<TokenReplacer.Token>> builtTheme = theme.build(HashMultimap.create(), tokenizer);

            // remove roughly acceptance% words but never all
            builtTheme.asMap().values()
                    .forEach(v -> v.removeIf(p -> styleRandom.nextFloat() < acceptance && v.size() > 1));

            map.putAll(builtTheme);
        }
        while (styleRandom.nextBoolean());

        return Theme.build(map);
    }

    @Nonnull
    protected static String randomTitle(Random random, PoemContext poemContext, Integer maxLength, Map<String, List<List<TokenReplacer.Token>>> built)
    {
        for (int i = 0; i < TITLE_TRIES; i++)
        {
            String title = evaluate(random, built.get("title"), poemContext, built).trim();
            if (maxLength == null || title.length() < maxLength)
                return title;
        }

        return StringUtils.abbreviate(evaluate(random, built.get("title"), poemContext, built).trim(), maxLength);
    }

    private static String evaluate(Random random, List<List<TokenReplacer.Token>> patterns, PoemContext context, Map<String, List<List<TokenReplacer.Token>>> theme)
    {
        return TokenReplacer.evaluate(random, getRandomElementFrom(patterns, random), context, theme);
    }

    private static <O> O getRandomElementFrom(List<O> list, Random random)
    {
        return list.get(random.nextInt(list.size()));
    }

    @Nonnull
    protected static TokenReplacer.ReplaceFactory factory()
    {
        return new TokenReplacer.ReplaceFactory<PoemContext>()
        {
            @Nonnull
            protected TokenReplacer.Exploder<PoemContext> exploder(String tag, List<String> flags)
            {
                switch (tag)
                {
                    case "br":
                        return TokenReplacer.Exploder.string((token, theme, context, random) -> "\n");
                    case "place":
                        return TokenReplacer.Exploder.string((token, theme, context, random) ->
                                Poem.getRandomElementFrom(context.places, random));
                    case "name":
                        return TokenReplacer.Exploder.string((token, theme, context, random) ->
                                Poem.getRandomElementFrom(context.names, random));
                    case "number":
                        return TokenReplacer.Exploder.string(numEvaluator(
                                Integer.valueOf(TokenReplacer.Theme.flag(flags, 0, "2")),
                                Integer.valueOf(TokenReplacer.Theme.flag(flags, 1, "10")),
                                Integer.valueOf(TokenReplacer.Theme.flag(flags, 2, "1"))
                        ));
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

            private TokenReplacer.Exploder.StringExploder<PoemContext> numEvaluator(int min, int max, int mul)
            {
                return (token, theme, context, random) ->
                        String.valueOf((random.nextInt(max - min + 1) + min) * mul);
            }
        };
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

        @Override
        protected TokenReplacer.Theme getOther(String include)
        {
            return Poem.THEME_REGISTRY.get(include);
        }
    }
}
