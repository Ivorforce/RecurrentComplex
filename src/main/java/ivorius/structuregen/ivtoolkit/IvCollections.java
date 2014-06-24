/*
 * Copyright (c) 2014, Lukas Tenbrink.
 * http://lukas.axxim.net
 *
 * You are free to:
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes, unless you have a permit by the creator.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package ivorius.structuregen.ivtoolkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 22.06.14.
 */
public class IvCollections
{
    public static <O> ArrayList<O> modifiableCopy(List<O> list)
    {
        ArrayList<O> newList = new ArrayList<>();
        newList.addAll(list);
        return newList;
    }

    public static <O> ArrayList<O> modifiableCopyWithout(List<O> list, int... removeIndices)
    {
        int[] newIndices = removeIndices.clone();
        Arrays.sort(removeIndices);

        ArrayList<O> newList = modifiableCopy(list);

        for (int i = newIndices.length - 1; i >= 0; i--)
        {
            newList.remove(newIndices[i]);
        }

        return newList;
    }

    @SafeVarargs
    public static <O> ArrayList<O> modifiableCopyWith(List<O> list, O... objects)
    {
        ArrayList<O> newList = modifiableCopy(list);
        Collections.addAll(newList, objects);
        return newList;
    }

    public static <O> void setContentsOfList(List<O> list, List<O> contents)
    {
        list.clear();
        list.addAll(contents);
    }
}
