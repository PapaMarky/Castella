package com.markyshouse.mc.Castella;

/**
 * Created by mark on 9/18/2015.
 */
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.markyshouse.mc.IBlockChooser;
import com.markyshouse.mc.TerrainMap;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

import java.util.*;

class TreeEater {
    Map<String,BlockPos> logMap;
    int[] max;
    int[] min = new int[3]; // x, y, z

    private String makeKey(BlockPos pos) {
        return pos.toString().substring(8);
    }

    private BlockPlanks.EnumType getWoodType(BlockPos blockPos, Block block, World world, IChunkProvider chunkProvider) {
        if (block instanceof BlockOldLeaf) {
            BlockOldLeaf b = (BlockOldLeaf)block;
            return (BlockPlanks.EnumType)b.getActualState(world.getBlockState(blockPos), world, blockPos).getProperties().get(BlockPlanks.VARIANT);
        }
        if (block instanceof BlockNewLeaf) {
            BlockNewLeaf b = (BlockNewLeaf)block;
            return (BlockPlanks.EnumType)b.getActualState(world.getBlockState(blockPos), world, blockPos).getProperties().get(BlockPlanks.VARIANT);
        }
        if (block instanceof BlockOldLog) {
            BlockOldLog b = (BlockOldLog)block;
            return (BlockPlanks.EnumType)b.getActualState(world.getBlockState(blockPos), world, blockPos).getProperties().get(BlockPlanks.VARIANT);
        }
        if (block instanceof BlockNewLog) {
            BlockNewLog b = (BlockNewLog)block;
            return (BlockPlanks.EnumType)b.getActualState(world.getBlockState(blockPos), world, blockPos).getProperties().get(BlockPlanks.VARIANT);
        }
        return (BlockPlanks.EnumType)block.getActualState(world.getBlockState(blockPos), world, blockPos).getProperties().get(BlockPlanks.VARIANT);
    }
    private BlockPlanks.EnumType getLogType(BlockPos blockPos, Block block, World world, IChunkProvider chunkProvider) {
        if (!(block instanceof BlockLog)) return null;
        BlockPlanks.EnumType type = getWoodType(blockPos, block, world, chunkProvider);
        return type;
    }
    private BlockPlanks.EnumType getLeafType(BlockPos blockPos, Block block, World world, IChunkProvider chunkProvider) {
        if (!(block instanceof BlockLeavesBase)) return null;
        return getWoodType(blockPos, block, world, chunkProvider);
    }

    private void setMinMax(BlockPos pos) {
        if (max == null) {
            max = new int[3]; // x, y, z
            min = new int[3]; // x, y, z
            max[0] = min[0] = pos.getX();
            max[1] = min[1] = pos.getY();
            max[2] = min[2] = pos.getZ();
        } else {
            if (pos.getX() > max[0]) max[0] = pos.getX();
            if (pos.getY() > max[1]) max[1] = pos.getY();
            if (pos.getZ() > max[2]) max[2] = pos.getZ();

            if (pos.getX() < min[0]) min[0] = pos.getX();
            if (pos.getY() < min[1]) min[1] = pos.getY();
            if (pos.getZ() < min[2]) min[2] = pos.getZ();
        }
    }
    private int addAdjoiningLogs(BlockPos logPos, BlockPlanks.EnumType type, Stack<BlockPos> logStack, World world, IChunkProvider chunkProvider) {
        int number_added = 0;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                BlockPos pos = logPos.east(x).north(z);
                Chunk chunk = chunkProvider.provideChunk(pos);
                Block block = chunk.getBlock(pos);
                BlockPlanks.EnumType t = getLogType(pos, block, world, chunkProvider);
                if (t == null) continue;
                /**/
                if (t != type) {
                    if (t != null) {
                    System.out.println(String.format("addAdjoiningLogs: found %s, expected %s", t, type));
                        BlockPlanks.EnumType t3 =
                                (BlockPlanks.EnumType)world.getBlockState(pos).getProperties().get(BlockPlanks.VARIANT);
                        System.out.println(String.format(" - world: %s", t3));
                    }
                    continue;
                }
                /**/
                String logPosKey = makeKey(pos);
                if (! logMap.containsKey(logPosKey)) {
                    logMap.put(logPosKey, pos);
                    logStack.push(pos);
                    setMinMax(pos);
                    number_added++;
                }
            }
        }
        return number_added;
    }

    private void destroyDeadLeaves(World world, IChunkProvider chunkProvider) {
        for (int y = max[1]; y >= min[1] + 4; y--) {
            for (int x = min[0] - 4; x <= max[0] + 4; x++) {
                for (int z = min[2] - 4; z <= max[2] + 4; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    Chunk chunk = chunkProvider.provideChunk(pos);
                    Block block = chunk.getBlock(pos);
                    if (block instanceof BlockLeavesBase) {
                        //block.beginLeavesDecay(world, pos);
                        IBlockState state = block.getActualState(world.getBlockState(pos), world, pos);
                        block.updateTick(world, pos, state, null);
                        boolean decaying = ((Boolean) state.getValue(BlockLeaves.CHECK_DECAY)).booleanValue();
                        if (decaying) {
                            world.destroyBlock(pos, false);
                        }
                    }
                }
            }
        }
    }
    private void destroyLeafBall(BlockPos logPos, BlockPlanks.EnumType type, World world, IChunkProvider chunkProvider) {
        /*
          4      -
          3     ---
          2    -----
          1   -------
          0  ----*----
         */
        List<BlockPos> nearTreeList = new ArrayList<BlockPos>();

        for (int y = -4; y <= 4; y++) {
            BlockPos trunkPos = logPos.up(y);
            //nearTreeList.clear();
            for (int x = 2; x <= 8; x++) {
                for (int z = 2; z <= 8; z++) {
                    BlockPos pos = trunkPos.north(z).east(x);
                    Chunk chunk = chunkProvider.provideChunk(pos);
                    Block block = chunk.getBlock(pos);
                    if (block instanceof BlockLog) {
                        nearTreeList.add(pos);
                    }

                    pos = trunkPos.south(z).east(x);
                    chunk = chunkProvider.provideChunk(pos);
                    block = chunk.getBlock(pos);
                    if (block instanceof BlockLog) {
                        nearTreeList.add(pos);
                    }

                    pos = trunkPos.north(z).west(x);
                    chunk = chunkProvider.provideChunk(pos);
                    block = chunk.getBlock(pos);
                    if (block instanceof BlockLog) {
                        nearTreeList.add(pos);
                    }

                    pos = trunkPos.south(z).west(x);
                    chunk = chunkProvider.provideChunk(pos);
                    block = chunk.getBlock(pos);
                    if (block instanceof BlockLog) {
                        nearTreeList.add(pos);
                    }
                }
            }
            int w = 4 - Math.abs(y);
            for (int x = -w; x <= w; x++) {
                for (int z = -w; z <= w; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    BlockPos blockPos = logPos.up(y).north(x).east(z);
                    Chunk chunk = chunkProvider.provideChunk(blockPos);
                    Block block = chunk.getBlock(blockPos);
                    if (block instanceof BlockLeavesBase) {
                        IBlockState bs = world.getBlockState(blockPos);
                        ImmutableMap properties = bs.getProperties();
                        BlockPlanks.EnumType t = getLeafType(blockPos, block, world, chunkProvider);
                        // jungle trees produce oak leaves close to the ground
                        if (true || t == type || (type == BlockPlanks.EnumType.JUNGLE && t == BlockPlanks.EnumType.OAK)) {
                            double x0 = Math.abs(blockPos.getX() - logPos.getX());
                            double z0 = Math.abs(blockPos.getZ() - logPos.getZ());
                            //double d = x0*x0 + z0*z0;
                            Iterator<BlockPos> it = nearTreeList.iterator();
                            boolean zap = true;
                            while(it.hasNext()) {
                                BlockPos p = it.next();
                                double x1 = Math.abs(blockPos.getX() - p.getX());
                                double z1 = Math.abs(blockPos.getZ() - p.getZ());
                                //double dd = x0*x0 + z0*z0;
                                if(x1 < x0 || z1 < z0) {
                                    zap = false;
                                    break;
                                }
                            }
                            if(zap) world.destroyBlock(blockPos, false);
                            //world.setBlockState(blockPos, Blocks.stained_glass.getDefaultState().withProperty(BlockStainedGlass.COLOR, EnumDyeColor.GREEN));
                        } else {
                            BlockPlanks.EnumType t2 = (BlockPlanks.EnumType)world.getBlockState(blockPos).getProperties().get(BlockPlanks.VARIANT);
                            System.out.println(String.format("destroyLeafBall %s: found %s, expected %s -- world has %s", blockPos.toString(), t, type, t2));
                            //world.destroyBlock(blockPos, false);
                            //world.setBlockState(blockPos, Blocks.stained_glass.getDefaultState().withProperty(BlockStainedGlass.COLOR, EnumDyeColor.RED));
                            if (block instanceof BlockNewLeaf) {
                                BlockNewLeaf b1 = (BlockNewLeaf)block;
                                BlockPlanks.EnumType t3 = (BlockPlanks.EnumType)b1.getActualState(block.getDefaultState(), world, blockPos).getProperties().get(BlockPlanks.VARIANT);
                                System.out.println(String.format(" - BlockNewLeaf: %s", t3));
                            } else if (block instanceof BlockOldLeaf) {
                                BlockOldLeaf b1 = (BlockOldLeaf)block;
                                BlockPlanks.EnumType t3 = (BlockPlanks.EnumType)b1.getActualState(block.getDefaultState(), world, blockPos).getProperties().get(BlockPlanks.VARIANT);
                                System.out.println(String.format(" - BlockOldLeaf: %s", t3));
                            }
                        }
                    }
                }
            }
        }
    }
    public void eatFromRoot(BlockPos origin, World world, IChunkProvider chunkProvider) {
        Chunk chunk = chunkProvider.provideChunk(origin);
        Block block = chunk.getBlock(origin);
        BlockPlanks.EnumType type = getLogType(origin, block, world, chunkProvider);
        if (type == null) return;
        logMap = new HashMap<String, BlockPos>();
        Stack<BlockPos> logStack = new Stack<BlockPos>();

        String logPosKey = makeKey(origin);
        if (!logMap.containsKey(logPosKey)) {
            logMap.put(logPosKey, origin);
            setMinMax(origin);
        }

        logStack.push(origin);

        while(!logStack.isEmpty()) {
            BlockPos logPos = logStack.pop();

            addAdjoiningLogs(logPos, type, logStack, world, chunkProvider);
            BlockPos pos = logPos.up();
            chunk = chunkProvider.provideChunk(pos);
            block = chunk.getBlock(pos);
            BlockPlanks.EnumType t = getLogType(pos, block, world, chunkProvider);
            if (t == type) {
                logPosKey = makeKey(pos);
                if (! logMap.containsKey(logPosKey)) {
                    logMap.put(logPosKey, pos);
                    logStack.push(pos);
                    setMinMax(pos);
                }
            }
            if (t == null) {
                addAdjoiningLogs(logPos.up(), type, logStack, world, chunkProvider);
            }
        }
        Collection<BlockPos> collection =logMap.values();
        Iterator<BlockPos> iterator = collection.iterator();
        while(iterator.hasNext()) {
            BlockPos logPos = iterator.next();
            destroyLeafBall(logPos, type, world, chunkProvider);
            world.destroyBlock(logPos, false);
        }
        /* destroyDeadLeaves(world, chunkProvider); */
        // place sign at origin
        IBlockState signBlockState = Blocks.standing_sign.getDefaultState();
        BlockStandingSign signBlock = (BlockStandingSign)signBlockState.getBlock();
        TileEntity tileentity = signBlock.createNewTileEntity(world, signBlock.getMetaFromState(signBlockState));
        if (tileentity instanceof TileEntitySign) {
            TileEntitySign entity = (TileEntitySign)tileentity;
            entity.signText[0] = new ChatComponentText("TOOK OUT");
            entity.signText[1] = new ChatComponentText(type.getName());
            entity.signText[2] = new ChatComponentText("TREE");
            entity.signText[3] = new ChatComponentText("");
            entity.markDirty();
            BlockPos blockPos = origin;
            block = chunk.getBlock(blockPos);

            while (block instanceof BlockAir) {
                blockPos = blockPos.down();
                block = chunk.getBlock(blockPos);
            }
            while (! (block instanceof BlockAir)) {
                blockPos = blockPos.up();
                block = chunk.getBlock(blockPos);
            }
            world.destroyBlock(blockPos, false);
            world.destroyBlock(blockPos.up(), false);
            world.setBlockState(blockPos, signBlock.getActualState(signBlockState, world, blockPos));
            world.setTileEntity(blockPos, entity);
        }

    }
}

class LeafSearchNode {
    public int depth;
    public BlockPos pos;
}

public class BullDozer {
    enum ACTION {IGNORE, DESTROY, REPLACE}

    static public void destroyLeaves(BlockPlanks.EnumType type, BlockPos origin, BlockPos pos, World world, IChunkProvider chunkProvider) {
        int dist = Math.abs(pos.getX() - origin.getX()) + Math.abs(pos.getZ() - origin.getZ()) + Math.abs(pos.getY() - origin.getY());
        if (dist > 4) return;
        Chunk chunk = chunkProvider.provideChunk(pos);
        Block block = chunk.getBlock(pos);
        if (!(block instanceof BlockLeavesBase) && !(block instanceof BlockLog)) return;
        BlockPlanks.EnumType type1 = (BlockPlanks.EnumType) block.getActualState(block.getDefaultState(), world, pos).getProperties().get(BlockPlanks.VARIANT);
        if (type != type1) return;
        if (block instanceof BlockLeavesBase) {
            world.destroyBlock(pos, false);
            //world.setBlockState(pos, Blocks.glass.getDefaultState());
            destroyLeaves(type, origin, pos.north(), world, chunkProvider);
            destroyLeaves(type, origin, pos.east(), world, chunkProvider);
            destroyLeaves(type, origin, pos.south(), world, chunkProvider);
            destroyLeaves(type, origin, pos.west(), world, chunkProvider);
            //destroyLeaves(type, origin, pos.up(), world, chunkProvider);
            //destroyLeaves(type, origin, pos.down(), world, chunkProvider);
        } else if (block instanceof BlockLog) {
            if (dist == 1) {
                //destroyTree(pos, world, chunkProvider);
            }
        }
    }
    static private String makeBlockPosKey(BlockPos blockPos) {
        return String.format("%d,%d,%d",blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }
    // Tree Notes: branches can be diagonal
    static public BlockPos findTreeRoot(BlockPos logPos, IChunkProvider chunkProvider) {
        Chunk chunk = chunkProvider.provideChunk(logPos);
        Block logBlock = chunk.getBlock(logPos);
        if (!(logBlock instanceof BlockLog)) {
            return null;
        }
        Block rootBlock = chunk.getBlock(logPos.down());
        if(TerrainMap.isGround(rootBlock)) {
            return logPos;
        }
        BlockPos rootPos = findTreeRoot(logPos.down(), chunkProvider);
        if (rootPos != null) return rootPos;
        rootPos = findTreeRoot(logPos.down().east(), chunkProvider);
        if (rootPos != null) return rootPos;
        rootPos = findTreeRoot(logPos.down().south(), chunkProvider);
        if (rootPos != null) return rootPos;
        rootPos = findTreeRoot(logPos.down().west(), chunkProvider);
        if (rootPos != null) return rootPos;
        rootPos = findTreeRoot(logPos.down().north(), chunkProvider);
        if (rootPos != null) return rootPos;

        rootPos = findTreeRoot(logPos.down().east().north(), chunkProvider);
        if (rootPos != null) return rootPos;
        rootPos = findTreeRoot(logPos.down().south().east(), chunkProvider);
        if (rootPos != null) return rootPos;
        rootPos = findTreeRoot(logPos.down().west().south(), chunkProvider);
        if (rootPos != null) return rootPos;
        rootPos = findTreeRoot(logPos.down().north().west(), chunkProvider);
        return rootPos;
    }

    static private BlockPos findTreeCheckLeafNeighbor(BlockPos pos, int depth, Deque<LeafSearchNode> leafQueue, Map<String, BlockPos> leafMap, IChunkProvider chunkProvider) {
        if (depth >= 5) return null;
        if (leafMap.containsKey(makeBlockPosKey(pos))) return null;

        Chunk chunk = chunkProvider.provideChunk(pos);
        Block block = chunk.getBlock(pos);
        if (block instanceof BlockLog) return pos;
        if (!(block instanceof BlockLeavesBase)) return null;
        // Check types?
        LeafSearchNode node1 = new LeafSearchNode();
        node1.depth = depth + 1;
        node1.pos = pos;
        leafQueue.addLast(node1);

        return null;
    }
    static private BlockPos findTreeVisitNextLeafNode(Deque<LeafSearchNode> leafQueue, Map<String, BlockPos> leafMap, IChunkProvider chunkProvider) {
        BlockPos logPos = null;
        LeafSearchNode node = leafQueue.removeFirst();
        BlockPos leafPos = node.pos;
        // adding the node to the map marks it as visited
        leafMap.put(makeBlockPosKey(leafPos), leafPos);

        logPos = findTreeCheckLeafNeighbor(leafPos.north(), node.depth, leafQueue, leafMap, chunkProvider);
        if (logPos != null) return logPos;

        logPos = findTreeCheckLeafNeighbor(leafPos.east(), node.depth, leafQueue, leafMap, chunkProvider);
        if (logPos != null) return logPos;

        logPos = findTreeCheckLeafNeighbor(leafPos.south(), node.depth, leafQueue, leafMap, chunkProvider);
        if (logPos != null) return logPos;

        logPos = findTreeCheckLeafNeighbor(leafPos.west(), node.depth, leafQueue, leafMap, chunkProvider);
        if (logPos != null) return logPos;

        logPos = findTreeCheckLeafNeighbor(leafPos.up(), node.depth, leafQueue, leafMap, chunkProvider);
        if (logPos != null) return logPos;

        logPos = findTreeCheckLeafNeighbor(leafPos.down(), node.depth, leafQueue, leafMap, chunkProvider);
        if (logPos != null) return logPos;

        return logPos;
    }
    // Use BFS to find nearest wood
    static public BlockPos findTreeForLeaves(BlockPos leafPos, World world, IChunkProvider chunkProvider) {
        Chunk chunk = chunkProvider.provideChunk(leafPos);
        Block block = chunk.getBlock(leafPos);
        if (! (block instanceof BlockLeavesBase)) return null;

        Map<String, BlockPos> leafMap = new HashMap<String, BlockPos>();
        Deque<LeafSearchNode> leafQueue = new LinkedList<LeafSearchNode>();

        LeafSearchNode node = new LeafSearchNode();
        node.depth = 0;
        node.pos = leafPos;
        leafQueue.addLast(node);

        BlockPos logPos = null;
        while(!leafQueue.isEmpty()) {
            logPos = findTreeVisitNextLeafNode(leafQueue, leafMap, chunkProvider);
            if (logPos != null) return logPos;
        }
        return logPos;
    }
    static public List<BlockPos> findTreeForLeavesXXX(BlockPos leafPos, World world, IChunkProvider chunkProvider) {
        Chunk chunk = chunkProvider.provideChunk(leafPos);
        Block block = chunk.getBlock(leafPos);
        if (! (block instanceof BlockLeavesBase)) return null;

        List<BlockPos> treeList = new ArrayList<BlockPos>();
        IBlockState bs = world.getBlockState(leafPos);
        ImmutableMap properties = bs.getProperties();
        BlockPlanks.EnumType type = (BlockPlanks.EnumType) properties.get(BlockPlanks.VARIANT);

        for (int y = -4; y <= 4; y++) {
            int w = 5 - y;
            for (int x = -w; x <= w; x++) {
                for (int z = -w; z <= w; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    BlockPos blockPos = leafPos.up(y).north(x).east(z);
                    chunk = chunkProvider.provideChunk(blockPos);
                    block = chunk.getBlock(blockPos);
                    if (block instanceof BlockLog) {
                        bs = world.getBlockState(blockPos);
                        properties = bs.getProperties();
                        BlockPlanks.EnumType t = (BlockPlanks.EnumType) properties.get(BlockPlanks.VARIANT);
                        if (t == type) {
                            treeList.add(blockPos);
                        }
                    }
                }
            }
        }

        return treeList;
    }

    static private void checkTreeNodeNeighbor(BlockPos blockPos, Deque<BlockPos> treeQueue, Map<String, BlockPos> treeMap, IChunkProvider chunkProvider) {
        if (treeMap.containsKey(makeBlockPosKey(blockPos))) return;
        Chunk chunk = chunkProvider.provideChunk(blockPos);
        Block block = chunk.getBlock(blockPos);
        if (!(block instanceof  BlockLog)) return;
        treeQueue.addLast(blockPos);
    }
    static private void visitNextTreeNode(Deque<BlockPos> treeQueue, Map<String, BlockPos> treeMap, IChunkProvider chunkProvider) {
        BlockPos blockPos = treeQueue.removeFirst();
        if (treeMap.containsKey(makeBlockPosKey(blockPos))) return;
        Chunk chunk = chunkProvider.provideChunk(blockPos);
        Block block = chunk.getBlock(blockPos);
        if (!(block instanceof  BlockLog)) return;

        treeMap.put(makeBlockPosKey(blockPos), blockPos);

        checkTreeNodeNeighbor(blockPos.up(), treeQueue, treeMap, chunkProvider);
        checkTreeNodeNeighbor(blockPos.down(), treeQueue, treeMap, chunkProvider);

        checkTreeNodeNeighbor(blockPos.up().north(), treeQueue, treeMap, chunkProvider);
        checkTreeNodeNeighbor(blockPos.up().east(), treeQueue, treeMap, chunkProvider);
        checkTreeNodeNeighbor(blockPos.up().south(), treeQueue, treeMap, chunkProvider);
        checkTreeNodeNeighbor(blockPos.up().west(), treeQueue, treeMap, chunkProvider);

        checkTreeNodeNeighbor(blockPos.down().north(), treeQueue, treeMap, chunkProvider);
        checkTreeNodeNeighbor(blockPos.down().east(), treeQueue, treeMap, chunkProvider);
        checkTreeNodeNeighbor(blockPos.down().south(), treeQueue, treeMap, chunkProvider);
        checkTreeNodeNeighbor(blockPos.down().west(), treeQueue, treeMap, chunkProvider);

        checkTreeNodeNeighbor(blockPos.north(), treeQueue, treeMap, chunkProvider);
        checkTreeNodeNeighbor(blockPos.east(), treeQueue, treeMap, chunkProvider);
        checkTreeNodeNeighbor(blockPos.south(), treeQueue, treeMap, chunkProvider);
        checkTreeNodeNeighbor(blockPos.west(), treeQueue, treeMap, chunkProvider);

        checkTreeNodeNeighbor(blockPos.north().east(), treeQueue, treeMap, chunkProvider);
        checkTreeNodeNeighbor(blockPos.south().east(), treeQueue, treeMap, chunkProvider);
        checkTreeNodeNeighbor(blockPos.north().west(), treeQueue, treeMap, chunkProvider);
        checkTreeNodeNeighbor(blockPos.south().west(), treeQueue, treeMap, chunkProvider);
    }
    static public Map<String, BlockPos> makeTreeMap(BlockPos blockPos, IChunkProvider chunkProvider) {
        Chunk chunk = chunkProvider.provideChunk(blockPos);
        Block block = chunk.getBlock(blockPos);
        Map<String, BlockPos> treeMap = null;
        if (block instanceof BlockLog) {
            treeMap = new HashMap<String, BlockPos>();
            Deque<BlockPos> treeQueue = new LinkedList<BlockPos>();
            treeQueue.addLast(blockPos);

            while(!treeQueue.isEmpty()) {
                visitNextTreeNode(treeQueue, treeMap, chunkProvider);
            }
        }
        return treeMap;
    }
    static public boolean destroyTree(BlockPos blockPos, World world, IChunkProvider chunkProvider) {
        Chunk chunk = chunkProvider.provideChunk(blockPos);
        Block block = chunk.getBlock(blockPos);
        if (block instanceof BlockLeavesBase) {
            BlockPos logPos = findTreeForLeaves(blockPos, world, chunkProvider);
            if (logPos == null) return false;
            blockPos = logPos;
            chunk = chunkProvider.provideChunk(blockPos);
            block = chunk.getBlock(blockPos);
        }
        if (block instanceof BlockLog) {
            String type = "UNKNOWN";
            IBlockState bs = world.getBlockState(blockPos);
            type = bs.getValue(BlockPlanks.VARIANT).toString();
            Map<String, BlockPos> treeMap = makeTreeMap(blockPos, chunkProvider);
            if (treeMap == null) return false;

            // minx, miny, minz, maxx, maxy, maxz
            int[] bounds = null;
            Iterator<BlockPos> it = treeMap.values().iterator();
            while (it.hasNext()) {
                BlockPos pos = it.next();
                if (bounds == null) {
                    bounds = new int[6];
                    bounds[0] = bounds[3] = pos.getX();
                    bounds[1] = bounds[4] = pos.getY();
                    bounds[2] = bounds[5] = pos.getZ();
                } else {
                    if (pos.getX() < bounds[0]) bounds[0] = pos.getX();
                    else if (pos.getX() > bounds[3]) bounds[3] = pos.getX();

                    if (pos.getY() < bounds[1]) bounds[1] = pos.getY();
                    else if (pos.getY() > bounds[4]) bounds[4] = pos.getY();

                    if (pos.getZ() < bounds[2]) bounds[2] = pos.getZ();
                    else if (pos.getZ() > bounds[5]) bounds[5] = pos.getZ();
                }
            }
            int w = 8;
            bounds[0] -= w; // minx
            //bounds[1] -= w; // miny
            bounds[2] -= w;   // minz
            bounds[3] += w;   // maxx
            bounds[4] += w;   // maxy
            bounds[5] += w;   // maxz

            List<BlockPos> leafList = new ArrayList<BlockPos>();
            for (int y = bounds[4]; y >= bounds[1]; y--) {
                for (int x = bounds[0]; x <= bounds[3]; x++) {
                    for (int z = bounds[2]; z <= bounds[5]; z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        chunk = chunkProvider.provideChunk(pos);
                        block = chunk.getBlock(pos);
                        if (block instanceof BlockLeavesBase) {
                            BlockPos logPos = findTreeForLeaves(pos, world, chunkProvider);
                            if (logPos != null && treeMap.containsKey(makeBlockPosKey(logPos))) {
                                leafList.add(pos);
                            }
                        }
                    }
                }
            }
            it = leafList.iterator();
            while(it.hasNext()) {
                world.destroyBlock(it.next(), false);
            }
            it = treeMap.values().iterator();
            BlockPos signPos = null;
            while(it.hasNext()) {
                BlockPos pos = it.next();
                if (signPos == null) {
                    signPos = pos;
                }
                world.destroyBlock(pos, false);
                //world.setBlockState(pos, Blocks.glass.getDefaultState());
            }
            // place sign at origin
            IBlockState signBlockState = Blocks.standing_sign.getDefaultState();
            BlockStandingSign signBlock = (BlockStandingSign)signBlockState.getBlock();
            TileEntity tileentity = signBlock.createNewTileEntity(world, signBlock.getMetaFromState(signBlockState));
            if (tileentity instanceof TileEntitySign) {
                TileEntitySign entity = (TileEntitySign)tileentity;
                entity.signText[0] = new ChatComponentText("TOOK OUT");
                entity.signText[1] = new ChatComponentText(type);
                entity.signText[2] = new ChatComponentText("TREE");
                entity.signText[3] = new ChatComponentText("");
                entity.markDirty();
                block = chunk.getBlock(signPos);

                while (block instanceof BlockAir) {
                    signPos = signPos.down();
                    block = chunk.getBlock(signPos);
                }
                while (! (block instanceof BlockAir)) {
                    signPos = signPos.up();
                    block = chunk.getBlock(signPos);
                }
                world.destroyBlock(signPos, false);
                world.destroyBlock(signPos.up(), false);
                world.setBlockState(signPos, signBlock.getActualState(signBlockState, world, signPos));
                world.setTileEntity(signPos, entity);
            }
        }
        return false;
    }
    static public boolean destroyTreeXXX(BlockPos blockPos, World world, IChunkProvider chunkProvider) {
        Chunk chunk = chunkProvider.provideChunk(blockPos);
        Block block = chunk.getBlock(blockPos);

        List<BlockPos> treeList;
        if (block instanceof BlockLeavesBase) {
            return false;
        }
        if (block instanceof BlockLog) {
            IBlockState bs = block.getActualState(block.getDefaultState(), world, blockPos);
            BlockPlanks.EnumType type = null;
            ImmutableMap properties = bs.getProperties();
            type = (BlockPlanks.EnumType) properties.get(BlockPlanks.VARIANT);
            if (type == null) return false;
            //world.destroyBlock(blockPos, false);
            if( ! destroyTree(blockPos.north(), world, chunkProvider)) {
                destroyLeaves(type, blockPos, blockPos.north(), world, chunkProvider);
            }
            if( ! destroyTree(blockPos.east(), world, chunkProvider)) {
                destroyLeaves(type, blockPos, blockPos.east(), world, chunkProvider);
            }
            if( ! destroyTree(blockPos.south(), world, chunkProvider)) {
                destroyLeaves(type, blockPos, blockPos.south(), world, chunkProvider);
                /*
                treeList = findTreeForLeaves(blockPos, world, chunkProvider);
            } else {
            treeList = new ArrayList<BlockPos>();
            treeList.add(blockPos);
        }
        boolean rval = false;
        Iterator<BlockPos> iterator = treeList.iterator();
        while(iterator.hasNext()) {
            BlockPos logPos = iterator.next();
            if (logPos != null) {
                blockPos = logPos;
                block = chunk.getBlock(blockPos);
                if (!(block instanceof BlockLog)) {
                    continue;
                }
            } else {
                continue;
                */
            }
            if( ! destroyTree(blockPos.west(), world, chunkProvider)) {
                destroyLeaves(type, blockPos, blockPos.west(), world, chunkProvider);
            }
                /**/
            if (block instanceof BlockLog) {
                BlockPos rootPos = findTreeRoot(blockPos, chunkProvider);
                if (rootPos != null) {
                    blockPos = rootPos;
                } else {
                    System.out.println(String.format("destroyTree: findTreeRoot returned null at %s", blockPos.toString()));
                    //continue;
                    return false;
                }
                bs = world.getBlockState(blockPos);
                type = null;
                properties = bs.getProperties();
                type = (BlockPlanks.EnumType) properties.get(BlockPlanks.VARIANT);
                /**/
                if (type == null) return false;
                //////////////
                //rval = true;
            }
            TreeEater treeEater = new TreeEater();
            treeEater.eatFromRoot(blockPos, world, chunkProvider);

            //if (! destroyTree(blockPos.up(), world, chunkProvider)) {
            destroyLeaves(type, blockPos.up(), blockPos.up(), world, chunkProvider);
            //}
            destroyTree(blockPos.down(), world, chunkProvider);
            return true;
        }
        //return rval;
        return false;
    }

    static public void setTerrainHeight(int x, int target_h, int z,
                                        Random random, World world, IChunkProvider chunkProvider,
                                        IBlockChooser blockChooser) {
        BlockPos pos = new BlockPos(x, target_h, z);
        Chunk chunk = chunkProvider.provideChunk(pos);
        int h = chunk.getHeight(pos);
        int ground = TerrainMap.getGroundLevel(new BlockPos(x, h, z), world, chunkProvider);
        int water = TerrainMap.getGroundOrWaterLevel(new BlockPos(x, h, z), world, chunkProvider);
        if (water > ground && blockChooser == null) {
            return;
        }
        BlockPos groundPos = new BlockPos(x, ground, z);
        destroyTree(groundPos.up(), world, chunkProvider);
        // clear vegetation
        if (h > ground) {
            for(int y = h; y > ground; y--) {
                Block block = chunk.getBlock(new BlockPos(x, y, z));
                /*
                BlockPos blockPos = new BlockPos(x, y, z);
                Block block = chunk.getBlock(blockPos);
                */
                if (block instanceof IGrowable) {
                    //if (! destroyTree(new BlockPos(x, y, z), world, chunkProvider)) {
                    world.destroyBlock(new BlockPos(x, y, z), false);
                    //}
                    /*
                    if (!destroyTree(blockPos, world, chunkProvider)) world.destroyBlock(blockPos, false);
                    */
                }
            }
        }
        IBlockState groundTop = null;
        if (h > target_h) {
            //
            // *** need to dig ***
            //
            // find ground block
            for (int y = h; y >= ground; y--) {
                BlockPos bp = new BlockPos(x, y, z);
                Block block = chunk.getBlock(bp);
                if (TerrainMap.isGround(block)) {
                    groundTop = world.getBlockState(bp);
                    break;
                }
            }
            for (int y = h; y > target_h; y--) {
                BlockPos bp = new BlockPos(x, y, z);
                Block block = chunk.getBlock(bp);
                boolean decayable = false;
                if (!destroyTree(bp, world, chunkProvider)) {
                        world.destroyBlock(bp, false);
                }
                /**
                IBlockState glass = Blocks.glass.getDefaultState();
                if (!chunk.getBlock(new BlockPos(x, y, z)).isAir(null, null)) {
                    world.setBlockState(new BlockPos(x, y, z), glass);
                }
                **/
            }
            BlockPos target_pos = new BlockPos(x, target_h, z);

            IBlockState block = null;
            if (blockChooser != null) {
                block = blockChooser.chooseBlock(target_pos, world, chunkProvider);
                if (! destroyTree(target_pos, world, chunkProvider)) {
                    world.destroyBlock(target_pos, false);
                }
                world.setBlockState(target_pos, block);
            } else {
                // replace with what was on top
                if (groundTop != null) {
                    if (!destroyTree(target_pos, world, chunkProvider)) {
                        world.destroyBlock(target_pos, false);
                    }
                    world.setBlockState(target_pos, groundTop);
                }
            }
            if (blockChooser == null) {
                block = world.getBlockState(target_pos);
            }

            // check for gaps under the tower and fill them in
            target_pos = target_pos.down();
            while(world.isAirBlock(target_pos)) {
                if (blockChooser != null) {
                    block = blockChooser.chooseBlock(target_pos, world, chunkProvider);
                }
                world.setBlockState(target_pos, block);
                target_pos = target_pos.down();
            }
        } else if (ground < target_h) {
            // *** need to fill ***
            Block b = chunk.getBlock(new BlockPos(x, ground, z));
            BlockPos bp = new BlockPos(x, ground, z);
            IBlockState block = world.getBlockState(bp);
            if (world.isAirBlock(bp) || TerrainMap.isLiquid(block.getBlock())) {
                System.out.println("Filling With Water or Air");
            }

            for (int y = target_h; y > ground; y--) {
                BlockPos pos1 = new BlockPos(x, y, z);
                if (blockChooser != null) {
                    block = blockChooser.chooseBlock(pos1, world, chunkProvider);
                }
                if (!destroyTree(pos1, world, chunkProvider)) world.destroyBlock(pos1, false);
                world.setBlockState(pos1, block);
                // if we are over water, don't fill
                if (water > ground) {
                    return;
                }
            }
        } else {
            // check for water
            if (blockChooser != null) {
                BlockPos blockPos = new BlockPos(x, target_h, z);
                if (! destroyTree(blockPos, world, chunkProvider)) {
                    world.destroyBlock(blockPos, false);
                }
                world.setBlockState(blockPos, blockChooser.chooseBlock(blockPos, world, chunkProvider));
            }
        }
    }
    static public void bullDoze(BlockPos _pos, char[][] footprint,
                                IBlockChooser groundsBlockChooser,
                                IBlockChooser foundationBlockChooser,
                                Random random, World world, IChunkProvider chunkProvider) {
        int w = footprint[0].length;
        int h = footprint.length;
        for (int x = 0; x < w; x++) {
            int xx = x + _pos.getX();
            for (int z = 0; z < h; z++) {
                int zz = z + _pos.getZ();
                char fp = footprint[x][z];
                if (fp == ' ') {
                    continue;
                }
                int py = _pos.getY();

                if (fp ==  'g'){
                    IBlockState stone = Block.getStateById(5);
                    setTerrainHeight(xx, py, zz, random, world, chunkProvider, groundsBlockChooser);
                } else if (fp == 'W' || fp == 'F' || fp == 'D' || fp == 'c') {
                    setTerrainHeight(xx, py, zz, random, world, chunkProvider, foundationBlockChooser);
                } else if (fp >= '0' && fp <= '9'){
                    double pct = 1.0 - (double)(fp - '0')/10.0;
                    int ground = TerrainMap.getGroundLevel(new BlockPos(xx, py, zz), world, chunkProvider);
                    int slope_y = py + (int)Math.round((ground - py) * pct);
                    setTerrainHeight(xx, slope_y, zz, random, world, chunkProvider, null);
                }
            }
        }
    }
}
