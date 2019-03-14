/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.random;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import ivorius.reccomplex.RecurrentComplex;
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
        StitchedTheme theme = StitchedTheme.random(author);

        PoemContext poemContext = new PoemContext();
        //noinspection StatementWithEmptyBody
        while (poemContext.add(random, poemContext.names, 0.3f, Person.randomHuman(random, random.nextBoolean()).getFirstName()))
            ;
        //noinspection StatementWithEmptyBody
        while (poemContext.add(random, poemContext.places, 0.3f, Place.randomPlace(random).getFullPlaceType()))
            ;

        String title = randomTitle(random, poemContext, maxTitleLength, theme);
        String phrase = null;

        try {
            phrase = evaluate(random, theme.get("text"), poemContext, theme);
        }
        catch (TokenReplacer.OverpoemedException e) {
            RecurrentComplex.logger.error(String.format("Too much text with theme: %s", theme));
            phrase = String.format("ERROR: %s", theme);
        }

        return new Poem(title, phrase);
    }

    @Nonnull
    protected static String randomTitle(Random random, PoemContext poemContext, Integer maxLength, StitchedTheme theme)
    {
        for (int i = 0; i <= TITLE_TRIES; i++) {
            String title;

            try {
                title = evaluate(random, theme.get("title"), poemContext, theme).trim();
            }
            catch (TokenReplacer.OverpoemedException e) {
                RecurrentComplex.logger.error(String.format("Too much title with theme: %s", theme));
                return String.format("ERROR: %s", theme);
            }

            if (i == TITLE_TRIES) {
                return StringUtils.abbreviate(title, maxLength);
            }

            if (maxLength == null || title.length() < maxLength)
                return title;
        }

        throw new RuntimeException("Shouldn't be here");
    }

    private static String evaluate(Random random, List<List<TokenReplacer.Token>> patterns, PoemContext context, StitchedTheme theme) throws TokenReplacer.OverpoemedException
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
            protected TokenReplacer.Exploder<PoemContext> exploder(String tag, List<String> params)
            {
                switch (tag) {
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
                                Integer.valueOf(TokenReplacer.Theme.parameter(params, 0, "2")),
                                Integer.valueOf(TokenReplacer.Theme.parameter(params, 1, "10")),
                                Integer.valueOf(TokenReplacer.Theme.parameter(params, 2, "1"))
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

    public static class StitchedTheme
    {
        protected Map<String, List<List<TokenReplacer.Token>>> converters;
        protected List<String> titles = new ArrayList<>();

        public static StitchedTheme random(Person author)
        {
            Random styleRandom = new Random(author.hashCode());

            SymbolTokenizer<TokenReplacer.Token> tokenizer = new SymbolTokenizer<>(
                    new SymbolTokenizer.SimpleCharacterRules('\\', null, new char[0], null),
                    factory()
            );

            StitchedTheme stitchedTheme = new StitchedTheme();

            // Stitch together from random themes
            HashMultimap<String, List<TokenReplacer.Token>> map = HashMultimap.create();
            do {
                Theme theme = getRandomElementFrom(new ArrayList<>(THEME_REGISTRY.allActive()), styleRandom);
                stitchedTheme.titles.add(THEME_REGISTRY.id(theme));

                float acceptance = styleRandom.nextFloat();

                Multimap<String, List<TokenReplacer.Token>> builtTheme = theme.build(HashMultimap.create(), tokenizer);

                // remove roughly acceptance% words but never all
                builtTheme.asMap().values()
                        .forEach(v -> v.removeIf(p -> styleRandom.nextFloat() < acceptance && v.size() > 1));

                map.putAll(builtTheme);
            }
            while (styleRandom.nextBoolean());

            stitchedTheme.converters = build(map);
            return stitchedTheme;
        }

        public static Map<String, List<List<TokenReplacer.Token>>> build(Multimap<String, List<TokenReplacer.Token>> build)
        {
            return build.asMap().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, k -> Lists.newArrayList(k.getValue())));
        }

        public List<List<TokenReplacer.Token>> get(String title)
        {
            return converters.get(title);
        }

        @Override
        public String toString()
        {
            return String.format("Stitched: %s", titles);
        }
    }
}
