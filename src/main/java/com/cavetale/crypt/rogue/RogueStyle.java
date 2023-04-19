package com.cavetale.crypt.rogue;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public enum RogueStyle {
    STONE {
        @Override public BlockData floor(RogueContext context) {
            return Material.SMOOTH_STONE.createBlockData();
        }

        @Override public BlockData ceiling(RogueContext context) {
            return Material.OAK_PLANKS.createBlockData();
        }

        @Override public BlockData wall(RogueContext context) {
            if (context.tile.isCorner()) {
                return Material.STRIPPED_OAK_LOG.createBlockData();
            } else if (context.y == context.floorLevel + 1 || context.hints.contains(RogueContext.Hint.DOOR_FRAME)) {
                return switch (context.random.nextInt(10)) {
                case 0 -> Material.MOSSY_COBBLESTONE.createBlockData();
                default -> Material.COBBLESTONE.createBlockData();
                };
            } else {
                return switch (context.random.nextInt(10)) {
                case 0 -> Material.STONE.createBlockData();
                case 1 -> Material.MOSSY_STONE_BRICKS.createBlockData();
                case 2 -> Material.CRACKED_STONE_BRICKS.createBlockData();
                default -> Material.STONE_BRICKS.createBlockData();
                };
            }
        }
    },
    DESERT {
        @Override public BlockData floor(RogueContext context) {
            return Material.SAND.createBlockData();
        }

        @Override public BlockData ceiling(RogueContext context) {
            return Material.SANDSTONE.createBlockData();
        }

        @Override public BlockData wall(RogueContext context) {
            if (context.y == context.floorLevel + 1) {
                return Material.CHISELED_SANDSTONE.createBlockData();
            } else if (context.tile.isCorner() || context.hints.contains(RogueContext.Hint.DOOR_FRAME)) {
                return Material.CUT_SANDSTONE.createBlockData();
            } else {
                return Material.SMOOTH_SANDSTONE.createBlockData();
            }
        }
    },
    ;

    public abstract BlockData floor(RogueContext context);
    public abstract BlockData ceiling(RogueContext context);
    public abstract BlockData wall(RogueContext context);

    public BlockData pillar(RogueContext context) {
        return wall(context);
    }
}
