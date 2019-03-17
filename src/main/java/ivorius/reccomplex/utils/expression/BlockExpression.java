/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils.expression;

import com.google.common.base.Splitter;
import com.google.common.primitives.Ints;
import ivorius.ivtoolkit.blocks.BlockStates;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.utils.IntegerRanges;
import ivorius.reccomplex.utils.algebra.BoolFunctionExpressionCache;
import ivorius.reccomplex.utils.algebra.RCBoolAlgebra;
import ivorius.reccomplex.utils.algebra.SupplierCache;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Created by lukas on 03.03.15.
 */
public class BlockExpression extends BoolFunctionExpressionCache<IBlockState, Object>
{
    public static final String BLOCK_ID_PREFIX = "id=";
    public static final String DOMAIN_PREFIX = "domain=";
    public static final String METADATA_PREFIX = "metadata=";
    public static final String PROPERTY_PREFIX = "property[";

    public final MCRegistry registry;

    public BlockExpression(MCRegistry registry)
    {
        super(RCBoolAlgebra.algebra(), true, TextFormatting.GREEN + "Any Block");

        this.registry = registry;

        addTypes(new IDVariableType(BLOCK_ID_PREFIX, "", registry), t -> t.alias("", ""));
        addTypes(new DomainVariableType(DOMAIN_PREFIX, "", registry));
        addTypes(new MetadataVariableType(METADATA_PREFIX, ""), t -> t.alias("#", ""));
        addTypes(new PropertyVariableType(PROPERTY_PREFIX, ""), t -> t.alias("$[", ""));
    }

    public static String of(MCRegistry registry, Block block)
    {
        return registry.idFromBlock(block).toString();
    }

    public static String of(MCRegistry registry, Block block, Integer metadata)
    {
        return String.format("%s & %s%d", registry.idFromBlock(block), METADATA_PREFIX, metadata);
    }

    public static String of(MCRegistry registry, Block block, IntegerRange range)
    {
        return String.format("%s & %s%d-%d", registry.idFromBlock(block), METADATA_PREFIX, range.min, range.max);
    }

    public class IDVariableType extends VariableType<Boolean, IBlockState, Object>
    {
        public MCRegistry registry;

        public IDVariableType(String prefix, String suffix, MCRegistry registry)
        {
            super(prefix, suffix);
            this.registry = registry;
        }

        @Override
        public Function<SupplierCache<IBlockState>, Boolean> parse(String var)
        {
            Block block = registry.blockFromID(new ResourceLocation(var));
            return s -> s.get().getBlock() == block;
        }

        @Override
        public Validity validity(String var, Object object)
        {
            ResourceLocation location = new ResourceLocation(var); // Since MC defaults to air now
            return registry.blockFromID(location) != Blocks.AIR || location.equals(Block.REGISTRY.getNameForObject(Blocks.AIR))
                    ? Validity.KNOWN : Validity.UNKNOWN;
        }
    }

    public class DomainVariableType extends VariableType<Boolean, IBlockState, Object>
    {
        public MCRegistry registry;

        public DomainVariableType(String prefix, String suffix, MCRegistry registry)
        {
            super(prefix, suffix);
            this.registry = registry;
        }

        @Override
        public Function<SupplierCache<IBlockState>, Boolean> parse(String var)
        {
            return s -> registry.idFromBlock(s.get().getBlock()).getResourceDomain().equals(var);
        }

        @Override
        public Validity validity(String var, Object object)
        {
            if (Loader.instance().getIndexedModList().keySet().contains(var)) {
                return Validity.KNOWN;
            }

            return Validity.UNKNOWN;
        }
    }

    public class MetadataVariableType extends VariableType<Boolean, IBlockState, Object>
    {
        public MetadataVariableType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        public IntegerRange parseMetadataExp(String var)
        {
            if (var.contains("-"))
            {
                List<String> split = Splitter.on('-').splitToList(var);

                if (split.size() != 2)
                    return null;

                Integer left = parseMetadata(split.get(0));
                Integer right = parseMetadata(split.get(1));

                return left != null && right != null ? IntegerRanges.from(left, right) : null;
            }

            Integer meta = parseMetadata(var);
            return meta != null ? new IntegerRange(meta, meta) : null;
        }

        public Integer parseMetadata(String var)
        {
            Integer integer = Ints.tryParse(var);
            return integer != null && integer >= 0 && integer < 16 ? integer : null;
        }

        @Override
        public Function<SupplierCache<IBlockState>, Boolean> parse(String var)
        {
            IntegerRange range = parseMetadataExp(var);

            return s -> {
                int metadata = BlockStates.toMetadata(s.get());
                return range != null && metadata >= range.min && metadata <= range.max;
            };
        }

        @Override
        public Validity validity(String var, Object object)
        {
            return parseMetadataExp(var) != null ? Validity.KNOWN : Validity.ERROR;
        }
    }

    public class PropertyVariableType extends VariableType<Boolean, IBlockState, Object>
    {
        public PropertyVariableType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        public Pair<String, String> parsePropery(String var)
        {
            int close = var.indexOf("]=");
            return close >= 0 ? Pair.of(var.substring(0, close), var.substring(close + 2)) : null;
        }

        @Override
        public Function<SupplierCache<IBlockState>, Boolean> parse(String var)
        {
            Pair<String, String> pair = parsePropery(var);

            return state -> pair != null && getProperty(state.get().getBlock(), pair.getLeft())
                    .filter(property -> property.parseValue(pair.getRight()).orNull() == state.get().getValue(property)).isPresent();
        }

        @Override
        public Validity validity(String var, Object object)
        {
            Pair<String, String> pair = parsePropery(var);

            if (pair != null)
            {
                return Block.REGISTRY.getKeys().stream().map(Block.REGISTRY::getObject)
                        .anyMatch(block -> hasPropertyEntry(pair, block))
                        ? Validity.KNOWN : Validity.UNKNOWN;
            }
            else
                return Validity.ERROR;
        }

        protected boolean hasPropertyEntry(Pair<String, String> pair, Block block)
        {
            return getProperty(block, pair.getLeft()).filter(property -> property.parseValue(pair.getRight()).isPresent()).isPresent();
        }

        private Optional<IProperty<?>> getProperty(Block block, String key)
        {
            return block.getDefaultState().getProperties().keySet().stream().filter(
                    property -> property.getName().equals(key)).findFirst();
        }

        @Override
        protected String getVarRepresentation(String var, Object o)
        {
            Pair<String, String> pair = parsePropery(var);
            return pair != null ? getRepresentation(keyValidity(pair.getLeft(), o))
                    + pair.getLeft() + TextFormatting.BLUE + "]="
                    + getRepresentation(validity(var, o)) + pair.getRight()
                    : super.getVarRepresentation(var, o);
        }

        protected Validity keyValidity(String var, Object o)
        {
            return Block.REGISTRY.getKeys().stream().map(Block.REGISTRY::getObject)
                    .anyMatch(block -> getProperty(block, var).isPresent())
                    ? Validity.KNOWN : Validity.UNKNOWN;
        }
    }
}
