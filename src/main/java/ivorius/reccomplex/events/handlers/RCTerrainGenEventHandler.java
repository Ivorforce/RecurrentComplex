/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.events.handlers;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.world.gen.feature.decoration.RCBiomeDecorator;
import ivorius.reccomplex.world.gen.feature.sapling.RCSaplingGenerator;
import ivorius.reccomplex.world.gen.feature.structure.MapGenStructureHook;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.structure.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * Created by lukas on 14.09.16.
 */
public class RCTerrainGenEventHandler
{
    private static boolean hasAmountData(DecorateBiomeEvent.Decorate event)
    {
        try
        {
            // Ensure
            event.getClass().getDeclaredMethod("getModifiedAmount");
            event.getClass().getDeclaredMethod("setModifiedAmount", int.class);
            return (boolean) event.getClass().getDeclaredMethod("hasAmountData").invoke(event);
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private static int getModifiedAmount(DecorateBiomeEvent.Decorate event)
    {
        try
        {
            return (int) event.getClass().getDeclaredMethod("getModifiedAmount").invoke(event);
        }
        catch (Exception ignored)
        {
            return -1;
        }
    }

    private static void setModifiedAmount(DecorateBiomeEvent.Decorate event, int amount)
    {
        try
        {
            event.getClass().getDeclaredMethod("setModifiedAmount", int.class).invoke(event, amount);
        }
        catch (Exception ignored)
        {
        }
    }

    public void register()
    {
        MinecraftForge.TERRAIN_GEN_BUS.register(this);
    }

    @SubscribeEvent
    public void onSaplingGrow(SaplingGrowTreeEvent event)
    {
        if (event.getWorld() instanceof WorldServer)
        {
            if (RCSaplingGenerator.maybeGrowSapling((WorldServer) event.getWorld(), event.getPos(), event.getRand()))
            {
                event.setResult(Event.Result.DENY);
            }
        }
    }

    @SubscribeEvent
    public void onDecoration(DecorateBiomeEvent.Decorate event)
    {
        if (event.getWorld() instanceof WorldServer)
        {
            RCBiomeDecorator.DecorationType type = RCBiomeDecorator.DecorationType.getDecorationType(event);

            if (type != null)
            {
                int amount;
                if (hasAmountData(event) && (amount = getModifiedAmount(event)) >= 0)
                    setModifiedAmount(event, RCBiomeDecorator.decorate((WorldServer) event.getWorld(), event.getRand(), event.getPos(), type, amount));
                else
                {
                    Event.Result result = RCBiomeDecorator.decorate((WorldServer) event.getWorld(), event.getRand(), event.getPos(), type);
                    if (result != null)
                        event.setResult(result);
                }
            }
        }
    }

    @SubscribeEvent
    public void onInitMapGen(InitMapGenEvent event)
    {
        if (RCConfig.decorationHacks)
        {
            InitMapGenEvent.EventType type = event.getType();

            // All need to inherit from the base type
            MapGenStructureHook hook;

            switch (type)
            {
                case OCEAN_MONUMENT:

                    hook = new MapGenStructureHook((MapGenStructure) event.getNewGen(), RCBiomeDecorator.DecorationType.OCEAN_MONUMENT);
                    event.setNewGen(new StructureOceanMonument()
                    {
                        @Override
                        public String getStructureName()
                        {
                            return hook.getStructureName();
                        }

                        @Override
                        public boolean generateStructure(World worldIn, Random randomIn, ChunkPos chunkCoord)
                        {
                            return hook.generateStructure(worldIn, randomIn, chunkCoord);
                        }

                        @Override
                        public boolean isInsideStructure(BlockPos pos)
                        {
                            return hook.isInsideStructure(pos);
                        }

                        @Override
                        public boolean isPositionInStructure(World worldIn, BlockPos pos)
                        {
                            return hook.isPositionInStructure(worldIn, pos);
                        }

                        @Override
                        @Nullable
                        public BlockPos getNearestStructurePos(World worldIn, BlockPos pos, boolean findUnexplored)
                        {
                            return hook.getNearestStructurePos(worldIn, pos, findUnexplored);
                        }

                        @Override
                        public void generate(World worldIn, int x, int z, ChunkPrimer primer)
                        {
                            hook.generate(worldIn, x, z, primer);
                        }
                    });
                    break;
                case SCATTERED_FEATURE:
                    hook = new MapGenStructureHook((MapGenStructure) event.getNewGen(), RCBiomeDecorator.DecorationType.SCATTERED_FEATURE);
                    event.setNewGen(new MapGenScatteredFeature()
                    {
                        @Override
                        public String getStructureName()
                        {
                            return hook.getStructureName();
                        }

                        @Override
                        public boolean generateStructure(World worldIn, Random randomIn, ChunkPos chunkCoord)
                        {
                            return hook.generateStructure(worldIn, randomIn, chunkCoord);
                        }

                        @Override
                        public boolean isInsideStructure(BlockPos pos)
                        {
                            return hook.isInsideStructure(pos);
                        }

                        @Override
                        public boolean isPositionInStructure(World worldIn, BlockPos pos)
                        {
                            return hook.isPositionInStructure(worldIn, pos);
                        }

                        @Override
                        @Nullable
                        public BlockPos getNearestStructurePos(World worldIn, BlockPos pos, boolean findUnexplored)
                        {
                            return hook.getNearestStructurePos(worldIn, pos, findUnexplored);
                        }

                        @Override
                        public void generate(World worldIn, int x, int z, ChunkPrimer primer)
                        {
                            hook.generate(worldIn, x, z, primer);
                        }
                    });
                    break;
                case VILLAGE:
                    hook = new MapGenStructureHook((MapGenStructure) event.getNewGen(), RCBiomeDecorator.DecorationType.VILLAGE);
                    event.setNewGen(new MapGenVillage()
                    {
                        @Override
                        public String getStructureName()
                        {
                            return hook.getStructureName();
                        }

                        @Override
                        public boolean generateStructure(World worldIn, Random randomIn, ChunkPos chunkCoord)
                        {
                            return hook.generateStructure(worldIn, randomIn, chunkCoord);
                        }

                        @Override
                        public boolean isInsideStructure(BlockPos pos)
                        {
                            return hook.isInsideStructure(pos);
                        }

                        @Override
                        public boolean isPositionInStructure(World worldIn, BlockPos pos)
                        {
                            return hook.isPositionInStructure(worldIn, pos);
                        }

                        @Override
                        @Nullable
                        public BlockPos getNearestStructurePos(World worldIn, BlockPos pos, boolean findUnexplored)
                        {
                            return hook.getNearestStructurePos(worldIn, pos, findUnexplored);
                        }

                        @Override
                        public void generate(World worldIn, int x, int z, ChunkPrimer primer)
                        {
                            hook.generate(worldIn, x, z, primer);
                        }
                    });
                    break;
                case NETHER_BRIDGE:
                    hook = new MapGenStructureHook((MapGenStructure) event.getNewGen(), RCBiomeDecorator.DecorationType.NETHER_BRIDGE);
                    event.setNewGen(new MapGenNetherBridge()
                    {
                        @Override
                        public String getStructureName()
                        {
                            return hook.getStructureName();
                        }

                        @Override
                        public boolean generateStructure(World worldIn, Random randomIn, ChunkPos chunkCoord)
                        {
                            return hook.generateStructure(worldIn, randomIn, chunkCoord);
                        }

                        @Override
                        public boolean isInsideStructure(BlockPos pos)
                        {
                            return hook.isInsideStructure(pos);
                        }

                        @Override
                        public boolean isPositionInStructure(World worldIn, BlockPos pos)
                        {
                            return hook.isPositionInStructure(worldIn, pos);
                        }

                        @Override
                        @Nullable
                        public BlockPos getNearestStructurePos(World worldIn, BlockPos pos, boolean findUnexplored)
                        {
                            return hook.getNearestStructurePos(worldIn, pos, findUnexplored);
                        }

                        @Override
                        public void generate(World worldIn, int x, int z, ChunkPrimer primer)
                        {
                            hook.generate(worldIn, x, z, primer);
                        }
                    });
                    break;
                case STRONGHOLD:
                    hook = new MapGenStructureHook((MapGenStructure) event.getNewGen(), RCBiomeDecorator.DecorationType.STRONGHOLD);
                    event.setNewGen(new MapGenStronghold()
                    {
                        @Override
                        public String getStructureName()
                        {
                            return hook.getStructureName();
                        }

                        @Override
                        public boolean generateStructure(World worldIn, Random randomIn, ChunkPos chunkCoord)
                        {
                            return hook.generateStructure(worldIn, randomIn, chunkCoord);
                        }

                        @Override
                        public boolean isInsideStructure(BlockPos pos)
                        {
                            return hook.isInsideStructure(pos);
                        }

                        @Override
                        public boolean isPositionInStructure(World worldIn, BlockPos pos)
                        {
                            return hook.isPositionInStructure(worldIn, pos);
                        }

                        @Override
                        @Nullable
                        public BlockPos getNearestStructurePos(World worldIn, BlockPos pos, boolean findUnexplored)
                        {
                            return hook.getNearestStructurePos(worldIn, pos, findUnexplored);
                        }

                        @Override
                        public void generate(World worldIn, int x, int z, ChunkPrimer primer)
                        {
                            hook.generate(worldIn, x, z, primer);
                        }
                    });
                    break;
                case MINESHAFT:
                    hook = new MapGenStructureHook((MapGenStructure) event.getNewGen(), RCBiomeDecorator.DecorationType.MINESHAFT);
                    event.setNewGen(new MapGenMineshaft()
                    {
                        @Override
                        public String getStructureName()
                        {
                            return hook.getStructureName();
                        }

                        @Override
                        public boolean generateStructure(World worldIn, Random randomIn, ChunkPos chunkCoord)
                        {
                            return hook.generateStructure(worldIn, randomIn, chunkCoord);
                        }

                        @Override
                        public boolean isInsideStructure(BlockPos pos)
                        {
                            return hook.isInsideStructure(pos);
                        }

                        @Override
                        public boolean isPositionInStructure(World worldIn, BlockPos pos)
                        {
                            return hook.isPositionInStructure(worldIn, pos);
                        }

                        @Override
                        @Nullable
                        public BlockPos getNearestStructurePos(World worldIn, BlockPos pos, boolean findUnexplored)
                        {
                            return hook.getNearestStructurePos(worldIn, pos, findUnexplored);
                        }

                        @Override
                        public void generate(World worldIn, int x, int z, ChunkPrimer primer)
                        {
                            hook.generate(worldIn, x, z, primer);
                        }
                    });
                    break;
            }
        }
    }
}
