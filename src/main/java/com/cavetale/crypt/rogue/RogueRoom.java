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
        boundingBox = mainArea;
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
        this.boundingBox = Area.getBoundingBox(areas);
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

    public boolean contains(int x, int z) {
        for (Area area : areas) {
            if (area.contains(x, z)) return true;
        }
        return false;
    }

    public Vec2i worldToRoom(Vec2i room) {
        return room.add(-boundingBox.ax, -boundingBox.az);
    }

    public Vec2i roomToWorld(Vec2i world) {
        return world.add(boundingBox.ax, boundingBox.az);
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
                    tile = RogueTile.wall(emptyCartFaces.iterator().next());
                } else if (emptyDiagFaces.size() == 1) {
                    tile = RogueTile.innerCorner(emptyDiagFaces.iterator().next());
                } else if (emptyCartFaces.size() == 2) {
                    int dx = 0;
                    int dz = 0;
                    for (BlockFace it : emptyCartFaces) {
                        if (it.getModX() != 0) dx = it.getModX();
                        if (it.getModZ() != 0) dz = it.getModZ();
                    }
                    for (BlockFace it : emptyDiagFaces) {
                        if (it.getModX() == dx && it.getModZ() == dz) {
                            tile = RogueTile.corner(it);
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
