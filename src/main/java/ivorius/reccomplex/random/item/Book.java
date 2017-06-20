/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.random.item;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;
import ivorius.ivtoolkit.tools.NBTTagLists;
import ivorius.reccomplex.random.Person;
import ivorius.reccomplex.random.Poem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagString;
import org.apache.commons.lang3.text.WordUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by lukas on 02.10.16.
 */
public class Book
{

    public static final int LINES_PER_PAGE = 12;
    public static final int CHARS_PER_LINE = 18;

    public static ItemStack any(Random random)
    {
        return random.nextFloat() < 0.5f ? generic(random) : poem(random);
    }

    public static ItemStack generic(Random random)
    {
        ItemStack stack = new ItemStack(Items.BOOK);
        String bookName = Person.chaoticName(random, random.nextFloat() < 0.8f);

        stack.setStackDisplayName(bookName);

        return stack;
    }

    public static ItemStack poem(Random random)
    {
        ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
        Person author = Person.randomHuman(random, random.nextFloat() < 0.9f);
        Poem poem = Poem.randomPoem(random, 32, author);

        stack.setTagInfo("pages", NBTTagLists.write(bookPages(poem.getText()).stream()
                .map(Book::toJSON).map(NBTTagString::new).collect(Collectors.toList())));
        stack.setTagInfo("author", new NBTTagString(author.getFullName()));
        stack.setTagInfo("title", new NBTTagString(poem.getTitle()));

        return stack;
    }

    public static String toJSON(String text)
    {
        return new Gson().toJson(new JsonPrimitive(text));
    }

    public static List<String> bookPages(String text)
    {
        List<String> lines = Arrays.stream(text.split("\n"))
                // Add manual newlines
                .map(line -> line + "\n")
                // Split hardcoded lines into final lines
                .flatMap(line -> Arrays.stream(WordUtils.wrap(line, CHARS_PER_LINE, "\r", true)
                        // Add back spaces that were deleted on wrap
                        .replaceAll("\r", " \r")
                        .split("\r")))
                .collect(Collectors.toList());
        // Partition by page
        return Lists.partition(lines, LINES_PER_PAGE).stream()
                .map(pageLines -> pageLines.stream().reduce("", (l, r) -> l + r))
                .collect(Collectors.toList());
    }

    // With FontRenderer
    //    public static String[] getPages(String content, FontRenderer fontRenderer, int maxStringWidth, int maxCharacters)
//    {
//        ArrayList<String> returnList = new ArrayList<>();
//        int lastCut = 0;
//
//        for (int i = 0; i < content.length(); )
//        {
//            int wordEndIndex = !content.substring(i).contains(" ") ? content.length() : i + content.substring(i).indexOf(" ");
//
//            String currentPageString = content.substring(lastCut, wordEndIndex);
//            int realLength = fontRenderer.splitStringWidth(currentPageString, maxStringWidth);
//
//            if (!(realLength <= maxStringWidth && currentPageString.length() < maxCharacters))
//            {
//                returnList.add(content.substring(lastCut, i));
//                lastCut = i;
//            }
//
//            i = wordEndIndex + 1;
//        }
//        if (lastCut < content.length())
//        {
//            returnList.add(content.substring(lastCut, content.length()));
//        }
//
//        String[] returnArray = new String[returnList.size()];
//
//        for (int i = 0; i < returnList.size(); i++)
//        {
//            returnArray[i] = returnList.get(i);
//        }
//
//        return returnArray;
//    }
//
//    public static String[] getLines(String content, FontRenderer fontRenderer, int maxStringWidth, int maxCharacters)
//    {
//        ArrayList<String> returnList = new ArrayList<>();
//        int lastCut = 0;
//
//        for (int i = 0; i < content.length(); )
//        {
//            int wordEndIndex = i + 1;
//            if (content.substring(i).indexOf(" ") == -1)
//            {
//                wordEndIndex = content.length();
//            }
//            else
//            {
//                wordEndIndex = i + content.substring(i).indexOf(" ");
//            }
//
//            String currentPageString = content.substring(lastCut, wordEndIndex);
//            int realLength = fontRenderer.getStringWidth(currentPageString);
//
//            if (!(realLength <= maxStringWidth && currentPageString.length() < maxCharacters))
//            {
//                returnList.add(content.substring(lastCut, i));
//                lastCut = i;
//            }
//
//            i = wordEndIndex + 1;
//        }
//        if (lastCut < content.length())
//        {
//            returnList.add(content.substring(lastCut, content.length()));
//        }
//
//        String[] returnArray = new String[returnList.size()];
//
//        for (int i = 0; i < returnList.size(); i++)
//        {
//            returnArray[i] = returnList.get(i);
//        }
//
//        return returnArray;
//    }
}
