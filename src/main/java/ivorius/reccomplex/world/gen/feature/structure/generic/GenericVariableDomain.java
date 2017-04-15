/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic;

import com.google.gson.annotations.SerializedName;
import ivorius.reccomplex.world.gen.feature.structure.VariableDomain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 15.04.17.
 */
public class GenericVariableDomain
{
    @SerializedName("variables")
    public final List<Variable> variables = new ArrayList<>();

    public void fill(VariableDomain domain, Random random)
    {
        for (Variable variable : variables)
        {
            if (!domain.isSet(variable.id))
                domain.set(variable.id, random.nextFloat() < variable.chance);
        }
    }

    public static class Variable
    {
        @SerializedName("name")
        public String id = "";

        @SerializedName("chance")
        public float chance = 0.5f;

        @SerializedName("affectsLogic")
        public boolean affectsLogic = false;
    }
}
