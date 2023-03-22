package com.cavetale.crypt.rogue;

import com.cavetale.core.struct.Vec2i;
import com.cavetale.crypt.struct.Area;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.Axis;
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
    protected final List<RogueDoor> doors = new ArrayList<>();
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

    public int getArea() {
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

    protected RogueDoor getDoor(RogueRoom nbor) {
        for (RogueDoor door : doors) {
            if (door.otherRoom(this) == nbor) return door;
        }
        return null;
    }

    protected void findDoors(Random random) {
        for (RogueRoom nbor : nbors) {
            if (getDoor(nbor) == null) findDoor(nbor, random);
        }
    }

    protected void findDoor(RogueRoom nbor, Random random) {
        List<RogueDoor> possibleDoors = new ArrayList<>();
        for (int dz = 0; dz < board.size.z; dz += 1) {
            for (int dx = 0; dx < board.size.x; dx += 1) {
                RogueTile tile1 = board.getTile(dx, dz);
                if (tile1.type != RogueTile.Type.WALL) continue;
                final int x1 = dx + boundingBox.ax;
                final int z1 = dz + boundingBox.az;
                final int x2 = x1 + tile1.blockFace.getModX();
                final int z2 = z1 + tile1.blockFace.getModZ();
                if (!nbor.contains(x2, z2)) continue;
                RogueTile tile2 = nbor.board.getTile(x2 - nbor.boundingBox.ax, z2 - nbor.boundingBox.az);
                if (tile2.type != RogueTile.Type.WALL) continue;
                if (tile2.blockFace != tile1.blockFace.getOppositeFace()) continue;
                RogueDoor door = new RogueDoor(tile1.blockFace.getModX() != 0 ? Axis.X : Axis.Z,
                                               this, nbor, new Vec2i(x1, z1), new Vec2i(x2, z2));
                possibleDoors.add(door);
            }
        }
        assert !possibleDoors.isEmpty();
        RogueDoor door = possibleDoors.get(random.nextInt(possibleDoors.size()));
        doors.add(door);
        nbor.doors.add(door);
    }

    protected void placeDoors() {
        for (RogueDoor door : doors) {
            for (Vec2i tile : door.tiles) {
                if (contains(tile)) {
                    board.setTile(tile.x - boundingBox.ax, tile.z - boundingBox.az, RogueTile.DOOR);
                }
            }
        }
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
