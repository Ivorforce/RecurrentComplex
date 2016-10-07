/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.random.item;

import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;
import ivorius.ivtoolkit.tools.NBTTagLists;
import ivorius.reccomplex.random.Person;
import ivorius.reccomplex.random.Poem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagString;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lukas on 02.10.16.
 */
public class Book
{
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
        Poem poem = Poem.randomPoem(random, 32);
        Person author = Person.randomHuman(random, random.nextFloat() < 0.9f);

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
        List<Integer> pageIndices = new ArrayList<>();

        int allowedLines = 12;
        int charsPerLine = 15;

        int currentLineChars = 0;
        int currentLineNumber = 0;

        int hardcodedLineIndex = 0;

        String[] hardcodedLines = text.split("\n");
        for (String hardcodedLine : hardcodedLines)
        {
            Scanner scanner = new Scanner(hardcodedLine);
            while (scanner.hasNext())
            {
                String word = scanner.next();

                if (word.length() > charsPerLine)
                {
                    int lines = word.length() / charsPerLine;

                    if (currentLineNumber + lines > allowedLines)
                    {
                        int index = scanner.match().end() + hardcodedLineIndex;
                        pageIndices.add(index);
                        currentLineNumber = 0;
                    }

                    currentLineNumber += lines;
                    currentLineChars = word.length() - lines * charsPerLine;
                }
                else if (word.length() + currentLineChars > charsPerLine)
                {
                    if (currentLineNumber >= allowedLines)
                    {
                        int index = scanner.match().end() + hardcodedLineIndex;
                        pageIndices.add(index);
                        currentLineNumber = 0;
                    }
                    else
                    {
                        currentLineNumber++;
                    }

                    currentLineChars = word.length();
                }
                else
                {
                    currentLineChars += word.length();
                }
            }

            currentLineChars = 0;
            currentLineNumber++;

            hardcodedLineIndex += hardcodedLine.length() + 1;

            if (currentLineNumber >= allowedLines)
            {
                pageIndices.add(hardcodedLineIndex);
                currentLineNumber = 0;
            }
        }

        List<String> pages = new ArrayList<>();
        int lastIndex = 0;
        for (Integer index : pageIndices)
        {
            String newPage = text.substring(lastIndex, index);

            if (newPage.trim().length() > 0)
            {
                pages.add(newPage.trim());
            }

            lastIndex = index;
        }
        if (text.length() > lastIndex)
        {
            String newPage = text.substring(lastIndex, text.length());

            if (newPage.trim().length() > 0)
            {
                pages.add(newPage.trim());
            }
        }

        return pages;
    }

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
