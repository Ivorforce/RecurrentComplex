/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.blocktransformers.BlockTransformerReplaceAll;
import net.minecraft.block.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBTReplaceAll implements TableDataSource, TableElementPropertyListener
{
    private BlockTransformerReplaceAll blockTransformer;

    public TableDataSourceBTReplaceAll(BlockTransformerReplaceAll blockTransformer)
    {
        this.blockTransformer = blockTransformer;
    }

    public BlockTransformerReplaceAll getBlockTransformer()
    {
        return blockTransformer;
    }

    public void setBlockTransformer(BlockTransformerReplaceAll blockTransformer)
    {
        this.blockTransformer = blockTransformer;
    }

    @Override
    public boolean has(GuiTable table, int index)
    {
        return index >= 0 && index < 4;
    }

    @Override
    public TableElement elementForIndex(GuiTable table, int index)
    {
        if (index == 0)
        {
            TableElementString element = new TableElementString("sourceID", "Source Block", Block.blockRegistry.getNameForObject(blockTransformer.sourceBlock));
            element.setShowsValidityState(true);
            TableDataSourceBTNatural.setStateForBlockTextfield(element);
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 1)
        {
            TableElementInteger element = new TableElementInteger("sourceMeta", "Source Metadata", blockTransformer.sourceMetadata, 0, 16);
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 2)
        {
            TableElementString element = new TableElementString("destID", "Dest Block", Block.blockRegistry.getNameForObject(blockTransformer.destBlock));
            element.setShowsValidityState(true);
            TableDataSourceBTNatural.setStateForBlockTextfield(element);
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 3)
        {
            TableElementString element = new TableElementString("destMeta", "Dest Metadatas (Hex)", byteArrayToHexString(blockTransformer.destMetadata));
            element.addPropertyListener(this);
            return element;
        }

        return null;
    }

    public static String byteArrayToHexString(byte[] bytes)
    {
        StringBuilder builder = new StringBuilder();

        for (byte aByte : bytes)
        {
            builder.append(String.format("%01X", aByte));
        }

        return builder.toString();
    }

    public static byte[] hexStringToByteArray(String string)
    {
        List<Byte> bytes = new ArrayList<>();

        for (int i = 0; i < string.length(); i++)
        {
            char aChar = string.charAt(i);
            byte aByte = (byte) Character.digit(aChar, 16);

            if (aByte >= 0)
            {
                bytes.add(aByte);
            }
        }

        byte[] byteArray = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++)
        {
            byteArray[i] = bytes.get(i);
        }

        return byteArray;
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        if ("sourceID".equals(element.getID()))
        {
            blockTransformer.sourceBlock = (Block) Block.blockRegistry.getObject(element.getPropertyValue());
            TableDataSourceBTNatural.setStateForBlockTextfield(((TableElementString) element));
        }
        else if ("sourceMeta".equals(element.getID()))
        {
            blockTransformer.sourceMetadata = (int) element.getPropertyValue();
        }
        else if ("destID".equals(element.getID()))
        {
            blockTransformer.destBlock = (Block) Block.blockRegistry.getObject(element.getPropertyValue());
            TableDataSourceBTNatural.setStateForBlockTextfield(((TableElementString) element));
        }
        else if ("destMeta".equals(element.getID()))
        {
            String propValue = ((String) element.getPropertyValue());
            blockTransformer.destMetadata = hexStringToByteArray(propValue);
            String newString = byteArrayToHexString(blockTransformer.destMetadata);

            if (!propValue.equalsIgnoreCase(newString))
            {
                element.setPropertyValue(newString);
            }
        }
    }
}
