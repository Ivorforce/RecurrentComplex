/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.random;

import com.google.common.io.LineReader;
import ivorius.structuregen.StructureGen;
import ivorius.structuregen.ivtoolkit.tools.IvFileHelper;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * Created by lukas on 25.06.14.
 */
public class Poem
{
    public static final List<String> sentencePatterns = Arrays.asList("As the <1>s are, the <1>s <4> <7>.",
            "All the <1>s <3> <6>, so <5> the <1>s.",
            "<9>! We <4> the <1> and the <2>, why not <5>?",
            "What is the <6> <2> to <7> <3> the <1>?",
            "To <3>, we <4>. To <3>, we <4>.",
            "Sometimes <6> and always <6>.",
            "The <6> <1> <7> <3>s a <1>.",
            "<7> <6>, <1>s <7> <3> a <6> <1>.",
            "<2> is a <6> <1> <8> <2> and <2>.",
            "<9>! <2>, the <6> <2>.",
            "<1>s <4> the <1> <8> the <6> <2>, <7> but <7>.",
            "<1> and <1>, <9>, <9>! <1>s like the <2>.",
            "<1>s <5> and <8> them <2> <5>s!",
            "Only the <1> <5>s as a <6> <1>.",
            "<9> <1>s...",
            "What is <2> after all...",
            "We <5>, but only for a while,",
            "When do <1>s become <1>s?",
            "Count the <1>s, <3> the <2>.",
            "Though it's now more <6> and less <6>.",
            "Yet there's <2> <8> the <2>s and the <1>s.",
            "<1>s -- <6> <1>s!",
            "<1>s <4> <1>s like <6> <1>s <5>.",
            "Why did the <1> <3> it, to <3> the <2>?",
            "How does the <1> not <5>?",
            "<8> or <8>, how <2> <5>s on.",
            "<7>, <7>, <7>.",
            "<5> <7> like a <6> <1> <8> <2>.",
            "<2>, <2>, and ever <2>.",
            "They never <3> the <1>s nor the <1>s, with <2>.",
            "Where is the <6> <1>, the <6> <2> now?",
            "Where was the <6> <2> then?",
            "Ever to <3> a <1>, it <4> a <1>.");

    public static final Map<String, Theme> themes = new HashMap<>();

    private String title;
    private String text;

    public Poem(String title, String text)
    {
        this.title = title;
        this.text = text;
    }

    public static void registerThemes(String modid, String... themeNames)
    {
        for (String name : themeNames)
        {
            Theme theme = Theme.themeFromMod(new ResourceLocation(modid, "poemThemes/" + name + ".txt"));

            if (theme != null)
            {
                themes.put(name, theme);
            }
        }
    }

    public static Poem randomPoem(Random random)
    {
        return randomPoem(random, getRandomElementFrom(Arrays.asList(themes.values().toArray(new Theme[themes.size()])), random));
    }

    public static Poem randomPoem(Random random, Theme theme)
    {
        String title = getRandomPhrase(random, theme, sentencePatterns);
        StringBuilder poem = new StringBuilder();

//        int verses = random.nextInt(5) + 1; // TODO Increase number when custom books are implemented
        int verses = 1;
        for (int verse = 0; verse < verses; verse++)
        {
//            int lines = random.nextInt(10) + 1;
            int lines = random.nextInt(7) + 1;
            for (int line = 0; line < lines; line++)
            {
                poem.append(getRandomPhrase(random, theme, sentencePatterns)).append("\n");
            }

            poem.append("\n");
        }

        return new Poem(title, poem.toString());
    }

    private static String getRandomPhrase(Random random, Theme theme, List<String> sentencePatterns)
    {
        return replaceAllWords(random, getRandomElementFrom(sentencePatterns, random), theme);
    }

    private static String replaceAllWords(Random random, String text, Theme theme)
    {
        StringBuilder builder = new StringBuilder(text);

        replaceAll(random, builder, "<1>", theme.concreteNouns);
        replaceAll(random, builder, "<2>", theme.abstractNouns);
        replaceAll(random, builder, "<3>", theme.transitivePresentVerbs);
        replaceAll(random, builder, "<4>", theme.transitivePastVerbs);
        replaceAll(random, builder, "<5>", theme.intransitivePresentVerbs);
        replaceAll(random, builder, "<6>", theme.adjectives);
        replaceAll(random, builder, "<7>", theme.adverbs);
        replaceAll(random, builder, "<8>", theme.prepositions);
        replaceAll(random, builder, "<9>", theme.interjections);

        return builder.toString();
    }

    private static void replaceAll(Random random, StringBuilder builder, String tag, List<String> words)
    {
        int tagLength = tag.length();
        int index;
        while ((index = builder.indexOf(tag)) >= 0)
        {
            builder.replace(index, index + tagLength, getRandomElementFrom(words, random));
        }
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
        public List<String> concreteNouns = new ArrayList<>();
        public List<String> abstractNouns = new ArrayList<>();
        public List<String> transitivePresentVerbs = new ArrayList<>();
        public List<String> transitivePastVerbs = new ArrayList<>();
        public List<String> intransitivePresentVerbs = new ArrayList<>();
        public List<String> adjectives = new ArrayList<>();
        public List<String> adverbs = new ArrayList<>();
        public List<String> prepositions = new ArrayList<>();
        public List<String> interjections = new ArrayList<>();

        public static Theme themeFromMod(ResourceLocation resourceLocation)
        {
            try
            {
                return themeFromFile(IOUtils.toString(IvFileHelper.inputStreamFromResourceLocation(resourceLocation), "UTF-8"));
            }
            catch (IOException e)
            {
                StructureGen.logger.error(e);
            }

            return null;
        }

        public static Theme themeFromFile(String fileContents)
        {
            Theme theme = new Theme();
            LineReader reader = new LineReader(new StringReader(fileContents));

            List<String> currentList = null;
            String line;
            try
            {
                while ((line = reader.readLine()) != null)
                {
                    if (line.startsWith("***"))
                    {
                        String tag = line.substring(4).trim();
                        switch (tag)
                        {
                            case "Concrete Nouns":
                                currentList = theme.concreteNouns;
                                break;
                            case "Abstract Nouns":
                                currentList = theme.abstractNouns;
                                break;
                            case "Present Transitive Verbs":
                                currentList = theme.transitivePresentVerbs;
                                break;
                            case "Past Transitive Verbs":
                                currentList = theme.transitivePastVerbs;
                                break;
                            case "Present Intransitive Verbs":
                                currentList = theme.intransitivePresentVerbs;
                                break;
                            case "Adjectives":
                                currentList = theme.adjectives;
                                break;
                            case "Adverbs":
                                currentList = theme.adverbs;
                                break;
                            case "Prepositions":
                                currentList = theme.prepositions;
                                break;
                            case "Interjections":
                                currentList = theme.interjections;
                                break;
                            default:
                                currentList = null;
                        }
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
                StructureGen.logger.error(e);
            }

            assertContents(theme.concreteNouns);
            assertContents(theme.abstractNouns);
            assertContents(theme.transitivePresentVerbs);
            assertContents(theme.transitivePastVerbs);
            assertContents(theme.intransitivePresentVerbs);
            assertContents(theme.adjectives);
            assertContents(theme.adverbs);
            assertContents(theme.prepositions);
            assertContents(theme.interjections);

            return theme;
        }

        private static void assertContents(List<String> list)
        {
            if (list.size() == 0)
            {
                list.add("MISSING");
            }
        }
    }
}
