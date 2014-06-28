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

package ivorius.ivtoolkit.asm;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukas on 26.04.14.
 */
public class IvNodeFinder
{
    public static AbstractInsnNode findNode(IvSingleNodeMatcher matcher, MethodNode methodNode)
    {
        return findNode(matcher, methodNode.instructions);
    }

    public static AbstractInsnNode findNode(IvSingleNodeMatcher matcher, InsnList insnList)
    {
        for (int i = 0; i < insnList.size(); i++)
        {
            AbstractInsnNode node = insnList.get(i);

            if (matcher.matchNode(node))
            {
                return node;
            }
        }

        return null;
    }

    public static AbstractInsnNode findNodeList(IvMultiNodeMatcher matcher, MethodNode methodNode)
    {
        return findNodeList(matcher, methodNode.instructions);
    }

    public static AbstractInsnNode findNodeList(IvMultiNodeMatcher matcher, InsnList insnList)
    {
        for (int i = 0; i < insnList.size(); i++)
        {
            AbstractInsnNode node = insnList.get(i);

            if (matcher.matchFromNodeInList(insnList, node))
            {
                return node;
            }
        }

        return null;
    }

    public static List<AbstractInsnNode> findNodes(IvSingleNodeMatcher matcher, MethodNode methodNode)
    {
        return findNodes(matcher, methodNode.instructions);
    }

    public static List<AbstractInsnNode> findNodes(IvSingleNodeMatcher matcher, InsnList insnList)
    {
        List<AbstractInsnNode> nodes = new ArrayList<AbstractInsnNode>();

        for (int i = 0; i < insnList.size(); i++)
        {
            AbstractInsnNode node = insnList.get(i);

            if (matcher.matchNode(node))
            {
                nodes.add(node);
            }
        }

        return nodes;
    }

    public static List<AbstractInsnNode> findNodeLists(IvMultiNodeMatcher matcher, MethodNode methodNode)
    {
        return findNodeLists(matcher, methodNode.instructions);
    }

    public static List<AbstractInsnNode> findNodeLists(IvMultiNodeMatcher matcher, InsnList insnList)
    {
        List<AbstractInsnNode> nodes = new ArrayList<AbstractInsnNode>();

        for (int i = 0; i < insnList.size(); i++)
        {
            AbstractInsnNode node = insnList.get(i);

            if (matcher.matchFromNodeInList(insnList, node))
            {
                nodes.add(node);
            }
        }

        return nodes;
    }
}
