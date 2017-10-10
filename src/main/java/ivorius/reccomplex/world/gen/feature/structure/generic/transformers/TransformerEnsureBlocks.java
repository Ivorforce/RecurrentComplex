/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.transformers;

import com.google.gson.*;
import ivorius.ivtoolkit.blocks.BlockAreas;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.ivtoolkit.blocks.IvMutableBlockPos;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.ivtoolkit.world.chunk.gen.StructureBoundingBoxes;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.editstructure.transformers.TableDataSourceBTEnsureSpace;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.nbt.NBTNone;
import ivorius.reccomplex.utils.algebra.ExpressionCache;
import ivorius.reccomplex.world.gen.feature.structure.context.*;
import ivorius.reccomplex.utils.expression.BlockExpression;
import ivorius.reccomplex.utils.expression.PositionedBlockExpression;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

/**
 * Created by lukas on 25.05.14.
 */
public class TransformerEnsureBlocks extends Transformer<NBTNone>
{
    public BlockExpression sourceMatcher;
    public PositionedBlockExpression destMatcher;

    public TransformerEnsureBlocks()
    {
        this(null, "!(id=reccomplex:generic_space | id=reccomplex:generic_solid)", "is:air | is:leaves | is:replaceable");
    }

    public TransformerEnsureBlocks(@Nullable String id, String sourceExpression, String destExpression)
    {
        super(id != null ? id : randomID(TransformerEnsureBlocks.class));
        this.sourceMatcher = ExpressionCache.of(new BlockExpression(RecurrentComplex.specialRegistry), sourceExpression);
        this.destMatcher = ExpressionCache.of(new PositionedBlockExpression(RecurrentComplex.specialRegistry), destExpression);
    }

    @Override
    public boolean mayGenerate(NBTNone instanceData, StructurePrepareContext context, IvWorldData worldData)
    {
        IvBlockCollection blockCollection = worldData.blockCollection;
        int[] areaSize = new int[]{blockCollection.width, blockCollection.height, blockCollection.length};
        BlockPos lowerCoord = StructureBoundingBoxes.min(context.boundingBox);

        BlockPos.MutableBlockPos worldCoord = new BlockPos.MutableBlockPos();
        for (BlockPos sourcePos : BlockAreas.mutablePositions(blockCollection.area()))
        {
            if (sourceMatcher.test(blockCollection.getBlockState(sourcePos)))
            {
                IvMutableBlockPos.add(context.transform.applyOn(sourcePos, worldCoord, areaSize), lowerCoord);
                if (!(destMatcher.expressionIsEmpty() || destMatcher.evaluate(() -> PositionedBlockExpression.Argument.at(context.environment.world, worldCoord))))
                    return false;
            }
        }

        return true;
    }

    @Override
    public boolean skipGeneration(NBTNone instanceData, StructureLiveContext context, BlockPos pos, IBlockState state, IvWorldData worldData, BlockPos sourcePos)
    {
        return false;
    }

    @Override
    public void transform(NBTNone instanceData, Phase phase, StructureSpawnContext context, IvWorldData worldData, RunTransformer transformer)
    {

    }

    @SideOnly(Side.CLIENT)
    @Override
    public String getDisplayString()
    {
        return String.format("Ensure: %s", destMatcher.getDisplayString(null));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceBTEnsureSpace(this, navigator, delegate);
    }

    @Override
    public NBTNone prepareInstanceData(StructurePrepareContext context, IvWorldData worldData)
    {
        return new NBTNone();
    }

    @Override
    public NBTNone loadInstanceData(StructureLoadContext context, NBTBase nbt)
    {
        return new NBTNone();
    }

    public static class Serializer implements JsonDeserializer<TransformerEnsureBlocks>, JsonSerializer<TransformerEnsureBlocks>
    {
        private MCRegistry registry;

        public Serializer(MCRegistry registry)
        {
            this.registry = registry;
        }

        @Override
        public TransformerEnsureBlocks deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonObject = JsonUtils.asJsonObject(jsonElement, "transformerEnsureSpace");

            String id = readID(jsonObject);

            String expression = JsonUtils.getString(jsonObject, "sourceExpression", "");
            String destExpression = JsonUtils.getString(jsonObject, "destExpression", "");

            return new TransformerEnsureBlocks(id, expression, destExpression);
        }

        @Override
        public JsonElement serialize(TransformerEnsureBlocks transformer, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", transformer.id());

            jsonObject.addProperty("sourceExpression", transformer.sourceMatcher.getExpression());
            jsonObject.addProperty("destExpression", transformer.destMatcher.getExpression());

            return jsonObject;
        }
    }
}
