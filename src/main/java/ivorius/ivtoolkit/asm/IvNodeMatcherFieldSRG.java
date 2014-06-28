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

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;

/**
 * Created by lukas on 26.04.14.
 */
public class IvNodeMatcherFieldSRG implements IvSingleNodeMatcher
{
    public int opCode;
    public String srgFieldName;
    public String owner;
    public Type type;

    public IvNodeMatcherFieldSRG(int opCode, String srgFieldName, String owner, Type type)
    {
        this.opCode = opCode;
        this.srgFieldName = srgFieldName;
        this.owner = owner;
        this.type = type;
    }

    @Override
    public boolean matchNode(AbstractInsnNode node)
    {
        if (node.getOpcode() != opCode)
        {
            return false;
        }

        FieldInsnNode fieldInsnNode = (FieldInsnNode) node;

        if (srgFieldName != null && !srgFieldName.equals(IvClassTransformer.getSrgName(fieldInsnNode)))
        {
            return false;
        }

        if (owner != null && !owner.equals(IvClassTransformer.getSrgClassName(fieldInsnNode.owner)))
        {
            return false;
        }

        if (type != null && !type.equals(Type.getType(fieldInsnNode.desc)))
        {
            return false;
        }

        return true;
    }
}
