package com.cavetale.crypt.rogue;

import com.cavetale.core.struct.Vec2i;
import com.cavetale.crypt.struct.Area;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.block.BlockFace;

/**
 * Room abstraction for the RogueGenerator.
 * Care must be taken because most values use world coordinates while
 * the board uses room coordinates.
 */
final class RogueRoom {
    // World Coordinates:
    protected final List<Area> areas = new ArrayList<>();
    protected final List<RogueRoom> nbors = new ArrayList<>();
    // Room Coordinates:
    protected RogueBoard board;
    protected Area boundingBox;

    protected RogueRoom() { }

    protected RogueRoom(final Area mainArea) {
        areas.add(mainArea);
    }

    /**
     * Combiner constructor.
     */
    protected RogueRoom(final RogueRoom a, final RogueRoom b) {
        areas.addAll(a.areas);
        areas.addAll(b.areas);
        nbors.addAll(a.nbors);
        for (RogueRoom nbor : b.nbors) {
            if (!nbors.contains(nbor)) nbors.add(nbor);
        }
        nbors.remove(a);
        nbors.remove(b);
    }

    protected int getArea() {
        int result = 0;
        for (Area area : areas) result += area.getArea();
        return result;
    }

    public boolean contains(Vec2i vec) {
        for (Area area : areas) {
            if (area.contains(vec)) return true;
        }
        return false;
    }

    private static final BlockFace[] ADJACENT_FACES = {
        BlockFace.NORTH,
        BlockFace.EAST,
        BlockFace.SOUTH,
        BlockFace.WEST,
        BlockFace.NORTH_EAST,
        BlockFace.SOUTH_EAST,
        BlockFace.SOUTH_WEST,
        BlockFace.NORTH_WEST,
    };

    protected void makeBoundingBox() {
        this.boundingBox = Area.getBoundingBox(areas);
    }

    protected void makeBoard() {
        this.board = new RogueBoard(boundingBox.getSizeX(), boundingBox.getSizeZ());
        for (Area area : areas) {
            int w = area.getSizeX();
            int h = area.getSizeZ();
            for (int z = 0; z < h; z += 1) {
                for (int x = 0; x < w; x += 1) {
                    board.setTile(area.ax + x - boundingBox.ax, area.az + z - boundingBox.az, RogueTile.FLOOR);
                }
            }
            for (Vec2i wall : area.getWalls()) {
                List<BlockFace> emptyDiagFaces = new ArrayList<>();
                List<BlockFace> emptyCartFaces = new ArrayList<>();
                for (BlockFace face : ADJACENT_FACES) {
                    final int dx = face.getModX();
                    final int dz = face.getModZ();
                    if (contains(wall.add(dx, dz))) continue;
                    if (face.isCartesian()) {
                        emptyCartFaces.add(face);
                    } else {
                        emptyDiagFaces.add(face);
                    }
                }
                if (emptyCartFaces.isEmpty() && emptyDiagFaces.isEmpty()) continue;
                RogueTile tile = null;
                if (emptyCartFaces.size() == 1) {
                    BlockFace singleFace = emptyCartFaces.iterator().next();
                    tile = switch (singleFace) {
                    case NORTH -> RogueTile.WALL_NORTH;
                    case EAST -> RogueTile.WALL_EAST;
                    case SOUTH -> RogueTile.WALL_SOUTH;
                    case WEST -> RogueTile.WALL_WEST;
                    default -> throw new IllegalStateException(singleFace.name());
                    };
                } else if (emptyDiagFaces.size() == 1) {
                    BlockFace singleFace = emptyDiagFaces.iterator().next();
                    tile = switch (singleFace) {
                    case NORTH_EAST -> RogueTile.CORNER_INNER_NE;
                    case SOUTH_EAST -> RogueTile.CORNER_INNER_SE;
                    case SOUTH_WEST -> RogueTile.CORNER_INNER_SW;
                    case NORTH_WEST -> RogueTile.CORNER_INNER_NW;
                    default -> null;
                    };
                } else if (emptyCartFaces.size() == 2) {
                    int dx = 0;
                    int dz = 0;
                    for (BlockFace it : emptyCartFaces) {
                        if (it.getModX() != 0) dx = it.getModX();
                        if (it.getModZ() != 0) dz = it.getModZ();
                    }
                    for (BlockFace it : emptyDiagFaces) {
                        if (it.getModX() == dx && it.getModZ() == dz) {
                            tile = switch (it) {
                            case NORTH_EAST -> RogueTile.CORNER_NE;
                            case SOUTH_EAST -> RogueTile.CORNER_SE;
                            case SOUTH_WEST -> RogueTile.CORNER_SW;
                            case NORTH_WEST -> RogueTile.CORNER_NW;
                            default -> null;
                            };
                            break;
                        }
                    }
                }
                if (tile == null) {
                    throw new IllegalStateException(emptyCartFaces + " " + emptyDiagFaces);
                }
                board.setTile(wall.x - boundingBox.ax, wall.z - boundingBox.az, tile);
            }
        }
    }
}
