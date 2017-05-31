/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import ivorius.reccomplex.files.loading.ResourceDirectory;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.GenerationType;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.NaturalGeneration;
import net.minecraft.command.CommandException;

/**
 * Created by lukas on 31.05.17.
 */
public class RCParameter extends Parameter
{
    public RCParameter(Parameter other)
    {
        super(other);
    }

    public static int[] parseMetadatas(String arg) throws CommandException
    {
        try
        {
            String[] strings = arg.split(",");
            int[] ints = new int[strings.length];

            for (int i = 0; i < strings.length; i++)
            {
                ints[i] = Integer.valueOf(strings[i]);
            }

            return ints;
        }
        catch (Exception ex)
        {
            throw ServerTranslations.wrongUsageException("commands.selectModify.invalidMetadata", arg);
        }
    }

    @Override
    public RCParameter move(int idx)
    {
        return new RCParameter(super.move(idx));
    }

    public Result<Structure<?>> structure()
    {
        return at(0).map(StructureRegistry.INSTANCE::get,
                t -> ServerTranslations.commandException("commands.strucGen.noStructure", at(0)));
    }

    public Result<GenerationType> generationType(Structure<?> structure)
    {
        return at(0).failable().map(structure::generationType, t -> ServerTranslations.commandException("No Generation by this ID"))
                .orElse(() -> structure.<GenerationType>generationTypes(NaturalGeneration.class).stream().findFirst()
                        .orElse(structure.generationTypes(GenerationType.class).stream().findFirst().orElse(null)));
    }

    public Result<ResourceDirectory> resourceDirectory()
    {
        return at(0).map(t ->
        {
            try
            {
                return ResourceDirectory.valueOf(t);
            }
            catch (IllegalArgumentException e)
            {
                throw ServerTranslations.commandException("commands.rcsave.nodirectory");
            }
        });
    }

    public Result<int[]> metadatas()
    {
        return at(0).map(RCParameter::parseMetadatas);
    }
}
