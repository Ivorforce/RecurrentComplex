/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.utils.algebra.ExpressionCache;
import ivorius.reccomplex.utils.expression.EnvironmentMatcher;
import ivorius.reccomplex.world.gen.feature.structure.Environment;
import ivorius.reccomplex.world.gen.feature.structure.VariableDomain;

import java.lang.reflect.Type;
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

    public void fill(VariableDomain domain, Environment environment, Random random)
    {
        for (Variable variable : variables)
        {
            if (!domain.isSet(variable.id))
                domain.set(variable.id, random.nextFloat() < variable.chance
                        && variable.condition.test(environment));
        }
    }

    public float chance(VariableDomain domain)
    {
        float chance = 1f;
        for (Variable variable : variables)
            chance *= (domain.get(variable.id) ? variable.chance : 1f - variable.chance);
        return chance;
    }

    public static class Variable
    {
        public String id = "";

        public EnvironmentMatcher condition = ExpressionCache.of(new EnvironmentMatcher(), "");

        public float chance = 0.5f;

        public boolean affectsLogic = false;

        public static class Serializer implements JsonSerializer<Variable>, JsonDeserializer<Variable>
        {
            @Override
            public Variable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
            {
                JsonObject jsonObject = JsonUtils.asJsonObject(json, "Generic Variable");

                Variable variable = new Variable();

                variable.id = JsonUtils.getString(jsonObject, "id");
                variable.condition.setExpression(JsonUtils.getString(jsonObject, "condition", ""));
                variable.chance = JsonUtils.getFloat(jsonObject, "chance");
                variable.affectsLogic = JsonUtils.getBoolean(jsonObject, "affectsLogic");

                return null;
            }

            @Override
            public JsonElement serialize(Variable src, Type typeOfSrc, JsonSerializationContext context)
            {
                JsonObject jsonObject = new JsonObject();

                jsonObject.addProperty("id", src.id);
                jsonObject.addProperty("condition", src.condition.getExpression());
                jsonObject.addProperty("chance", src.chance);
                jsonObject.addProperty("affectsLogic", src.affectsLogic);

                return jsonObject;
            }
        }
    }
}
