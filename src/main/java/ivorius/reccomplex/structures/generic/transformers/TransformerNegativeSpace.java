/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.transformers;

import com.google.gson.*;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.blocks.RCBlocks;
import ivorius.reccomplex.gui.editstructure.transformers.TableDataSourceBTNegativeSpace;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.structures.generic.matchers.BlockMatcher;
import net.minecraft.block.Block;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class TransformerNegativeSpace implements Transformer
{
    public BlockMatcher sourceMatcher;

    public TransformerNegativeSpace()
    {
        this(BlockMatcher.of(RCBlocks.negativeSpace, 0));
    }

    public TransformerNegativeSpace(String sourceExpression)
    {
        this.sourceMatcher = new BlockMatcher(sourceExpression);
    }

    @Override
    public boolean skipGeneration(Block block, int metadata)
    {
        return sourceMatcher.apply(new BlockMatcher.BlockFragment(block, metadata));
    }

    @Override
    public void transform(Phase phase, StructureSpawnContext context, IvWorldData worldData, List<Transformer> transformerList)
    {

    }

    @Override
    public String getDisplayString()
    {
        return "Space: " + sourceMatcher.getDisplayString();
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceBTNegativeSpace(this);
    }

    @Override
    public boolean generatesInPhase(Phase phase)
    {
        return false;
    }

    public static class Serializer implements JsonDeserializer<TransformerNegativeSpace>, JsonSerializer<TransformerNegativeSpace>
    {
        private MCRegistry registry;

        public Serializer(MCRegistry registry)
        {
            this.registry = registry;
        }

        @Override
        public TransformerNegativeSpace deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(jsonElement, "transformerNegativeSpace");

            String expression = TransformerReplace.Serializer.readLegacyMatcher(jsonObject, "source", "sourceMetadata"); // Legacy
            if (expression == null)
                expression = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "sourceExpression", "");

            return new TransformerNegativeSpace(expression);
        }

        @Override
        public JsonElement serialize(TransformerNegativeSpace transformer, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("sourceExpression", transformer.sourceMatcher.getExpression());

            return jsonObject;
        }
    }
}
