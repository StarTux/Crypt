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
    CORNER_NE(Type.CORNER, BlockFace.NORTH_EAST, '\u2557'),
    CORNER_SE(Type.CORNER, BlockFace.SOUTH_EAST, '\u255D'),
    CORNER_SW(Type.CORNER, BlockFace.SOUTH_WEST, '\u255A'),
    CORNER_NW(Type.CORNER, BlockFace.NORTH_WEST, '\u2554'),
    CORNER_INNER_NE(Type.CORNER_INNER, BlockFace.NORTH_EAST, '\u255A'),
    CORNER_INNER_SE(Type.CORNER_INNER, BlockFace.SOUTH_EAST, '\u2554'),
    CORNER_INNER_SW(Type.CORNER_INNER, BlockFace.SOUTH_WEST, '\u2557'),
    CORNER_INNER_NW(Type.CORNER_INNER, BlockFace.NORTH_WEST, '\u255D'),
    DOOR_WEST(Type.DOOR, BlockFace.WEST, '\u2B9C'), // '\u2588'
    DOOR_NORTH(Type.DOOR, BlockFace.NORTH, '\u2B9D'),
    DOOR_EAST(Type.DOOR, BlockFace.EAST, '\u2B9E'),
    DOOR_SOUTH(Type.DOOR, BlockFace.SOUTH, '\u2B9F'),
    PIT(Type.PIT, ' '),
    WATER(Type.LIQUID, '_'),
    LAVA(Type.LIQUID, '&'),
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
        CORNER,
        CORNER_INNER,
        DOOR,
        PIT,
        LIQUID,
    }

    public boolean isUndefined() {
        return type == Type.UNDEFINED;
    }

    public boolean isFloor() {
        return type == Type.FLOOR;
    }

    public boolean isWall() {
        return type == Type.WALL
            || type == Type.CORNER
            || type == Type.CORNER_INNER;
    }

    public boolean isCorner() {
        return type == Type.CORNER
            || type == Type.CORNER_INNER;
    }

    public boolean isDoor() {
        return type == Type.DOOR;
    }

    public boolean isPit() {
        return type == Type.PIT;
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

    public static RogueTile door(BlockFace face) {
        return switch (face) {
        case NORTH -> RogueTile.DOOR_NORTH;
        case EAST -> RogueTile.DOOR_EAST;
        case SOUTH -> RogueTile.DOOR_SOUTH;
        case WEST -> RogueTile.DOOR_WEST;
        default -> throw new IllegalArgumentException(face.name());
        };
    }

    public int getDepth() {
        return switch (this) {
        case PIT -> -1;
        case WATER -> 5;
        case LAVA -> 5;
        default -> 0;
        };
    }
}
