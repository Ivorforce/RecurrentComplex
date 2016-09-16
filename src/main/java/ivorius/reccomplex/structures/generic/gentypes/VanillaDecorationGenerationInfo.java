/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.structures.generic.gentypes;

import com.google.gson.*;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.editstructure.gentypes.TableDataSourceVanillaDecorationGenerationInfo;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.Environment;
import ivorius.reccomplex.structures.YSelector;
import ivorius.reccomplex.structures.generic.GenericYSelector;
import ivorius.reccomplex.structures.generic.matchers.EnvironmentMatcher;
import ivorius.reccomplex.worldgen.decoration.RCBiomeDecorator;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;

/**
 * Created by lukas on 19.01.15.
 */
public class VanillaDecorationGenerationInfo extends StructureGenerationInfo
{
    public Double generationWeight;
    public RCBiomeDecorator.DecorationType type;

    public BlockPos spawnShift;

    public EnvironmentMatcher environmentMatcher;

    public VanillaDecorationGenerationInfo()
    {
        this(null, null, RCBiomeDecorator.DecorationType.TREE, BlockPos.ORIGIN, "");
    }

    public VanillaDecorationGenerationInfo(@Nullable String id, Double generationWeight, RCBiomeDecorator.DecorationType type, BlockPos spawnShift, String environmentExpression)
    {
        super(id != null ? id : randomID(VanillaDecorationGenerationInfo.class));
        this.type = type;
        this.generationWeight = generationWeight;
        this.spawnShift = spawnShift;
        environmentMatcher = new EnvironmentMatcher(environmentExpression);
    }

    @Nonnull
    @Override
    public String id()
    {
        return id;
    }

    @Override
    public void setID(@Nonnull String id)
    {
        this.id = id;
    }

    public void setSpawnShift(BlockPos spawnShift)
    {
        this.spawnShift = spawnShift;
    }

    @Override
    public String displayString()
    {
        return IvTranslations.get("reccomplex.generationInfo.decoration.title");
    }

    @Nullable
    @Override
    public YSelector ySelector()
    {
        return new GenericYSelector(GenericYSelector.SelectionMode.SURFACE, spawnShift.getY(), spawnShift.getY());
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceVanillaDecorationGenerationInfo(navigator, delegate, this);
    }

    public boolean generatesIn(Environment environment)
    {
        return environmentMatcher.test(environment);
    }

    public double getActiveWeight()
    {
        return generationWeight != null ? generationWeight : 1.0;
    }

    public static class Serializer implements JsonSerializer<VanillaDecorationGenerationInfo>, JsonDeserializer<VanillaDecorationGenerationInfo>
    {
        @Override
        public VanillaDecorationGenerationInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.asJsonObject(json, "vanillaStructureSpawnInfo");

            String id = JsonUtils.getString(jsonObject, "id", null);

            Double spawnWeight = jsonObject.has("generationWeight") ? JsonUtils.getDouble(jsonObject, "generationWeight") : null;
            RCBiomeDecorator.DecorationType type = context.deserialize(jsonObject.get("type"), RCBiomeDecorator.DecorationType.class);

            int spawnX = JsonUtils.getInt(jsonObject, "spawnShiftX", 0);
            int spawnY = JsonUtils.getInt(jsonObject, "spawnShiftY", 0);
            int spawnZ = JsonUtils.getInt(jsonObject, "spawnShiftZ", 0);

            String environmentExpression = JsonUtils.getString(jsonObject, "environmentExpression", "");

            return new VanillaDecorationGenerationInfo(id, spawnWeight, type, new BlockPos(spawnX, spawnY, spawnZ), environmentExpression);
        }

        @Override
        public JsonElement serialize(VanillaDecorationGenerationInfo src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", src.id);

            if (src.generationWeight != null)
                jsonObject.addProperty("generationWeight", src.generationWeight);
            jsonObject.add("type", context.serialize(src.type));

            jsonObject.addProperty("spawnShiftX", src.spawnShift.getX());
            jsonObject.addProperty("spawnShiftY", src.spawnShift.getY());
            jsonObject.addProperty("spawnShiftZ", src.spawnShift.getZ());

            jsonObject.addProperty("environmentExpression", src.environmentMatcher.getExpression());

            return jsonObject;
        }
    }
}
