/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import ivorius.ivtoolkit.blocks.BlockSurfacePos;
import ivorius.ivtoolkit.world.chunk.gen.StructureBoundingBoxes;
import ivorius.mcopts.commands.CommandSplit;
import ivorius.mcopts.commands.SimpleCommand;
import ivorius.mcopts.commands.parameters.MCP;
import ivorius.mcopts.commands.parameters.Parameters;
import ivorius.mcopts.commands.parameters.expect.MCE;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.commands.parameters.IvP;
import ivorius.reccomplex.commands.parameters.expect.IvE;
import ivorius.reccomplex.commands.parameters.expect.RCE;
import ivorius.reccomplex.commands.structure.sight.CommandSightCheck;
import ivorius.reccomplex.random.Person;
import ivorius.reccomplex.utils.RCBlockAreas;
import ivorius.reccomplex.utils.RCStrings;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.structure.*;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by lukas on 28.06.17.
 */
public class CommandVanilla extends CommandSplit
{
    public static Method getStructureAt;

    public CommandVanilla()
    {
        super(RCConfig.commandPrefix + "vanilla");

        add(new SimpleCommand("gen", expect -> expect.any((Object[]) Type.values())
                .then(IvE.surfacePos("x", "z"))
                .named("dimension", "d").then(MCE::dimension)
                .named("seed").then(RCE::randomString)
                .flag("select", "s")
                .flag("suggest", "t")
        )
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
            {
                Parameters parameters = Parameters.of(args, expect()::declare);

                Type type = parameters.get(0).map(Type::fromName, s -> new CommandException("No such structure type")).require();
                WorldServer world = parameters.get("dimension").to(MCP.dimension(server, sender)).require();
                BlockSurfacePos pos = parameters.get(IvP.surfacePos("x", "z", sender.getPosition(), false)).require();
                String seed = parameters.get("seed").optional().orElseGet(() -> Person.chaoticName(new Random(), new Random().nextBoolean()));
                boolean suggest = parameters.has("suggest");

                MapGenStructure gen = type.generator(suggest);

                // Don't recursive generate
                ReflectionHelper.setPrivateValue(MapGenBase.class, gen, 0, "range", "field_75040_a");

                ChunkPos chunkPos = pos.chunkCoord();
                Random random = new Random(RCStrings.seed(seed));

                // ChunkPrimer mostly doesn't get used
                gen.generate(world, chunkPos.chunkXPos, chunkPos.chunkZPos, new ChunkPrimer());

                Long2ObjectMap<StructureStart> structureMap = ReflectionHelper.getPrivateValue(MapGenStructure.class, gen, "structureMap", "field_75053_d");

                StructureStart structureStart = structureMap.get(ChunkPos.chunkXZ2Int(chunkPos.chunkXPos, chunkPos.chunkZPos));

                if (structureStart == null)
                    throw new CommandException("Failed to place structure!");

                // 'retro'-generate all chunks
                for (ChunkPos retroPos : StructureBoundingBoxes.rasterize(structureStart.getBoundingBox()))
                    gen.generateStructure(world, random, retroPos);

                sender.addChatMessage(new TextComponentTranslation("Structure generated at %s with seed %s", RCTextStyle.chunkPos(chunkPos), RCTextStyle.copy(seed)));

                if (parameters.has("select"))
                    RCCommands.select(sender, RCBlockAreas.from(structureStart.getBoundingBox()));
            }
        });

        add(new SimpleCommand("check", expect -> expect.then(MCE::xyz))
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
            {
                Parameters parameters = Parameters.of(args, expect()::declare);

                World world = sender.getEntityWorld();
                BlockPos pos = parameters.get(MCP.pos("x", "y", "z", sender.getPosition(), false)).require();

                sender.addChatMessage(CommandSightCheck.list(sightNames(world, pos)));
            }
        });
    }

    public static List<ITextComponent> sightNames(World world, BlockPos pos)
    {
        return sights(world, pos).map(RCTextStyle::vanillaSight).collect(Collectors.toList());
    }

    public static Stream<MapGenStructure> sights(World world, BlockPos pos)
    {
        return Arrays.stream(Type.values())
                            .map(t -> t.generator(false))
                            .peek(m -> ReflectionHelper.setPrivateValue(MapGenBase.class, m, world, "worldObj", "field_75039_c"))
                            .filter(m -> m.isInsideStructure(pos));
    }

    public enum Type
    {
        VILLAGE,
        MINESHAFT,
        STRONGHOLD,
        TEMPLE,
        OCEAN_MONUMENT,
        NETHER_STRONGHOLD;

        public static Type fromName(String name)
        {
            return Arrays.stream(Type.values())
                    .filter(t -> t.structureName().equals(name))
                    .findFirst().orElse(null);
        }

        public String structureName()
        {
            return generator(false).getStructureName();
        }

        public MapGenStructure generator(boolean suggest)
        {
            switch (this)
            {
                case VILLAGE:
                    return new MapGenVillage()
                    {
                        @Override
                        protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ)
                        {
                            return !suggest || super.canSpawnStructureAtCoords(chunkX, chunkZ);
                        }
                    };
                case MINESHAFT:
                    return new MapGenMineshaft()
                    {
                        @Override
                        protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ)
                        {
                            return !suggest || super.canSpawnStructureAtCoords(chunkX, chunkZ);
                        }
                    };
                case STRONGHOLD:
                    return new MapGenStronghold()
                    {
                        @Override
                        protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ)
                        {
                            return !suggest || super.canSpawnStructureAtCoords(chunkX, chunkZ);
                        }
                    };
                case TEMPLE:
                    return new MapGenScatteredFeature()
                    {
                        @Override
                        protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ)
                        {
                            return !suggest || super.canSpawnStructureAtCoords(chunkX, chunkZ);
                        }
                    };
                case OCEAN_MONUMENT:
                    return new StructureOceanMonument()
                    {
                        @Override
                        protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ)
                        {
                            return !suggest || super.canSpawnStructureAtCoords(chunkX, chunkZ);
                        }
                    };
                case NETHER_STRONGHOLD:
                    return new MapGenNetherBridge()
                    {
                        @Override
                        protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ)
                        {
                            return !suggest || super.canSpawnStructureAtCoords(chunkX, chunkZ);
                        }
                    };
                default:
                    throw new InternalError();
            }
        }

        @Override
        public String toString()
        {
            return structureName();
        }
    }
}
