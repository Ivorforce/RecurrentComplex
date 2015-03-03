/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.blocktransformers;

import ivorius.reccomplex.gui.editstructure.TableDataSourceDimensionGen;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.blocktransformers.BlockTransformerReplace;
import net.minecraft.block.Block;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBTReplace extends TableDataSourceSegmented implements TableElementPropertyListener
{
    private BlockTransformerReplace blockTransformer;

    private TableElementTitle parsed;

    public TableDataSourceBTReplace(BlockTransformerReplace blockTransformer)
    {
        this.blockTransformer = blockTransformer;
    }

    public BlockTransformerReplace getBlockTransformer()
    {
        return blockTransformer;
    }

    public void setBlockTransformer(BlockTransformerReplace blockTransformer)
    {
        this.blockTransformer = blockTransformer;
    }

    @Override
    public int numberOfSegments()
    {
        return 2;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return 2;
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            if (index == 0)
            {
                TableElementString element = new TableElementString("source", "Sources", blockTransformer.sourceMatcher.getExpression());
                element.addPropertyListener(this);
                return element;
            }
            else if (index == 1)
                return parsed = new TableElementTitle("parsed", "", StringUtils.abbreviate(TableDataSourceDimensionGen.parsedString(blockTransformer.sourceMatcher), 60));
        }
        else if (segment == 1)
        {
            if (index == 0)
            {
                TableElementString element = new TableElementString("destID", "Dest Block", Block.blockRegistry.getNameForObject(blockTransformer.destBlock));
                element.setShowsValidityState(true);
                TableDataSourceBTNatural.setStateForBlockTextfield(element);
                element.addPropertyListener(this);
                return element;
            }
            else if (index == 1)
            {
                TableElementString element = new TableElementString("destMeta", "Dest Metadatas (Hex)", byteArrayToHexString(blockTransformer.destMetadata));
                element.addPropertyListener(this);
                return element;
            }
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
                bytes.add(aByte);
        }

        byte[] byteArray = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++)
            byteArray[i] = bytes.get(i);

        return byteArray;
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        if ("source".equals(element.getID()))
        {
            blockTransformer.sourceMatcher.setExpression((String) element.getPropertyValue());
            if (parsed != null)
                parsed.setDisplayString(StringUtils.abbreviate(TableDataSourceDimensionGen.parsedString(blockTransformer.sourceMatcher), 60));
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
                element.setPropertyValue(newString);
        }
    }
}
