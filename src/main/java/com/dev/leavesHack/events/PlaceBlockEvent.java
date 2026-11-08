package com.dev.leavesHack.events;

import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;

public class PlaceBlockEvent {

    public BlockPos blockPos;
    public Block block;

    public PlaceBlockEvent(BlockPos blockPos, Block block) {
        this.blockPos = blockPos;
        this.block = block;
    }
}