/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.gentypes;

import com.google.gson.*;
import ivorius.reccomplex.gui.editstructure.gentypes.TableDataSourceStaticGenerationInfo;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.generic.BiomeGenerationInfo;
import ivorius.reccomplex.structures.generic.DimensionGenerationInfo;
import ivorius.reccomplex.structures.generic.DimensionSelector;
import ivorius.reccomplex.structures.generic.GenerationYSelector;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.StatCollector;

import java.lang.reflect.Type;

/**
 * Created by lukas on 21.02.15.
 */
public class StaticGenerationInfo extends StructureGenerationInfo
{
    public GenerationYSelector ySelector;
    public DimensionSelector dimensionSelector;

    public boolean relativeToSpawn;
    public int positionX;
    public int positionZ;

    public StaticGenerationInfo()
    {
        this(new GenerationYSelector(GenerationYSelector.SelectionMode.SURFACE, 0, 0), new DimensionSelector("0"), true, 0, 0);
    }

    public StaticGenerationInfo(GenerationYSelector ySelector, DimensionSelector dimensionSelector, boolean relativeToSpawn, int positionX, int positionZ)
    {
        this.ySelector = ySelector;
        this.dimensionSelector = dimensionSelector;
        this.relativeToSpawn = relativeToSpawn;
        this.positionX = positionX;
        this.positionZ = positionZ;
    }

    @Override
    public String displayString()
    {
        if (relativeToSpawn)
            return StatCollector.translateToLocalFormatted("reccomplex.generationInfo.static.spawn", String.valueOf(positionX), String.valueOf(positionZ));
        else
            return StatCollector.translateToLocalFormatted("reccomplex.generationInfo.static.nospawn", String.valueOf(positionX), String.valueOf(positionZ));
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceStaticGenerationInfo(navigator, delegate, this);
    }

    public int getPositionX(ChunkCoordinates spawnPos)
    {
        return relativeToSpawn ? spawnPos.posX + positionX : positionX;
    }

    public int getPositionZ(ChunkCoordinates spawnPos)
    {
        return relativeToSpawn ? spawnPos.posZ + positionZ : positionZ;
    }

    public static class Serializer implements JsonSerializer<StaticGenerationInfo>, JsonDeserializer<StaticGenerationInfo>
    {
        @Override
        public StaticGenerationInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(json, "vanillaStructureSpawnInfo");

            GenerationYSelector ySelector = context.deserialize(jsonObject.get("generationY"), GenerationYSelector.class);
            String dimension = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "dimension", "");

            boolean relativeToSpawn = JsonUtils.getJsonObjectBooleanFieldValueOrDefault(jsonObject, "relativeToSpawn", false);
            int positionX = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "positionX", 0);
            int positionZ = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "positionZ", 0);

            return new StaticGenerationInfo(ySelector, new DimensionSelector(dimension), relativeToSpawn, positionX, positionZ);
        }

        @Override
        public JsonElement serialize(StaticGenerationInfo src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.add("generationY", context.serialize(src.ySelector));
            jsonObject.addProperty("dimension", src.dimensionSelector.getDimensionID());

            jsonObject.addProperty("relativeToSpawn", src.relativeToSpawn);
            jsonObject.addProperty("positionX", src.positionX);
            jsonObject.addProperty("positionZ", src.positionZ);

            return jsonObject;
        }
    }
}
