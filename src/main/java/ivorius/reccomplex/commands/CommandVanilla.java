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
import ivorius.reccomplex.random.Person;
import ivorius.reccomplex.utils.RCBlockAreas;
import ivorius.reccomplex.utils.RCStrings;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.util.Random;

/**
 * Created by lukas on 28.06.17.
 */
public class CommandVanilla extends CommandSplit
{
    public CommandVanilla()
    {
        super(RCConfig.commandPrefix + "vanilla");

        add(new SimpleCommand("gen", expect -> expect.any((Object[]) Type.values())
                .then(IvE.surfacePos("x", "z"))
                .named("dimension", "d").then(MCE::dimension)
                .named("seed").then(RCE::randomString)
                .flag("select", "s")
        )
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
            {
                Parameters parameters = Parameters.of(args, expect()::declare);

                Type type = parameters.get(0).map(Type::valueOf, s -> new CommandException("No such structure type")).require();
                WorldServer world = parameters.get("dimension").to(MCP.dimension(server, sender)).require();
                BlockSurfacePos pos = parameters.get(IvP.surfacePos("x", "z", sender.getPosition(), false)).require();
                String seed = parameters.get("seed").optional().orElseGet(() -> Person.chaoticName(new Random(), new Random().nextBoolean()));

                MapGenStructure gen;

                switch (type)
                {
                    case VILLAGE:
                        gen = new MapGenVillage()
                        {
                            @Override
                            protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ)
                            {
                                return true;
                            }
                        };
                        break;
                    default:
                        throw new InternalError();
                }

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

                if (parameters.has("select")) RCCommands.select(sender, RCBlockAreas.from(structureStart.getBoundingBox()));
            }
        });
    }

    public enum Type
    {
        VILLAGE
    }
}
