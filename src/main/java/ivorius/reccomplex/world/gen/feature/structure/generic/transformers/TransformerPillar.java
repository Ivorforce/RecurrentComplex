/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.transformers;

import com.google.gson.*;
import ivorius.ivtoolkit.blocks.BlockStates;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.utils.algebra.ExpressionCache;
import ivorius.reccomplex.world.gen.feature.structure.Environment;
import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.editstructure.transformers.TableDataSourceBTPillar;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureLoadContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructurePrepareContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;
import ivorius.reccomplex.utils.expression.BlockExpression;
import net.minecraft.block.state.IBlockState;
import ivorius.reccomplex.nbt.NBTNone;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

/**
 * Created by lukas on 25.05.14.
 */
public class TransformerPillar extends TransformerSingleBlock<NBTNone>
{
    public BlockExpression sourceMatcher;

    public IBlockState destState;

    public TransformerPillar()
    {
        this(null, BlockExpression.of(RecurrentComplex.specialRegistry, Blocks.STONE, 0), Blocks.STONE.getDefaultState());
    }

    public TransformerPillar(@Nullable String id, String sourceExpression, IBlockState destState)
    {
        super(id != null ? id : randomID(TransformerPillar.class));
        this.sourceMatcher = ExpressionCache.of(new BlockExpression(RecurrentComplex.specialRegistry), sourceExpression);
        this.destState = destState;
    }

    @Override
    public boolean matches(Environment environment, NBTNone instanceData, BlockPos sourcePos, IBlockState state)
    {
        return sourceMatcher.test(state);
    }

    @Override
    public void transformBlock(NBTNone instanceData, Phase phase, StructureSpawnContext context, RunTransformer transformer, int[] areaSize, BlockPos coord, IBlockState sourceState)
    {
        if (RecurrentComplex.specialRegistry.isSafe(destState.getBlock()))
        {
            // TODO Fix for partial generation
            World world = context.environment.world;

            int y = coord.getY();

            do
            {
                BlockPos pos = new BlockPos(coord.getX(), y--, coord.getZ());
                context.setBlock(pos, destState, 2);

                IBlockState blockState = world.getBlockState(pos);
                if (!(blockState.getBlock().isReplaceable(world, pos) || blockState.getMaterial() == Material.LEAVES || blockState.getBlock().isFoliage(world, pos)))
                    break;
            }
            while (y > 0);
        }
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

    @SideOnly(Side.CLIENT)
    @Override
    public String getDisplayString()
    {
        return "Pillar: " + sourceMatcher.getDisplayString(null) + "->" + destState.getBlock().getLocalizedName();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceBTPillar(this, navigator, delegate);
    }

    @Override
    public boolean generatesInPhase(NBTNone instanceData, Phase phase)
    {
        return phase == Phase.BEFORE;
    }

    public static class Serializer implements JsonDeserializer<TransformerPillar>, JsonSerializer<TransformerPillar>
    {
        private MCRegistry registry;

        public Serializer(MCRegistry registry)
        {
            this.registry = registry;
        }

        @Override
        public TransformerPillar deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonObject = JsonUtils.asJsonObject(jsonElement, "transformerPillar");

            String id = readID(jsonObject);

            String expression = TransformerReplace.Serializer.readLegacyMatcher(jsonObject, "source", "sourceMetadata"); // Legacy
            if (expression == null)
                expression = JsonUtils.getString(jsonObject, "sourceExpression", "");

            String destBlock = JsonUtils.getString(jsonObject, "dest");
            Block dest = registry.blockFromID(new ResourceLocation(destBlock));
            IBlockState destState = dest != null ? BlockStates.fromMetadata(dest, JsonUtils.getInt(jsonObject, "destMetadata")) : null;

            return new TransformerPillar(id, expression, destState);
        }

        @Override
        public JsonElement serialize(TransformerPillar transformer, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", transformer.id());
            jsonObject.addProperty("sourceExpression", transformer.sourceMatcher.getExpression());

            jsonObject.addProperty("dest", registry.idFromBlock(transformer.destState.getBlock()).toString());
            jsonObject.addProperty("destMetadata", ivorius.ivtoolkit.blocks.BlockStates.toMetadata(transformer.destState));

            return jsonObject;
        }
    }
}
