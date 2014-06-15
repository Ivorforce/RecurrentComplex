package ivorius.structuregen.items;

import ivorius.structuregen.StructureGen;
import ivorius.structuregen.gui.SGGuiHandler;
import ivorius.structuregen.worldgen.inventory.GenericInventoryGenerator;
import ivorius.structuregen.worldgen.inventory.InventoryGenerationHandler;
import ivorius.structuregen.worldgen.inventory.InventoryGenerator;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.List;

public abstract class ItemInventoryGenerationTag extends Item implements GeneratingItem, InventoryGeneratorHolder
{
    public ItemInventoryGenerationTag()
    {
    }

    @Override
    public boolean onItemUse(ItemStack usedItem, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
    {
        TileEntity rightClicked = world.getTileEntity(x, y, z);

        if (rightClicked instanceof IInventory)
        {
            if (!world.isRemote)
            {
                generateInInventory((IInventory) rightClicked, world.rand, usedItem, world.rand.nextInt(((IInventory) rightClicked).getSizeInventory()));
                InventoryGenerationHandler.generateAllTags((IInventory) rightClicked, world.rand);
            }

            return true;
        }

        return false;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        InventoryGenerator generator = inventoryGenerator(stack);

        if (generator != null)
        {
            if (!world.isRemote)
            {
                if (!generator.openEditGUI(stack, player, player.inventory.currentItem))
                {
                    GenericInventoryGenerator genericInventoryGenerator = generator.copyAsGenericInventoryGenerator();

                    if (genericInventoryGenerator != null)
                    {
                        genericInventoryGenerator.openEditGUI(stack, player, player.inventory.currentItem);
                    }
                }
            }
        }
        else if (inventoryGeneratorKey(stack) == null)
        {
            player.openGui(StructureGen.instance, SGGuiHandler.editInventoryGen, world, player.inventory.currentItem, 0, 0);
        }

        return super.onItemRightClick(stack, world, player);
    }

    public static String inventoryGeneratorKey(ItemStack stack)
    {
        if (stack.hasDisplayName())
        {
            return stack.getDisplayName();
        }

        return null;
    }

    public static InventoryGenerator inventoryGenerator(ItemStack stack)
    {
        return InventoryGenerationHandler.generator(inventoryGeneratorKey(stack));
    }

    @Override
    public void getSubItems(Item item, CreativeTabs creativeTabs, List list)
    {
        super.getSubItems(item, creativeTabs, list);

        for (String key : InventoryGenerationHandler.allInventoryGeneratorKeys())
        {
            ItemStack stack = new ItemStack(item);
            setItemStackGeneratorKey(stack, key);
            list.add(stack);
        }
    }

    public static void setItemStackGeneratorKey(ItemStack stack, String generatorKey)
    {
        stack.setStackDisplayName(generatorKey);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advancedInformation)
    {
        InventoryGenerator generator = inventoryGenerator(stack);
        if (generator != null)
        {
            generator.addInformation(stack, player, list, advancedInformation);
        }
        else
        {
            list.add(StatCollector.translateToLocal("inventoryGen.create"));
        }
    }

    @Override
    public String inventoryKey(ItemStack stack)
    {
        return inventoryGeneratorKey(stack);
    }
}
