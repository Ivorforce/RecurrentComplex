/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.structure;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.reccomplex.utils.expression.BlockExpression;
import ivorius.reccomplex.world.gen.feature.selector.StructureSelector;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import ivorius.reccomplex.world.gen.feature.structure.generic.Metadata;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.*;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by lukas on 05.07.17.
 */
public class StructureSearch
{
    @Nonnull
    public static Collection<String> keywords(String id, Structure<?> structure)
    {
        List<String> keywords = new ArrayList<>();

        keywords.add(id);

        structure.generationTypes(GenerationType.class).forEach(info -> keywords(keywords, info));

        if (structure instanceof GenericStructure)
            keywords(keywords, ((GenericStructure) structure).metadata);

        return keywords;
    }

    protected static void keywords(Collection<String> keywords, GenerationType info)
    {
        keywords.add(info.id());
        keywords.add(info.displayString());
        keywords.add(StructureRegistry.GENERATION_TYPES.iDForType(info.getClass()));
    }

    @Nonnull
    public static void keywords(Collection<String> collection, Metadata metadata)
    {
        collection.add(metadata.authors);
        collection.add(metadata.comment);
        collection.add(metadata.weblink);
    }

    public static double searchRank(List<String> query, Collection<String> keywords)
    {
        return keywords.stream().filter(Predicates.contains(Pattern.compile(String.join("|", Lists.transform(query, Pattern::quote)), Pattern.CASE_INSENSITIVE))::apply).count();
    }

    public static double biome(Structure<?> structure, Biome biome)
    {
        double result = 0;

        result += structure.generationTypes(NaturalGeneration.class).stream()
                .mapToDouble(g -> StructureSelector.generationWeightInBiome(g.biomeWeights, biome))
                .sum();

        result += structure.generationTypes(VanillaDecorationGeneration.class).stream()
                .mapToDouble(g -> StructureSelector.generationWeightInBiome(g.biomeWeights, biome))
                .sum();

        return result;
    }

    public static double dimension(Structure<?> structure, WorldServer world)
    {
        double result = 0;

        result += structure.generationTypes(NaturalGeneration.class).stream()
                .mapToDouble(g -> StructureSelector.generationWeightInDimension(g.dimensionWeights, world.provider))
                .sum();

        result += structure.generationTypes(VanillaDecorationGeneration.class).stream()
                .mapToDouble(g -> StructureSelector.generationWeightInDimension(g.dimensionWeights, world.provider))
                .sum();

        return result;
    }

    public static double list(Structure<?> structure, String listID)
    {
        return structure.generationTypes(ListGeneration.class).stream()
                .mapToDouble(g -> g.listID.equals(listID) ? 1 : 0)
                .sum();
    }

    public static double maze(Structure<?> structure, String mazeID)
    {
        return structure.generationTypes(MazeGeneration.class).stream()
                .mapToDouble(g -> g.mazeID.equals(mazeID) ? 1 : 0)
                .sum();
    }

    public static double author(Structure<?> structure, String author)
    {
        return structure instanceof GenericStructure
                ? searchRank(Collections.singletonList(author), Collections.singleton(((GenericStructure) structure).metadata.authors))
                : 0;
    }

    public static long containedBlocks(Structure structure, BlockExpression matcher)
    {
        if (structure == null)
            return 0;

        IvBlockCollection collection = structure.blockCollection();

        if (collection == null)
            return 0;

        return collection.area().stream()
                .anyMatch(p -> matcher.evaluate(collection.getBlockState(p))) ? 1 : 0;
    }
}
