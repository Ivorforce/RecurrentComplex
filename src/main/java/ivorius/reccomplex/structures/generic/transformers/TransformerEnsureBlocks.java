/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.structures.generic.transformers;

import com.google.gson.*;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.blocks.RCBlocks;
import ivorius.reccomplex.gui.editstructure.transformers.TableDataSourceBTEnsureSpace;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.Environment;
import ivorius.reccomplex.structures.StructureLoadContext;
import ivorius.reccomplex.structures.StructurePrepareContext;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.structures.generic.matchers.BlockMatcher;
import ivorius.reccomplex.structures.generic.matchers.PositionedBlockMatcher;
import ivorius.reccomplex.utils.NBTNone;
import ivorius.reccomplex.utils.RCAxisAlignedTransform;
import ivorius.reccomplex.utils.RCBlockAreas;
import ivorius.reccomplex.utils.RCMutableBlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

/**
 * Created by lukas on 25.05.14.
 */
public class TransformerEnsureBlocks extends Transformer<NBTNone>
{
    public BlockMatcher sourceMatcher;
    public PositionedBlockMatcher destMatcher;

    public TransformerEnsureBlocks()
    {
        this(null, "!(id=reccomplex:generic_space | id=reccomplex:generic_solid)", "is:air | is:leaves | is:replaceable");
    }

    public TransformerEnsureBlocks(@Nullable String id, String sourceExpression, String destExpression)
    {
        super(id != null ? id : randomID(TransformerEnsureBlocks.class));
        this.sourceMatcher = new BlockMatcher(RecurrentComplex.specialRegistry, sourceExpression);
        this.destMatcher = new PositionedBlockMatcher(RecurrentComplex.specialRegistry, destExpression);
    }

    @Override
    public boolean mayGenerate(NBTNone instanceData, StructureSpawnContext context, IvWorldData worldData, TransformerMulti transformer, TransformerMulti.InstanceData transformerData)
    {
        IvBlockCollection blockCollection = worldData.blockCollection;
        int[] areaSize = new int[]{blockCollection.width, blockCollection.height, blockCollection.length};
        BlockPos lowerCoord = context.lowerCoord();

        BlockPos.MutableBlockPos worldCoord = new BlockPos.MutableBlockPos();
        for (BlockPos sourcePos : RCBlockAreas.mutablePositions(blockCollection.area()))
        {
            RCMutableBlockPos.add(RCAxisAlignedTransform.apply(sourcePos, worldCoord, areaSize, context.transform), lowerCoord);
            IBlockState state = blockCollection.getBlockState(sourcePos);

            if (sourceMatcher.test(state) && !(destMatcher.expressionIsEmpty() || destMatcher.test(PositionedBlockMatcher.Argument.at(context.environment.world, worldCoord))))
                return false;
        }

        return true;
    }

    @Override
    public boolean skipGeneration(NBTNone instanceData, Environment environment, BlockPos pos, IBlockState state)
    {
        return false;
    }

    @Override
    public void transform(NBTNone instanceData, Phase phase, StructureSpawnContext context, IvWorldData worldData, TransformerMulti transformer, TransformerMulti.InstanceData transformerData)
    {

    }

    @Override
    public String getDisplayString()
    {
        return String.format("Ensure: %s", destMatcher.getDisplayString(null));
    }

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

            String id = JsonUtils.getString(jsonObject, "id", null);

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
