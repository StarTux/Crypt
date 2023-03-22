package com.cavetale.crypt.rogue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.block.BlockFace;

@Getter @RequiredArgsConstructor
public enum RogueTile {
    UNDEFINED(Type.UNDEFINED, '?'),
    FLOOR(Type.FLOOR, '.'),
    WALL_NORTH(Type.WALL, BlockFace.NORTH, '\u2550'),
    WALL_EAST(Type.WALL, BlockFace.EAST, '\u2551'),
    WALL_SOUTH(Type.WALL, BlockFace.SOUTH, '\u2550'),
    WALL_WEST(Type.WALL, BlockFace.WEST, '\u2551'),
    CORNER_NE(Type.WALL, BlockFace.NORTH_EAST, '\u2557'),
    CORNER_SE(Type.WALL, BlockFace.SOUTH_EAST, '\u255D'),
    CORNER_SW(Type.WALL, BlockFace.SOUTH_WEST, '\u255A'),
    CORNER_NW(Type.WALL, BlockFace.NORTH_WEST, '\u2554'),
    CORNER_INNER_NE(Type.WALL, BlockFace.NORTH_EAST, '\u255A'),
    CORNER_INNER_SE(Type.WALL, BlockFace.SOUTH_EAST, '\u2554'),
    CORNER_INNER_SW(Type.WALL, BlockFace.SOUTH_WEST, '\u2557'),
    CORNER_INNER_NW(Type.WALL, BlockFace.NORTH_WEST, '\u255D'),
    DOOR(Type.DOOR, '\u259A'),
    ;

    public final Type type;
    public final BlockFace blockFace;
    public final char character;

    RogueTile(final Type type, final char character) {
        this(type, BlockFace.SELF, character);
    }

    public enum Type {
        UNDEFINED,
        FLOOR,
        WALL,
        DOOR,
    }

    public boolean isUndefined() {
        return type == Type.UNDEFINED;
    }

    public boolean isFloor() {
        return type == Type.FLOOR;
    }

    public boolean isWall() {
        return type == Type.WALL;
    }

    public boolean isDoor() {
        return type == Type.DOOR;
    }

    public static RogueTile wall(BlockFace face) {
        return switch (face) {
        case NORTH -> RogueTile.WALL_NORTH;
        case EAST -> RogueTile.WALL_EAST;
        case SOUTH -> RogueTile.WALL_SOUTH;
        case WEST -> RogueTile.WALL_WEST;
        default -> throw new IllegalArgumentException(face.name());
        };
    }

    public static RogueTile corner(BlockFace face) {
        return switch (face) {
        case NORTH_EAST -> RogueTile.CORNER_NE;
        case SOUTH_EAST -> RogueTile.CORNER_SE;
        case SOUTH_WEST -> RogueTile.CORNER_SW;
        case NORTH_WEST -> RogueTile.CORNER_NW;
        default -> throw new IllegalArgumentException(face.name());
        };
    }

    public static RogueTile innerCorner(BlockFace face) {
        return switch (face) {
        case NORTH_EAST -> RogueTile.CORNER_INNER_NE;
        case SOUTH_EAST -> RogueTile.CORNER_INNER_SE;
        case SOUTH_WEST -> RogueTile.CORNER_INNER_SW;
        case NORTH_WEST -> RogueTile.CORNER_INNER_NW;
        default -> throw new IllegalArgumentException(face.name());
        };
    }
}
