package com.cavetale.crypt.rogue;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public enum RogueStyle {
    STONE {
        @Override public BlockData floor(RogueContext context, int x, int y, int z) {
            return Material.SMOOTH_STONE.createBlockData();
        }

        @Override public BlockData ceiling(RogueContext context, int x, int y, int z) {
            return Material.OAK_PLANKS.createBlockData();
        }

        @Override public BlockData wall(RogueContext context, int x, int y, int z) {
            return y == context.floorLevel + 1
                ? Material.COBBLESTONE.createBlockData()
                : Material.STONE_BRICKS.createBlockData();
        }
    },
    DESERT {
        @Override public BlockData floor(RogueContext context, int x, int y, int z) {
            return Material.SAND.createBlockData();
        }

        @Override public BlockData ceiling(RogueContext context, int x, int y, int z) {
            return Material.SANDSTONE.createBlockData();
        }

        @Override public BlockData wall(RogueContext context, int x, int y, int z) {
            return y == context.floorLevel + 1
                ? Material.CHISELED_SANDSTONE.createBlockData()
                : Material.CUT_SANDSTONE.createBlockData();
        }
    },
    ;

    public abstract BlockData floor(RogueContext context, int x, int y, int z);
    public abstract BlockData ceiling(RogueContext context, int x, int y, int z);
    public abstract BlockData wall(RogueContext context, int x, int y, int z);
}
