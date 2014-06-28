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

import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;

import static org.objectweb.asm.Opcodes.*;

/**
 * Created by lukas on 26.04.14.
 */
public class IvInsnHelper
{
    public static void insertDUP3(InsnList list)
    {
        list.add(new InsnNode(DUP2_X1));    // 1 2 3 1 2
        list.add(new InsnNode(POP2));       // 3 1 2
        list.add(new InsnNode(DUP_X2));     // 3 1 2 3
        list.add(new InsnNode(DUP_X2));     // 3 1 2 3 3
        list.add(new InsnNode(POP));        // 1 2 3 3
        list.add(new InsnNode(DUP2_X1));    // 1 2 3 1 2 3
    }

    public static void insertDUP4(InsnList list)
    {
        list.add(new InsnNode(DUP2_X2));    // 1 2 3 4 1 2
        list.add(new InsnNode(POP2));       // 3 4 1 2
        list.add(new InsnNode(DUP2_X2));    // 3 4 1 2 3 4
        list.add(new InsnNode(DUP2_X2));    // 3 4 1 2 3 4 3 4
        list.add(new InsnNode(POP2));       // 1 2 3 4 3 4
        list.add(new InsnNode(DUP2_X2));    // 1 2 3 4 1 2 3 4
    }
}
