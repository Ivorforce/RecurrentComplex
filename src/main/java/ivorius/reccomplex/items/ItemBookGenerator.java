/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.items;

import ivorius.reccomplex.events.ItemGenerationEvent;
import ivorius.reccomplex.random.Person;
import ivorius.reccomplex.random.Poem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by lukas on 18.06.14.
 */
public class ItemBookGenerator extends Item implements GeneratingItem
{
    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (!worldIn.isRemote)
            return ItemInventoryGenerationTag.applyGeneratorToInventory((WorldServer) worldIn, pos, this, stack) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;

        return super.onItemUse(stack, playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void generateInInventory(WorldServer server, IInventory inventory, Random random, ItemStack stack, int fromSlot)
    {
        if (!MinecraftForge.EVENT_BUS.post(new ItemGenerationEvent.Book(server, inventory, random, stack, fromSlot)))
            inventory.setInventorySlotContents(fromSlot, getRandomBook(random));
    }

    public static ItemStack getRandomBook(Random random)
    {
        return random.nextFloat() < 0.5f ? getRandomLoreBook(random) : getRandomPoemBook(random);
    }

    public static ItemStack getRandomLoreBook(Random random)
    {
        // TODO Create written book with text
        return getRandomGenericBook(random);
    }

    public static ItemStack getRandomGenericBook(Random random)
    {
        ItemStack stack = new ItemStack(Items.BOOK);
        String bookName = Person.chaoticName(random, random.nextFloat() < 0.8f);

        stack.setStackDisplayName(bookName);

        return stack;
    }

    public static ItemStack getRandomPoemBook(Random random)
    {
        ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
        Poem poem = Poem.randomPoem(random);
        Person author = Person.randomHuman(random, random.nextFloat() < 0.9f);

        stack.setTagInfo("pages", stringList(bookPages(poem.getText())));
        stack.setTagInfo("author", new NBTTagString(author.getFullName()));
        stack.setTagInfo("title", new NBTTagString(poem.getTitle()));

        return stack;
    }

    public static NBTTagList stringList(List<String> strings)
    {
        NBTTagList list = new NBTTagList();

        for (String s : strings)
        {
            list.appendTag(new NBTTagString(s));
        }

        return list;
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
