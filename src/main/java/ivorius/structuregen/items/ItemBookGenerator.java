/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.items;

import ivorius.structuregen.random.Person;
import ivorius.structuregen.random.Poem;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by lukas on 18.06.14.
 */
public class ItemBookGenerator extends Item implements GeneratingItem
{
    @Override
    public boolean onItemUse(ItemStack usedItem, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
    {
        return ItemInventoryGenerationTag.applyGeneratorToInventory(world, x, y, z, this, usedItem);
    }

    @Override
    public void generateInInventory(IInventory inventory, Random random, ItemStack stack, int fromSlot)
    {
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
        ItemStack stack = new ItemStack(Items.book);
        String bookName = Person.chaoticName(random, random.nextFloat() < 0.8f);

        stack.setStackDisplayName(bookName);

        return stack;
    }

    public static ItemStack getRandomPoemBook(Random random)
    {
        ItemStack stack = new ItemStack(Items.written_book);
        Poem poem = Poem.randomPoem(random);
        Person author = Person.randomHuman(random, random.nextFloat() < 0.9f);

        NBTTagList pages = new NBTTagList();
        pages.appendTag(new NBTTagString(poem.getText()));

        stack.setTagInfo("pages", pages);
        stack.setTagInfo("author", new NBTTagString(author.getFullName()));
        stack.setTagInfo("title", new NBTTagString(poem.getTitle()));

        return stack;
    }

    public static String[] getPages(String content, FontRenderer fontRenderer, int maxStringWidth, int maxCharacters)
    {
        ArrayList<String> returnList = new ArrayList<>();
        int lastCut = 0;

        for (int i = 0; i < content.length(); )
        {
            int wordEndIndex = !content.substring(i).contains(" ") ? content.length() : i + content.substring(i).indexOf(" ");

            String currentPageString = content.substring(lastCut, wordEndIndex);
            int realLength = fontRenderer.splitStringWidth(currentPageString, maxStringWidth);

            if (!(realLength <= maxStringWidth && currentPageString.length() < maxCharacters))
            {
                returnList.add(content.substring(lastCut, i));
                lastCut = i;
            }

            i = wordEndIndex + 1;
        }
        if (lastCut < content.length())
        {
            returnList.add(content.substring(lastCut, content.length()));
        }

        String[] returnArray = new String[returnList.size()];

        for (int i = 0; i < returnList.size(); i++)
        {
            returnArray[i] = returnList.get(i);
        }

        return returnArray;
    }

    public static String[] getLines(String content, FontRenderer fontRenderer, int maxStringWidth, int maxCharacters)
    {
        ArrayList<String> returnList = new ArrayList<>();
        int lastCut = 0;

        for (int i = 0; i < content.length(); )
        {
            int wordEndIndex = i + 1;
            if (content.substring(i).indexOf(" ") == -1)
            {
                wordEndIndex = content.length();
            }
            else
            {
                wordEndIndex = i + content.substring(i).indexOf(" ");
            }

            String currentPageString = content.substring(lastCut, wordEndIndex);
            int realLength = fontRenderer.getStringWidth(currentPageString);

            if (!(realLength <= maxStringWidth && currentPageString.length() < maxCharacters))
            {
                returnList.add(content.substring(lastCut, i));
                lastCut = i;
            }

            i = wordEndIndex + 1;
        }
        if (lastCut < content.length())
        {
            returnList.add(content.substring(lastCut, content.length()));
        }

        String[] returnArray = new String[returnList.size()];

        for (int i = 0; i < returnList.size(); i++)
        {
            returnArray[i] = returnList.get(i);
        }

        return returnArray;
    }
}
